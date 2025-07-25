package org.chorus_oss.chorus.level

import org.chorus_oss.chorus.GameMockExtension
import org.chorus_oss.chorus.level.format.LevelProvider
import org.chorus_oss.chorus.level.generator.Flat
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.GameLoop
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(GameMockExtension::class)
class GeneratorTest {
    @Test
    fun testCreate(level: Level, levelProvider: LevelProvider) {
        val loop = GameLoop.builder().loopCountPerSec(200).onTick { d: GameLoop ->
            level.scheduler.mainThreadHeartbeat(d.getTick())
        }.build()
        val thread = Thread { loop.startLoop() }
        thread.start()

        val flat = Flat(DimensionEnum.OVERWORLD.dimensionData, HashMap())
        flat.level = (level)
        val x = 10000
        val z = 10000
        val chunk = levelProvider.getChunk(x shr 4, z shr 4, true)!!
        flat.syncGenerate(chunk)
        loop.stop()
    }

    @Test
    fun testLight(level: Level, levelProvider: LevelProvider) {
        val loop = GameLoop.builder().loopCountPerSec(200).onTick { d: GameLoop ->
            level.scheduler.mainThreadHeartbeat(d.getTick())
        }.build()
        val thread = Thread { loop.startLoop() }
        thread.start()

        val flat = Flat(DimensionEnum.OVERWORLD.dimensionData, HashMap())
        flat.level = (level)
        val x = 10000
        val z = 10000
        val chunk = levelProvider.getChunk(x shr 4, z shr 4, true)!!
        flat.syncGenerate(chunk)
        val blockLightAt = level.getBlockLightAt(x, 4, z)
        val blockSkyLightAt = level.getBlockSkyLightAt(x, 4, z)
        val fullLight = level.getFullLight(Vector3(x.toDouble(), 4.0, z.toDouble()))
        val highestAdjacentBlockSkyLight = level.getHighestAdjacentBlockSkyLight(x, 4, z)
        //for flat, 0~4 is block,4 is top block
        Assertions.assertEquals(0, blockLightAt)
        Assertions.assertEquals(15, blockSkyLightAt)
        Assertions.assertEquals(15, fullLight)
        Assertions.assertEquals(15, highestAdjacentBlockSkyLight)
        Assertions.assertEquals(0, level.getBlockSkyLightAt(x, 3, z)) //so the skylight is 0
        loop.stop()
    }
}
