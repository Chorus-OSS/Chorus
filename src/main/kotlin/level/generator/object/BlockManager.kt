package org.chorus_oss.chorus.level.generator.`object`

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.format.UnsafeChunk
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.types.BlockPos
import java.util.function.Consumer
import java.util.function.Predicate

class BlockManager(val level: Level) {
    private val caches = mutableMapOf<Long, Block?>()
    private val places = mutableMapOf<Long, Block>()

    private fun hashXYZ(x: Int, y: Int, z: Int, layer: Int): Long {
        val v = (if (layer == 1) 0xFFFFFFF else 0x7FFFFFF).toLong()
        return ((x.toLong() and v) shl 37) or ((level.ensureY(y) + 64).toLong() shl 28) or (z.toLong() and 0xFFFFFFFL)
    }

    fun getBlockIdAt(x: Int, y: Int, z: Int): String {
        return this.getBlockIdAt(x, y, z, 0)
    }

    fun getBlockIdAt(x: Int, y: Int, z: Int, layer: Int): String {
        val block = caches.computeIfAbsent(
            hashXYZ(x, y, z, layer)
        ) { level.getBlock(x, y, z, layer) }
        return block!!.id
    }

    fun getBlockAt(vector3: Vector3): Block? {
        return getBlockAt(vector3.floorX, vector3.floorY, vector3.floorZ)
    }

    fun getBlockAt(x: Int, y: Int, z: Int): Block? {
        return caches.computeIfAbsent(
            hashXYZ(x, y, z, 0)
        ) { level.getBlock(x, y, z) }
    }

    fun setBlockStateAt(blockVector3: Vector3, blockState: BlockState) {
        this.setBlockStateAt(blockVector3.floorX, blockVector3.floorY, blockVector3.floorZ, blockState)
    }

    fun setBlockStateAt(blockVector3: BlockVector3, blockState: BlockState) {
        this.setBlockStateAt(blockVector3.x, blockVector3.y, blockVector3.z, blockState)
    }

    fun setBlockStateAt(x: Int, y: Int, z: Int, state: BlockState) {
        val hashXYZ = hashXYZ(x, y, z, 0)
        val block = Block.get(state, level, x, y, z, 0)
        places.put(hashXYZ, block)
        caches.put(hashXYZ, block)
    }

    fun setBlockStateAt(x: Int, y: Int, z: Int, layer: Int, state: BlockState) {
        val hashXYZ = hashXYZ(x, y, z, layer)
        val block = Block.get(state, level, x, y, z, layer)
        places.put(hashXYZ, block)
        caches.put(hashXYZ, block)
    }

    fun setBlockStateAt(x: Int, y: Int, z: Int, blockId: String) {
        val hashXYZ = hashXYZ(x, y, z, 0)
        val block = Block.get(blockId, level, x, y, z, 0)
        places.put(hashXYZ, block)
        caches.put(hashXYZ, block)
    }

    fun getChunk(chunkX: Int, chunkZ: Int): IChunk {
        return level.getChunk(chunkX, chunkZ)
    }

    val seed: Long
        get() = level.seed

    val isOverWorld: Boolean
        get() = level.isOverWorld

    val isNether: Boolean
        get() = level.isNether

    val isTheEnd: Boolean
        get() = level.isTheEnd

    val blocks: MutableList<Block>
        get() = ArrayList(places.values)

    fun applyBlockUpdate() {
        for (b in places.values) {
            level.setBlock(b.position, b, direct = true, update = true)
        }
    }

    @JvmOverloads
    fun applySubChunkUpdate(
        blockList: List<Block> = ArrayList<Block>(
            places.values
        ), predicate: Predicate<Block>? = null
    ) {
        var blockList1 = blockList
        if (predicate != null) {
            blockList1 = blockList1.stream().filter(predicate).toList()
        }
        val chunks: MutableMap<IChunk, ArrayList<Block>> = HashMap()
        val batches: MutableMap<SubChunkEntry, org.chorus_oss.protocol.packets.UpdateSubChunkBlocksPacket> =
            HashMap()
        for (b in blockList1) {
            val chunk = chunks.computeIfAbsent(
                level.getChunk(b.position.chunkX, b.position.chunkZ, true)
            ) { _: IChunk? -> ArrayList() }
            chunk.add(b)
            batches.computeIfAbsent(
                SubChunkEntry(b.position.chunkX shl 4, (b.position.floorY shr 4) shl 4, b.position.chunkZ shl 4)
            ) { s: SubChunkEntry ->
                val pk = org.chorus_oss.protocol.packets.UpdateSubChunkBlocksPacket(
                    position = BlockPos(s.x, s.y, s.z),
                    blocks = when (b.layer) {
                        0 -> listOf(
                            org.chorus_oss.protocol.types.BlockChangeEntry(
                                blockPos = BlockPos(b.position),
                                blockRuntimeID = b.blockState.blockStateHash().toUInt(),
                                flags = 21u,
                                syncedUpdateEntityUniqueID = -1,
                                syncedUpdateType = org.chorus_oss.protocol.types.BlockChangeEntry.Companion.MessageType.None,
                            )
                        )

                        else -> emptyList()
                    },
                    extra = when (b.layer) {
                        0 -> emptyList()
                        else -> listOf(
                            org.chorus_oss.protocol.types.BlockChangeEntry(
                                blockPos = BlockPos(b.position),
                                blockRuntimeID = b.blockState.blockStateHash().toUInt(),
                                flags = 21u,
                                syncedUpdateEntityUniqueID = -1,
                                syncedUpdateType = org.chorus_oss.protocol.types.BlockChangeEntry.Companion.MessageType.None,
                            )
                        )
                    }
                )
                pk
            }
        }
        chunks.entries.parallelStream()
            .forEach { entry ->
                val key: IChunk = entry.key
                val value: ArrayList<Block> = entry.value
                key.batchProcess { unsafeChunk: UnsafeChunk ->
                    value.forEach(Consumer { b: Block ->
                        unsafeChunk.setBlockState(
                            b.position.floorX and 15,
                            b.position.floorY,
                            b.position.floorZ and 15,
                            b.blockState,
                            b.layer
                        )
                    })
                    unsafeChunk.recalculateHeightMap()
                }
                key.reObfuscateChunk()
            }
        for (p in batches.values) {
            Server.broadcastPacket(level.players.values, p)
        }
        places.clear()
        caches.clear()
    }

    val maxHeight: Int
        get() = level.maxHeight

    val minHeight: Int
        get() = level.minHeight

    @JvmRecord
    private data class SubChunkEntry(val x: Int, val y: Int, val z: Int)
}
