package org.chorus_oss.chorus.experimental.level.format

import com.mayakapps.rwmutex.ReadWriteMutex
import com.mayakapps.rwmutex.withReadLock
import com.mayakapps.rwmutex.withWriteLock
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.experimental.block.Block
import org.chorus_oss.chorus.experimental.block.BlockPermutation
import org.chorus_oss.chorus.experimental.block.components.LightDampeningComponent
import org.chorus_oss.chorus.experimental.block.components.TransparentComponent
import org.chorus_oss.chorus.experimental.block.generated.definitions.Air
import org.chorus_oss.chorus.level.biome.BiomeID
import org.chorus_oss.chorus.level.format.LevelProvider
import org.chorus_oss.chorus.math.Vector2i
import org.chorus_oss.chorus.math.Vector3i
import org.chorus_oss.chorus.utils.Loggable
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.plusAssign

@OptIn(ExperimentalAtomicApi::class)
class Chunk(
    val x: Int,
    val z: Int,
    val provider: LevelProvider,
) {
    val index: Long = index(x, z)

    var state: State
        get() = _state.load()
        set(value) = _state.store(value)

    private var _state = AtomicReference(State.New)

    private val subChunks = List(provider.dimensionData.chunkSectionCount) {
        SubChunk(it + provider.dimensionData.minSectionY)
    }

    private val height = MutableList<Short>(256) { 0 }

    private val entities: Pair<ReadWriteMutex, MutableMap<Long, Entity>> = Pair(ReadWriteMutex(), mutableMapOf())
    private val blockEntities: Pair<ReadWriteMutex, MutableMap<Long, BlockEntity>> = Pair(ReadWriteMutex(), mutableMapOf())
    private val blockToEntity: Pair<ReadWriteMutex, MutableMap<Long, BlockEntity>> = Pair(ReadWriteMutex(), mutableMapOf())

    private val subChunksRWM = ReadWriteMutex()
    private val heightRWM = ReadWriteMutex()
    private val biomeRWM = ReadWriteMutex()
    private val blockLightRWM = ReadWriteMutex()
    private val skyLightRWM = ReadWriteMutex()
    
    private val _changes = AtomicLong(0)

    val changes: Long
        get() = _changes.load()

    val changed: Boolean
        get() = changes != 0L

    private fun getSubChunk(y: Int): SubChunk? {
        return subChunks.getOrNull(y - provider.dimensionData.minSectionY)
    }

    suspend fun getPermutation(position: Vector3i, layer: Int = 0): BlockPermutation {
        val y = position.y shr 4
        subChunksRWM.withReadLock {
            val subChunk = getSubChunk(y) ?: return Air.default
            return subChunk.getPermutation(position, layer)
        }
    }

    suspend fun setPermutation(position: Vector3i, permutation: BlockPermutation, layer: Int = 0) {
        val y = position.y shr 4
        subChunksRWM.withWriteLock {
            getSubChunk(y)?.setPermutation(position, permutation, layer)
            _changes += 1
        }
    }

    suspend fun getSkyLight(position: Vector3i): Byte {
        val y = position.y shr 4
        skyLightRWM.withReadLock {
            val subChunk = getSubChunk(y) ?: return 0
            return subChunk.getSkyLight(position)
        }
    }

    suspend fun setSkyLight(position: Vector3i, value: Byte) {
        val y = position.y shr 4
        skyLightRWM.withWriteLock {
            getSubChunk(y)?.setSkyLight(position, value)
        }
    }

    suspend fun getBlockLight(position: Vector3i): Byte {
        val y = position.y shr 4
        blockLightRWM.withReadLock {
            val subChunk = getSubChunk(y) ?: return 0
            return subChunk.getBlockLight(position)
        }
    }

    suspend fun setBlockLight(position: Vector3i, value: Byte) {
        val y = position.y shr 4
        blockLightRWM.withWriteLock {
            getSubChunk(y)?.setBlockLight(position, value)
        }
    }

    suspend fun getHeight(position: Vector2i): Int {
        val index = (position.y shl 4) or position.x
        heightRWM.withReadLock {
            return height[index] + provider.dimensionData.minHeight
        }
    }

    suspend fun setHeight(position: Vector2i, value: Int) {
        val index = (position.y shl 4) or position.x
        heightRWM.withWriteLock {
            height[index] = (value - provider.dimensionData.minHeight).toShort()
        }
    }

    suspend fun recalculateHeight() {
        for (z in 0 .. 15) {
            for (x in 0 .. 15) {
                recalculateHeight(Vector2i(x, z))
            }
        }
    }

    suspend fun recalculateHeight(position: Vector2i) {
        var height = provider.dimensionData.minHeight
        for (y in provider.dimensionData.maxHeight downTo provider.dimensionData.minHeight) {
            val permutation = getPermutation(Vector3i(x, y, z))
            val light = permutation[LightDampeningComponent] ?: LightDampeningComponent.DEFAULT
            if (light.dampening > 1) {
                height = y
                break
            }
        }

        setHeight(position, height)
    }

    suspend fun populateSkyLight() {
        for (z in 0 .. 15) {
            for (x in 0 .. 15) {
                val top = getHeight(Vector2i(x, z))
                var y = provider.dimensionData.minHeight
                while (y > top) {
                    setSkyLight(Vector3i(x, y, z), 15)
                    --y
                }

                var light = 15
                var nextDecrease = 0

                y = top
                while (y >= provider.dimensionData.minHeight) {
                    light -= nextDecrease

                    light.coerceAtLeast(0)

                    setSkyLight(Vector3i(x, y, z), light.toByte())

                    if (light == 0) {
                        --y
                        continue
                    }

                    val permutation = getPermutation(Vector3i(x, y, z))
                    val transparentComponent = permutation[TransparentComponent] ?: TransparentComponent.DEFAULT
                    val lightComponent = permutation[LightDampeningComponent] ?: LightDampeningComponent.DEFAULT

                    if (!transparentComponent.transparent) {
                        light = 0
                    } else {
                        nextDecrease += lightComponent.dampening
                    }
                    --y
                }
            }
        }
    }

    suspend fun getBiome(position: Vector3i): Int {
        val y = position.y shr 4
        biomeRWM.withReadLock {
            val subChunk = getSubChunk(y) ?: return BiomeID.PLAINS
            return subChunk.getBiome(position)
        }
    }

    suspend fun setBiome(position: Vector3i, value: Int) {
        val y = position.y shr 4
        biomeRWM.withWriteLock {
            getSubChunk(y)?.setBiome(position, value)
            _changes += 1
        }
    }

    suspend fun addEntity(entity: Entity) {
        entities.first.withWriteLock {
            entities.second[entity.getRuntimeID()] = entity
            if (entity !is Player) _changes += 1
        }
    }

    suspend fun removeEntity(entity: Entity) {
        entities.first.withWriteLock {
            entities.second.remove(entity.getRuntimeID())
            if (entity !is Player) _changes += 1
        }
    }

    suspend fun addBlockEntity(blockEntity: BlockEntity) {
        val pos = blockEntity.position
        val index = ((pos.floorZ and 0xF) shl 16) or
                ((pos.floorX and 0xF) shl 12) or
                (pos.floorY.coerceIn(provider.dimensionData.minHeight, provider.dimensionData.maxHeight) + 64) // TODO: Why + 64 ?
        blockEntities.first.withWriteLock {
            blockEntities.second[blockEntity.id] = blockEntity
        }

        val existing = blockToEntity.first.withReadLock {
            val existing = blockToEntity.second[index.toLong()]
            if (existing != null && existing != blockEntity) {
                existing
            } else null
        }

        if (existing != null) {
            blockEntities.first.withWriteLock {
                blockEntities.second.remove(existing.id)
            }
            existing.close()
        }

        blockToEntity.first.withWriteLock {
            blockToEntity.second[index.toLong()] = blockEntity
        }
        _changes += 1
    }

    suspend fun removeBlockEntity(blockEntity: BlockEntity) {
        val pos = blockEntity.position
        val index = ((pos.floorZ and 0xF) shl 16) or
                ((pos.floorX and 0xF) shl 12) or
                (pos.floorY.coerceIn(provider.dimensionData.minHeight, provider.dimensionData.maxHeight) + 64) // TODO: Why + 64 ?

        blockEntities.first.withWriteLock {
            blockEntities.second.remove(blockEntity.id)
        }

        blockToEntity.first.withWriteLock {
            blockToEntity.second.remove(index.toLong())
        }

        _changes += 1
    }

    suspend fun getBlockEntity(position: Vector3i): BlockEntity? {
        val index = ((position.z and 0xF) shl 16) or
                ((position.x and 0xF) shl 12) or
                (position.y.coerceIn(provider.dimensionData.minHeight, provider.dimensionData.maxHeight) + 64) // TODO: Why + 64 ?
        return blockToEntity.first.withReadLock {
            blockToEntity.second[index.toLong()]
        }
    }

    suspend fun getSubChunkChanges(y: Int): Long {
        subChunksRWM.withReadLock {
            return getSubChunk(y)?.blockChanges?.load() ?: 0
        }
    }

    suspend fun getBlocks(from: Vector3i, to: Vector3i, condition: (Vector3i, BlockPermutation) -> Boolean = { _, _ -> true }, layer: Int = 0): List<Block> {
        val xOffset = x shl 4
        val zOffset = z shl 4
        subChunksRWM.withReadLock {
            return (0..(provider.dimensionData.chunkSectionCount - 1)).flatMap {
                val subChunk = subChunks.getOrNull(it)
                if (subChunk == null || subChunk.isEmpty) listOf()
                else subChunk.getBlocks(provider, xOffset, zOffset, from, to, condition, layer)
            }
        }
    }

    private suspend fun validateBlockEntity(position: Vector3i) {
        val entity = getBlockEntity(position)
        if (entity != null) {
            try {
                if (!entity.closed && entity.isBlockEntityValid) {
                    return
                }
            } catch (e: Exception) {
                try {
                    log.warn(
                        "Block entity validation of {} at {}, {} {} {} failed, removing as invalid.",
                        entity.javaClass.name,
                        provider.level.name,
                        entity.position.x,
                        entity.position.y,
                        entity.position.z,
                        e
                    )
                } catch (e2: Exception) {
                    e.addSuppressed(e2)
                    log.warn("Block entity validation failed", e)
                }
            }
            entity.close()
        }
    }

    enum class State(val value: Int) {
        New(-1),
        Generated(0),
        Populated(1),
        Finished(2);
    }

    companion object : Loggable {
        fun index(x: Int, y: Int): Long {
            val x = x.toLong()
            val y = y.toLong()
            return (x shl 32) or (y and 0xFFFFFFFF)
        }
    }
}