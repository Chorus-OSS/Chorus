package org.chorus_oss.chorus.network.protocol

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.PacketDecoder
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import java.util.*


class PlayerSkinPacket : DataPacket() {
    lateinit var uuid: UUID
    lateinit var skin: Skin
    lateinit var newSkinName: String
    lateinit var oldSkinName: String

    override fun encode(byteBuf: HandleByteBuf) {
        byteBuf.writeUUID(uuid)
        byteBuf.writeSkin(skin)
        byteBuf.writeString(newSkinName)
        byteBuf.writeString(oldSkinName)
        byteBuf.writeBoolean(skin.isTrusted() || Server.instance.settings.playerSettings.forceSkinTrusted)
    }

    override fun pid(): Int {
        return ProtocolInfo.PLAYER_SKIN_PACKET
    }

    companion object : PacketDecoder<PlayerSkinPacket> {
        override fun decode(byteBuf: HandleByteBuf): PlayerSkinPacket {
            val packet = PlayerSkinPacket()

            packet.uuid = byteBuf.readUUID()
            packet.skin = byteBuf.readSkin()
            packet.newSkinName = byteBuf.readString()
            packet.oldSkinName = byteBuf.readString()
            if (byteBuf.isReadable) { // -facepalm-
                packet.skin.setTrusted(byteBuf.readBoolean())
            }

            return packet
        }
    }
}
