package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.level.Sound

import java.util.concurrent.ThreadLocalRandom

class PlaySoundExecutor(
    val sound: Sound,
    val minPitch: Float = 0.8f,
    val maxPitch: Float = 1.2f,
    val minVolume: Float = 1f,
    val maxVolume: Float = 1f
) : IBehaviorExecutor {
    override fun execute(entity: EntityMob): Boolean {
        val volume =
            if (minVolume == maxVolume) minVolume else ThreadLocalRandom.current().nextFloat(minVolume, maxVolume)
        val pitch = if (minPitch == maxPitch) minPitch else ThreadLocalRandom.current().nextFloat(minPitch, maxPitch)
        entity.locator.level.addSound(entity.position, sound, volume, pitch)
        return false
    }
}
