package org.chorus_oss.chorus.entity.projectile.throwable

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.mob.monster.EntityBlaze
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.GenericParticle
import org.chorus_oss.chorus.level.particle.Particle
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.DataPacket
import java.util.concurrent.ThreadLocalRandom


class EntitySnowball @JvmOverloads constructor(chunk: IChunk?, nbt: CompoundTag, shootingEntity: Entity? = null) :
    EntityThrowable(chunk, nbt, shootingEntity) {
    override fun getEntityIdentifier(): String {
        return EntityID.SNOWBALL
    }

    override fun getWidth(): Float {
        return 0.25f
    }

    override fun getLength(): Float {
        return 0.25f
    }

    override fun getHeight(): Float {
        return 0.25f
    }

    override fun getGravity(): Float {
        return 0.03f
    }

    override fun getDrag(): Float {
        return 0.01f
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (this.closed) {
            return false
        }

        var hasUpdate: Boolean = super.onUpdate(currentTick)

        if (this.age > 1200 || this.isCollided) {
            this.kill()
            hasUpdate = true
        }

        return hasUpdate
    }

    override fun getResultDamage(entity: Entity?): Int {
        return if (entity is EntityBlaze) 3 else super.getResultDamage(entity)
    }

    override fun addHitEffect() {
        val particles: Int = nextParticleCount()
        val particlePackets: Array<DataPacket> = GenericParticle(this.position, Particle.TYPE_SNOWBALL_POOF).encode()
        val length: Int = particlePackets.size
        val allPackets: Array<DataPacket> = Array(length * particles) { i ->
            particlePackets[i % length]
        }
        val chunkX: Int = position.x.toInt() shr 4
        val chunkZ: Int = position.z.toInt() shr 4
        val level: Level = level!!
        for (p in allPackets) {
            Server.broadcastPacket(level.getChunkPlayers(chunkX, chunkZ).values, p)
        }
    }

    override fun getOriginalName(): String {
        return "Snowball"
    }

    companion object {
        private val particleCounts: ByteArray = ByteArray(24)
        private var particleIndex: Int = 0

        init {
            for (i in particleCounts.indices) {
                particleCounts[i] = (ThreadLocalRandom.current().nextInt(10) + 5).toByte()
            }
        }

        private fun nextParticleCount(): Int {
            var index: Int = particleIndex++
            if (index >= particleCounts.size) {
                index = 0
                particleIndex = index
            }
            return particleCounts.get(index).toInt()
        }
    }
}
