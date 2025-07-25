package org.chorus_oss.chorus.level.format.palette

import io.netty.buffer.ByteBuf
import org.chorus_oss.chorus.block.BlockAir
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.level.AntiXraySystem
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.format.SubChunk
import org.chorus_oss.chorus.level.format.bitarray.BitArrayVersion
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.ByteBufVarInt
import org.chorus_oss.chorus.utils.ChorusRandom
import java.util.concurrent.atomic.AtomicLong

class BlockPalette : Palette<BlockState> {
    private var needReObfuscate = true
    private var obfuscatePalette: BlockPalette? = null
    private var blockChangeCache: Long = 0

    constructor(first: BlockState) : super(first, mutableListOf(), BitArrayVersion.V2)

    constructor(first: BlockState, version: BitArrayVersion) : super(first, version)

    constructor(first: BlockState, palette: MutableList<BlockState>, version: BitArrayVersion) : super(
        first,
        palette,
        version
    )

    override fun set(index: Int, value: BlockState) {
        super.set(index, value)
        if (obfuscatePalette != null) {
            obfuscatePalette!![index] = value
        }
    }

    fun writeObfuscatedToNetwork(
        level: Level,
        blockChanges: AtomicLong,
        byteBuf: ByteBuf,
        serializer: RuntimeDataSerializer<BlockState>
    ) {
        val realOreToFakeMap = level.antiXraySystem!!.rawRealOreToReplacedRuntimeIdMap
        val fakeBlockMap = level.antiXraySystem!!.rawFakeOreToPutRuntimeIdMap
        val transparentBlockSet: MutableSet<Int> = AntiXraySystem.rawTransparentBlockRuntimeIds
        val xAndDenominator = level.antiXraySystem!!.fakeOreDenominator - 1
        val chorusRandom = ChorusRandom(level.seed)

        var write = if (obfuscatePalette == null) this else obfuscatePalette!!
        if (needReObfuscate) {
            blockChangeCache = blockChanges.get()
            if (obfuscatePalette == null) {
                obfuscatePalette = BlockPalette(BlockAir.STATE)
                this.copyTo(obfuscatePalette!!)
            }
            for (i in 0..<SubChunk.SIZE) {
                val x = (i shr 8) and 0xF
                val z = (i shr 4) and 0xF
                val y = i and 0xF
                var rid = get(i).blockStateHash()
                if (x != 0 && z != 0 && y != 0 && x != 15 && z != 15 && y != 15) {
                    val tmp = realOreToFakeMap.getOrDefault(rid, Int.MAX_VALUE)
                    if (tmp != Int.MAX_VALUE && canBeObfuscated(transparentBlockSet, x, y, z)) {
                        rid = tmp
                    } else {
                        val tmp2 = fakeBlockMap[rid]
                        if (tmp2 != null && (chorusRandom.nextInt() and xAndDenominator) == 0 && canBeObfuscated(
                                transparentBlockSet,
                                x,
                                y,
                                z
                            )
                        ) {
                            rid = tmp2[chorusRandom.nextInt(0, tmp2.size - 1)]
                        }
                    }
                }
                obfuscatePalette!![i] = Registries.BLOCKSTATE[rid]!!
            }
            this.needReObfuscate = false
            write = obfuscatePalette!!
        }

        byteBuf.writeByte(getPaletteHeader(write.bitArray.version, true))
        for (word in write.bitArray.words) byteBuf.writeIntLE(word)
        bitArray.writeSizeToNetwork(byteBuf, write.palette.size)
        for (value in write.palette) ByteBufVarInt.writeInt(byteBuf, serializer.serialize(value))
    }

    fun setNeedReObfuscate() {
        this.needReObfuscate = true
    }

    private fun canBeObfuscated(transparentBlockSet: MutableSet<Int>, x: Int, y: Int, z: Int): Boolean {
        return !transparentBlockSet.contains(
            get(
                IChunk.index(
                    x + 1,
                    y,
                    z
                )
            ).blockStateHash()
        ) && !transparentBlockSet.contains(
            get(IChunk.index(x - 1, y, z)).blockStateHash()
        ) && !transparentBlockSet.contains(
            get(
                IChunk.index(
                    x,
                    y + 1,
                    z
                )
            ).blockStateHash()
        ) && !transparentBlockSet.contains(
            get(IChunk.index(x, y - 1, z)).blockStateHash()
        ) && !transparentBlockSet.contains(
            get(
                IChunk.index(
                    x,
                    y,
                    z + 1
                )
            ).blockStateHash()
        ) && !transparentBlockSet.contains(
            get(IChunk.index(x, y, z - 1)).blockStateHash()
        )
    }
}
