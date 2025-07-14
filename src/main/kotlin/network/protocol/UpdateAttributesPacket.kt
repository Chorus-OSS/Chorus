package org.chorus_oss.chorus.network.protocol

import org.chorus_oss.chorus.entity.Attribute
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf


class UpdateAttributesPacket : DataPacket() {
    var entries: Array<Attribute> = emptyArray()

    @JvmField
    var entityId: Long = 0
    var frame: Long = 0 //tick

    override fun encode(byteBuf: HandleByteBuf) {
        byteBuf.writeActorRuntimeID(this.entityId)

        byteBuf.writeUnsignedVarInt(entries.size)
        for (entry in entries) {
            byteBuf.writeFloatLE(entry.minValue)
            byteBuf.writeFloatLE(entry.maxValue)
            byteBuf.writeFloatLE(entry.getValue())
            byteBuf.writeFloatLE(entry.defaultMinimum)
            byteBuf.writeFloatLE(entry.defaultMaximum)
            byteBuf.writeFloatLE(entry.defaultValue)
            byteBuf.writeString(entry.name)
            byteBuf.writeUnsignedVarInt(0) // Modifiers
        }
        byteBuf.writeUnsignedVarInt(frame.toInt())
    }

    override fun pid(): Int {
        return ProtocolInfo.UPDATE_ATTRIBUTES_PACKET
    }

    override fun toString(): String {
        return "UpdateAttributesPacket(entries=${entries.contentToString()}, entityId=$entityId, frame=$frame)"
    }
}
