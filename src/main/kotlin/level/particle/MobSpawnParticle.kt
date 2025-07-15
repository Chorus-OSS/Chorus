package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.protocol.LevelEventPacket

class MobSpawnParticle(pos: Vector3, width: Float, height: Float) :
    Particle(pos.x, pos.y, pos.z) {
    private val width: Int = width.toInt()
    private val height: Int = height.toInt()

    override fun encode(): Array<DataPacket> {
        val packet = LevelEventPacket()
        packet.evid = LevelEventPacket.EVENT_PARTICLE_MOB_BLOCK_SPAWN
        packet.x = x.toFloat()
        packet.y = y.toFloat()
        packet.z = z.toFloat()
        packet.data = (this.width and 0xff) + ((this.height and 0xff) shl 8)

        return arrayOf(packet)
    }
}
