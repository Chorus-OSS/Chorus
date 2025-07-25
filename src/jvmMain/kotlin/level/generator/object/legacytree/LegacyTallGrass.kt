package org.chorus_oss.chorus.level.generator.`object`.legacytree

import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom
import kotlin.math.abs

object LegacyTallGrass {
    private val places = arrayOf(
        // Total: 106
        BlockShortGrass.properties.defaultState,  // 50
        BlockTallGrass.properties.defaultState,  // 30
        BlockDandelion.properties.defaultState,  // 10
        BlockPoppy.properties.defaultState,  // 10
        BlockAzureBluet.properties.defaultState,  // 5
        BlockOxeyeDaisy.properties.defaultState,  // 5
        BlockAllium.properties.defaultState,  // 2
        BlockCornflower.properties.defaultState,  // 2
        BlockBlueOrchid.properties.defaultState,  // 2
        BlockLilyOfTheValley.properties.defaultState,  // 2
        BlockRedTulip.properties.defaultState,  // 2
        BlockOrangeTulip.properties.defaultState,  // 2
        BlockPinkTulip.properties.defaultState,  // 2
        BlockWhiteTulip.properties.defaultState,  // 2
    )

    fun growGrass(level: BlockManager, pos: Vector3, random: ChorusRandom) {
        val y = pos.floorY + 1
        val minx = pos.floorX - 2
        val minz = pos.floorZ - 2
        val maxx = pos.floorX + 2
        val maxz = pos.floorZ + 2
        for (x in minx..maxx) {
            for (z in minz..maxz) {
                val newY: Int = y + random.nextInt(2) * (if (random.nextBoolean()) -1 else 1)
                if (random.nextBoolean()) {
                    if (level.getBlockIdAt(x, newY, z) == BlockID.AIR && level.getBlockIdAt(
                            x,
                            newY - 1,
                            z
                        ) == BlockID.GRASS_BLOCK
                    ) {
                        val ranNumber = Math.round(random.nextGaussian() * 1000).toInt()
                        val absRn = abs(ranNumber)
                        if (-300 <= ranNumber && ranNumber <= 300) {
                            level.setBlockStateAt(x, newY, z, places[0])
                        } else if (absRn in 300..500) { //-300 ~ -500 + 300 ~ 50
                            level.setBlockStateAt(x, newY, z, places[1])
                            val block: BlockTallGrass = BlockTallGrass()
                            block.isTopHalf = (true)
                            level.level.setBlock(x, newY + 1, z, block, false, false)
                        } else if (ranNumber in 500..599) {
                            level.setBlockStateAt(x, newY, z, places[2])
                        } else if (-600 <= ranNumber && ranNumber <= -500) {
                            level.setBlockStateAt(x, newY, z, places[3])
                        } else if (ranNumber in 600..699) {
                            level.setBlockStateAt(x, newY, z, places[4])
                        } else if (-700 <= ranNumber && ranNumber < -600) {
                            level.setBlockStateAt(x, newY, z, places[5])
                        } else if (-750 <= ranNumber && ranNumber < -700) {
                            level.setBlockStateAt(x, newY, z, places[6])
                        } else if (-800 <= ranNumber && ranNumber < -750) {
                            level.setBlockStateAt(x, newY, z, places[7])
                        } else if (-850 <= ranNumber && ranNumber < -800) {
                            level.setBlockStateAt(x, newY, z, places[8])
                        } else if (-900 <= ranNumber && ranNumber < -850) {
                            level.setBlockStateAt(x, newY, z, places[9])
                        } else if (-1000 <= ranNumber && ranNumber < -900) {
                            level.setBlockStateAt(x, newY, z, places[10])
                        } else if (ranNumber in 700..799) {
                            level.setBlockStateAt(x, newY, z, places[11])
                        } else if (ranNumber in 800..899) {
                            level.setBlockStateAt(x, newY, z, places[12])
                        } else if (ranNumber in 900..999) {
                            level.setBlockStateAt(x, newY, z, places[13])
                        }
                    }
                }
            }
        }
    }
}

