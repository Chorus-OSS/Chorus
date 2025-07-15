package org.chorus_oss.chorus.network.protocol

import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.PacketDecoder
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf


class MoveEntityAbsolutePacket : DataPacket() {
    @JvmField
    var eid: Long = 0

    @JvmField
    var x: Double = 0.0

    @JvmField
    var y: Double = 0.0

    @JvmField
    var z: Double = 0.0

    @JvmField
    var yaw: Double = 0.0

    @JvmField
    var headYaw: Double = 0.0

    @JvmField
    var pitch: Double = 0.0
    var onGround: Boolean = false
    var teleport: Boolean = false
    var forceMoveLocalEntity: Boolean = false

    override fun encode(byteBuf: HandleByteBuf) {
        byteBuf.writeActorRuntimeID(this.eid)
        var flags: Byte = 0
        if (onGround) {
            flags = (flags.toInt() or 0x01).toByte()
        }
        if (teleport) {
            flags = (flags.toInt() or 0x02).toByte()
        }
        if (forceMoveLocalEntity) {
            flags = (flags.toInt() or 0x04).toByte()
        }
        byteBuf.writeByte(flags.toInt())
        byteBuf.writeVector3f(x.toFloat(), y.toFloat(), z.toFloat())
        byteBuf.writeByte((this.pitch / (360.0 / 256.0)).toInt().toByte().toInt())
        byteBuf.writeByte((this.headYaw / (360.0 / 256.0)).toInt().toByte().toInt())
        byteBuf.writeByte((this.yaw / (360.0 / 256.0)).toInt().toByte().toInt())
    }

    override fun pid(): Int {
        return ProtocolInfo.MOVE_ENTITY_ABSOLUTE_PACKET
    }

    companion object : PacketDecoder<MoveEntityAbsolutePacket> {
        override fun decode(byteBuf: HandleByteBuf): MoveEntityAbsolutePacket {
            val packet = MoveEntityAbsolutePacket()
            packet.eid = byteBuf.readActorRuntimeID()
            val flags = byteBuf.readByte().toInt()
            packet.onGround = (flags and 0x01) != 0
            packet.teleport = (flags and 0x02) != 0
            packet.forceMoveLocalEntity = (flags and 0x04) != 0
            val v = byteBuf.readVector3f()
            packet.x = v.x.toDouble()
            packet.y = v.y.toDouble()
            packet.z = v.z.toDouble()
            packet.pitch = byteBuf.readByte() * (360.0 / 256.0)
            packet.headYaw = byteBuf.readByte() * (360.0 / 256.0)
            packet.yaw = byteBuf.readByte() * (360.0 / 256.0)
            return packet
        }

        const val FLAG_GROUND: Byte = 0x01
        const val FLAG_TELEPORT: Byte = 0x02
        const val FLAG_FORCE_MOVE_LOCAL_ENTITY: Byte = 0x04
    }
}
