package org.chorus_oss.chorus.network.protocol

import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.PacketDecoder
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf

class MovePlayerPacket : DataPacket() {
    @JvmField
    var eid: Long = 0

    @JvmField
    var x: Float = 0f

    @JvmField
    var y: Float = 0f

    @JvmField
    var z: Float = 0f

    @JvmField
    var yaw: Float = 0f

    @JvmField
    var headYaw: Float = 0f

    @JvmField
    var pitch: Float = 0f

    @JvmField
    var mode: Int = MODE_NORMAL

    @JvmField
    var onGround: Boolean = false

    @JvmField
    var ridingEid: Long = 0
    var teleportationCause: Int = 0
    var entityType: Int = 0

    var frame: Long = 0 // tick

    override fun encode(byteBuf: HandleByteBuf) {
        byteBuf.writeActorRuntimeID(this.eid)
        byteBuf.writeVector3f(this.x, this.y, this.z)
        byteBuf.writeFloatLE(this.pitch)
        byteBuf.writeFloatLE(this.yaw)
        byteBuf.writeFloatLE(this.headYaw)
        byteBuf.writeByte(mode.toByte().toInt())
        byteBuf.writeBoolean(this.onGround)
        byteBuf.writeActorRuntimeID(this.ridingEid)
        if (this.mode == MODE_TELEPORT) {
            byteBuf.writeIntLE(this.teleportationCause)
            byteBuf.writeIntLE(this.entityType)
        }
        byteBuf.writeUnsignedVarLong(this.frame)
    }

    override fun pid(): Int {
        return ProtocolInfo.MOVE_PLAYER_PACKET
    }

    companion object : PacketDecoder<MovePlayerPacket> {
        override fun decode(byteBuf: HandleByteBuf): MovePlayerPacket {
            val packet = MovePlayerPacket()

            packet.eid = byteBuf.readActorRuntimeID()
            val v = byteBuf.readVector3f()
            packet.x = v.x
            packet.y = v.y
            packet.z = v.z
            packet.pitch = byteBuf.readFloatLE()
            packet.yaw = byteBuf.readFloatLE()
            packet.headYaw = byteBuf.readFloatLE()
            packet.mode = byteBuf.readByte().toInt()
            packet.onGround = byteBuf.readBoolean()
            packet.ridingEid = byteBuf.readActorRuntimeID()
            if (packet.mode == MODE_TELEPORT) {
                packet.teleportationCause = byteBuf.readIntLE()
                packet.entityType = byteBuf.readIntLE()
            }
            packet.frame = byteBuf.readUnsignedVarLong()

            return packet
        }

        const val MODE_NORMAL: Int = 0
        const val MODE_RESET: Int = 1 //MODE_RESPAWN
        const val MODE_TELEPORT: Int = 2
        const val MODE_PITCH: Int = 3 //facepalm Mojang MODE_HEAD_ROTATION
    }
}
