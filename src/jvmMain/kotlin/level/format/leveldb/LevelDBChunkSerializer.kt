package org.chorus_oss.chorus.level.format.leveldb

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockAir
import org.chorus_oss.chorus.block.BlockUnknown
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.format.*
import org.chorus_oss.chorus.level.format.bitarray.BitArrayVersion
import org.chorus_oss.chorus.level.format.palette.BlockPalette
import org.chorus_oss.chorus.level.format.palette.Palette
import org.chorus_oss.chorus.level.util.LevelDBKeyUtil
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.Utils
import org.iq80.leveldb.DB
import org.iq80.leveldb.WriteBatch
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class LevelDBChunkSerializer private constructor() {
    fun serialize(writeBatch: WriteBatch, chunk: IChunk) {
        chunk.batchProcess { unsafeChunk ->
            try {
                writeBatch.put(
                    LevelDBKeyUtil.VERSION.getKey(
                        unsafeChunk.x,
                        unsafeChunk.z,
                        unsafeChunk.dimensionData
                    ), byteArrayOf(IChunk.VERSION.toByte())
                )
                writeBatch.put(
                    LevelDBKeyUtil.FINALIZED_STATE.getKey(
                        unsafeChunk.x,
                        unsafeChunk.z,
                        unsafeChunk.dimensionData
                    ), Unpooled.buffer(4).writeIntLE(unsafeChunk.chunkState.ordinal - 1).array()
                )
                serializeSubChunks(writeBatch, unsafeChunk)
                serializeHeightAndBiome(writeBatch, unsafeChunk)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        // Spawning block entities requires call the getSpawnPacket method,
        // which is easy to call Level#getBlock, which can cause a deadlock,
        // so handle it without locking
        serializeEntities(writeBatch, chunk)
    }

    @Throws(IOException::class)
    fun deserialize(db: DB, builder: IChunkBuilder) {
        db[LevelDBKeyUtil.VERSION.getKey(builder.chunkX, builder.chunkZ, builder.dimensionData)] ?: return
        val finalized =
            db[LevelDBKeyUtil.FINALIZED_STATE.getKey(builder.chunkX, builder.chunkZ, builder.dimensionData)]
        if (finalized == null) {
            builder.state(ChunkState.FINISHED)
        } else {
            val byteBuf = Unpooled.wrappedBuffer(finalized)
            val i = if (byteBuf.readableBytes() >= 4) byteBuf.readIntLE() else byteBuf.readByte().toInt()
            builder.state(ChunkState.entries[i + 1])
        }

        deserializeSubChunks(db, builder)
        deserializeHeightAndBiome(db, builder)
        deserializeEntities(db, builder)
    }

    private fun serializeSubChunks(writeBatch: WriteBatch, chunk: UnsafeChunk) {
        val sections = chunk.sections
        for (section in sections) {
            section ?: continue

            val buffer = ByteBufAllocator.DEFAULT.ioBuffer()
            try {
                buffer.writeByte(SubChunk.VERSION)
                buffer.writeByte(section.layers.size)
                buffer.writeByte(section.y.toInt())
                for (layer in section.layers) {
                    layer.writeToStoragePersistent(buffer) { it.blockStateTag }
                }
                writeBatch.put(
                    LevelDBKeyUtil.CHUNK_SECTION_PREFIX.getKey(
                        chunk.x,
                        chunk.z,
                        section.y.toInt(),
                        chunk.provider.dimensionData
                    ), Utils.convertByteBuf2Array(buffer)
                )
            } finally {
                buffer.release()
            }
        }
    }

    private fun deserializeSubChunks(db: DB, builder: IChunkBuilder) {
        val dimensionData = builder.dimensionData
        val minSectionY = dimensionData.minSectionY

        val sections = arrayOfNulls<SubChunk>(dimensionData.chunkSectionCount)
        for (sectionY in minSectionY..dimensionData.maxSectionY) {
            val bytes = db[LevelDBKeyUtil.CHUNK_SECTION_PREFIX.getKey(
                builder.chunkX, builder.chunkZ, sectionY,
                dimensionData
            )] ?: continue

            val byteBuf = ByteBufAllocator.DEFAULT.ioBuffer()
            try {
                byteBuf.writeBytes(bytes)

                var layers = 2

                val iSubChunkVersion = byteBuf.readByte().toInt()
                if (iSubChunkVersion == 8 || iSubChunkVersion == 9) {
                    layers = byteBuf.readByte().toInt()
                    if (iSubChunkVersion == 9) {
                        byteBuf.readByte() // sectionY not in use
                    }
                }

                when (iSubChunkVersion) {
                    1, 8, 9 -> {
                        val section: SubChunk
                        if (layers <= 2) {
                            section = SubChunk(sectionY.toByte())
                        } else {
                            val palettes = Array(layers) {
                                BlockPalette(
                                    BlockAir.properties.defaultState,
                                    BitArrayVersion.V2
                                )
                            }
                            section = SubChunk(sectionY.toByte(), palettes)
                        }
                        var layer = 0
                        while (layer < layers) {
                            section.layers[layer].readFromStoragePersistent(
                                byteBuf
                            ) { hash: Int ->
                                val blockState = Registries.BLOCKSTATE[hash]
                                    ?: return@readFromStoragePersistent BlockUnknown.properties.defaultState
                                blockState
                            }
                            layer++
                        }
                        sections[sectionY - minSectionY] = section
                    }
                }
            } finally {
                byteBuf.release()
            }
        }
        builder.sections(sections)
    }

    private fun serializeHeightAndBiome(writeBatch: WriteBatch, chunk: UnsafeChunk) {
        val heightAndBiomesBuffer = ByteBufAllocator.DEFAULT.ioBuffer()
        try {
            for (height in chunk.heightMapArray) {
                heightAndBiomesBuffer.writeShortLE(height.toInt())
            }
            var biomePalette: Palette<Int>? = null
            for (sectionY in chunk.provider.dimensionData.minSectionY..chunk.provider.dimensionData.maxSectionY) {
                val section = chunk.getSection(sectionY) ?: continue
                section.biomes.writeToStorageRuntime(heightAndBiomesBuffer, { it }, biomePalette)
                biomePalette = section.biomes
            }
            if (heightAndBiomesBuffer.readableBytes() > 0) {
                writeBatch.put(
                    LevelDBKeyUtil.DATA_3D.getKey(chunk.x, chunk.z, chunk.provider.dimensionData),
                    Utils.convertByteBuf2Array(heightAndBiomesBuffer)
                )
            }
        } finally {
            heightAndBiomesBuffer.release()
        }
    }

    private fun deserializeHeightAndBiome(db: DB, builder: IChunkBuilder) {
        var heightAndBiomesBuffer: ByteBuf? = null
        try {
            val dimensionInfo = builder.dimensionData

            val bytes = db[LevelDBKeyUtil.DATA_3D.getKey(
                builder.chunkX, builder.chunkZ,
                dimensionInfo
            )]

            if (bytes != null) {
                heightAndBiomesBuffer = Unpooled.wrappedBuffer(bytes)
                val heights = ShortArray(256)
                for (i in 0..255) {
                    heights[i] = heightAndBiomesBuffer.readShortLE()
                }
                builder.heightMap(heights)
                var lastPalette: Palette<Int>? = null
                val minSectionY = builder.dimensionData.minSectionY
                for (y in minSectionY..builder.dimensionData.maxSectionY) {
                    val section = builder.sections[y - minSectionY] ?: continue
                    section.biomes.readFromStorageRuntime(
                        heightAndBiomesBuffer,
                        { i: Int -> i }, lastPalette
                    )
                    lastPalette = section.biomes
                }
            }
        } finally {
            heightAndBiomesBuffer?.release()
        }
    }

    private fun deserializeEntities(db: DB, builder: IChunkBuilder) {
        val dimensionInfo = builder.dimensionData

        val blockEntityTags: MutableList<CompoundTag> = ArrayList()

        run {
            val tileBytes = db[LevelDBKeyUtil.BLOCK_ENTITIES.getKey(
                builder.chunkX, builder.chunkZ,
                dimensionInfo
            )] ?: return@run

            try {
                BufferedInputStream(ByteArrayInputStream(tileBytes)).use { stream ->
                    while (stream.available() > 0) {
                        blockEntityTags.add(NBTIO.read(stream, ByteOrder.LITTLE_ENDIAN))
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        builder.blockEntities(blockEntityTags)

        val entityTags: MutableList<CompoundTag> = ArrayList()

        run oldEntities@{
            val entityBytes = db[LevelDBKeyUtil.ENTITIES.getKey(
                builder.chunkX, builder.chunkZ,
                dimensionInfo
            )] ?: return@oldEntities

            try {
                BufferedInputStream(ByteArrayInputStream(entityBytes)).use { stream ->
                    while (stream.available() > 0) {
                        entityTags.add(NBTIO.read(stream, ByteOrder.LITTLE_ENDIAN))
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        run newEntities@{
            val chunkKey: ByteArray = LevelDBKeyUtil.getChunkKey(builder.chunkX, builder.chunkZ, dimensionInfo)
            val entitiesKey = LevelDBKeyUtil.NEW_ENTITIES_KEY
            val key = ByteBuffer
                .allocate(entitiesKey.size + chunkKey.size)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(entitiesKey)
                .put(chunkKey)
                .array()
            val newEntityBytes = db[key] ?: return@newEntities

            try {
                BufferedInputStream(ByteArrayInputStream(newEntityBytes)).use { stream ->
                    while (stream.available() > 0) {
                        val bytes = ByteArray(8)
                        val bytesRead = stream.read(bytes)
                        if (bytesRead != 8) {
                            throw IOException("Failed to read 8 bytes for Int64")
                        }

                        val entityId = ByteBuffer
                            .wrap(bytes)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .getLong()

                        val entityBytes =
                            db[LevelDBKeyUtil.getEntityKey(entityId)] ?: throw IOException("Failed to read entity key")

                        entityTags.add(NBTIO.read(entityBytes, ByteOrder.LITTLE_ENDIAN))
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        builder.entities(entityTags)
    }

    private fun serializeEntities(writeBatch: WriteBatch, chunk: IChunk) {
        val blockEntities: Collection<BlockEntity> = chunk.blockEntities.values
        val tileBuffer = ByteBufAllocator.DEFAULT.ioBuffer()
        try {
            ByteBufOutputStream(tileBuffer).use { bufStream ->
                val key = LevelDBKeyUtil.BLOCK_ENTITIES.getKey(chunk.x, chunk.z, chunk.provider.dimensionData)
                if (blockEntities.isEmpty()) writeBatch.delete(key)
                else {
                    for (blockEntity in blockEntities) {
                        blockEntity.saveNBT()
                        NBTIO.write(blockEntity.namedTag, bufStream, ByteOrder.LITTLE_ENDIAN)
                    }
                    writeBatch.put(key, Utils.convertByteBuf2Array(tileBuffer))
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            tileBuffer.release()
        }

        val entities: Collection<Entity> = chunk.entities.values
        val entityBuffer = ByteBufAllocator.DEFAULT.ioBuffer()
        try {
            ByteBufOutputStream(entityBuffer).use { bufStream ->
                val chunkKey: ByteArray = LevelDBKeyUtil.getChunkKey(chunk.x, chunk.z, chunk.provider.dimensionData)
                val entityKey = ByteBuffer
                    .allocate(LevelDBKeyUtil.NEW_ENTITIES_KEY.size + chunkKey.size)
                    .put(LevelDBKeyUtil.NEW_ENTITIES_KEY)
                    .put(chunkKey)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .array()

                for (e in entities) {
                    if ((e !is Player) && !e.closed && e.canBeSavedWithChunk()) {
                        e.saveNBT()

                        val entityUUID = e.uniqueId

                        bufStream.write(
                            ByteBuffer
                                .allocate(java.lang.Long.BYTES)
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .putLong(entityUUID)
                                .array()
                        )

                        val entityTagBytes = NBTIO.write(e.namedTag!!, ByteOrder.LITTLE_ENDIAN)

                        writeBatch.put(LevelDBKeyUtil.getEntityKey(entityUUID), entityTagBytes)
                    }
                }
                writeBatch.put(entityKey, Utils.convertByteBuf2Array(entityBuffer))
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            entityBuffer.release()
        }
    }

    companion object {
        val INSTANCE: LevelDBChunkSerializer = LevelDBChunkSerializer()
    }
}
