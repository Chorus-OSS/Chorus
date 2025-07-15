package org.chorus_oss.chorus.network.protocol

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import java.awt.Color
import java.util.*


class PlayerListPacket : DataPacket() {
    @JvmField
    var type: Byte = 0

    @JvmField
    var entries: Array<Entry> = emptyArray()

    override fun encode(byteBuf: HandleByteBuf) {
        byteBuf.writeByte(type.toInt())
        byteBuf.writeUnsignedVarInt(entries.size)

        if (this.type == TYPE_ADD) {
            for (entry in this.entries) {
                byteBuf.writeUUID(entry.uuid)

                byteBuf.writeVarLong(entry.entityId)
                byteBuf.writeString(entry.name)
                byteBuf.writeString(entry.xboxUserId)
                byteBuf.writeString(entry.platformChatId)
                byteBuf.writeIntLE(entry.buildPlatform)
                byteBuf.writeSkin(entry.skin!!)
                byteBuf.writeBoolean(entry.isTeacher)
                byteBuf.writeBoolean(entry.isHost)
                byteBuf.writeBoolean(entry.subClient)
                byteBuf.writeIntLE(entry.color.rgb)
            }

            for (entry in this.entries) {
                byteBuf.writeBoolean(
                    entry.trustedSkin || Server.instance.settings.playerSettings.forceSkinTrusted
                )
            }
        } else {
            for (entry in this.entries) {
                byteBuf.writeUUID(entry.uuid)
            }
        }
    }


    class Entry {
        val uuid: UUID

        var entityId: Long = 0
        var name: String = ""
        var xboxUserId: String = "" // TODO
        var platformChatId: String = "" // TODO
        var buildPlatform: Int = -1
        var skin: Skin? = null
        var isTeacher: Boolean = false
        var isHost: Boolean = false
        var subClient: Boolean = false
        var trustedSkin: Boolean = false
        var color: Color = Color.WHITE

        constructor(uuid: UUID) {
            this.uuid = uuid
        }

        @JvmOverloads
        constructor(uuid: UUID, entityId: Long, name: String, skin: Skin, xboxUserId: String? = "") {
            this.uuid = uuid
            this.entityId = entityId
            this.name = name
            this.skin = skin
            this.trustedSkin = skin.isTrusted()
            this.xboxUserId = xboxUserId ?: ""
        }
    }

    override fun pid(): Int {
        return ProtocolInfo.PLAYER_LIST_PACKET
    }

    companion object {
        const val TYPE_ADD: Byte = 0
        const val TYPE_REMOVE: Byte = 1
    }
}
