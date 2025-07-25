package org.chorus_oss.chorus.experimental.level.format

import org.chorus_oss.chorus.experimental.block.Block
import org.chorus_oss.chorus.experimental.block.BlockPermutation
import org.chorus_oss.chorus.experimental.block.generated.definitions.Air
import org.chorus_oss.chorus.level.biome.BiomeID
import org.chorus_oss.chorus.level.format.LevelProvider
import org.chorus_oss.chorus.level.format.palette.Palette
import org.chorus_oss.chorus.math.Vector3i
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.plusAssign

@OptIn(ExperimentalAtomicApi::class)
data class SubChunk(
    val y: Int,
    val layers: MutableList<Palette<BlockPermutation>>,
    val biomes: Palette<Int>,
    val blockLight: MutableList<Byte>,
    val skyLight: MutableList<Byte>,
    val blockChanges: AtomicLong
) {
    constructor(y: Int) : this(
        y,
        mutableListOf(
            Palette(Air.default),
            Palette(Air.default),
        ),
        Palette(BiomeID.PLAINS),
        MutableList(SIZE) { 0 },
        MutableList(SIZE) { 0 },
        AtomicLong(0)
    )

    constructor(y: Int, layers: MutableList<Palette<BlockPermutation>>) : this(
        y,
        layers,
        Palette(BiomeID.PLAINS),
        MutableList(SIZE) { 0 },
        MutableList(SIZE) { 0 },
        AtomicLong(0)
    )

    val yOffset = y shl 4

    val isEmpty: Boolean
        get() = layers.all { it.isEmpty && it[0] == Air.default }

    fun getPermutation(position: Vector3i, layer: Int = 0): BlockPermutation {
        return layers[layer][index(position)]
    }

    fun setPermutation(position: Vector3i, value: BlockPermutation, layer: Int = 0) {
        blockChanges += 1
        layers[layer][index(position)] = value
    }

    fun getBiome(position: Vector3i): Int {
        return biomes[index(position)]
    }

    fun setBiome(position: Vector3i, value: Int) {
        biomes[index(position)] = value
    }

    fun getBlockLight(position: Vector3i): Byte {
        return blockLight[index(position)]
    }

    fun setBlockLight(position: Vector3i, value: Byte) {
        blockLight[index(position)] = value
    }

    fun getSkyLight(position: Vector3i): Byte {
        return skyLight[index(position)]
    }

    fun setSkyLight(position: Vector3i, value: Byte) {
        skyLight[index(position)] = value
    }

    fun getBlocks(
        provider: LevelProvider,
        xOffset: Int,
        zOffset: Int,
        from: Vector3i,
        to: Vector3i,
        condition: (Vector3i, BlockPermutation) -> Boolean = { _, _ -> true },
        layer: Int = 0,
    ): List<Block> {
        val from = Vector3i(
            (from.x - xOffset).coerceIn(0, 15),
            (from.y - yOffset).coerceIn(0, 15),
            (from.z - zOffset).coerceIn(0, 15),
        )

        val to = Vector3i(
            (to.x - xOffset).coerceIn(0, 15),
            (to.y - yOffset).coerceIn(0, 15),
            (to.z - zOffset).coerceIn(0, 15),
        )

        val result = mutableListOf<Block>()
        for (x in from.x .. to.x) {
            for (y in from.y .. to.y) {
                for (z in from.z .. to.z) {
                    val position = Vector3i(x, y, z)
                    val permutation = getPermutation(position, layer)
                    if (condition(position, permutation)) {
                        result.add(
                            Block(
                                permutation,
                                position,
                                layer,
                                provider.level
                            )
                        )
                    }
                }
            }
        }
        return result
    }

    companion object {
        const val SIZE = 16 * 16 * 16
        const val VERSION = 9

        fun index(position: Vector3i): Int {
            /**
             * Chunk Order
             * - Bedrock: `XZY`
             * - Java: `YZX`
             */
            return (position.x.coerceIn(0, 15) shl 8) or (position.z.coerceIn(0, 15) shl 4) or position.y.coerceIn(0, 15)
        }
    }
}
