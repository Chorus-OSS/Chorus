package org.chorus_oss.chorus.network.protocol

import io.netty.handler.codec.EncoderException
import org.chorus_oss.chorus.nbt.NBTIO.writeValue
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import java.io.IOException
import java.nio.ByteOrder


class LevelEventGenericPacket : DataPacket() {
    var eventId: Int = 0
    var tag: CompoundTag? = null

    override fun encode(byteBuf: HandleByteBuf) {
        byteBuf.writeVarInt(eventId)
        try {
            byteBuf.writeBytes(writeValue(tag!!, ByteOrder.LITTLE_ENDIAN, true))
        } catch (e: IOException) {
            throw EncoderException(e)
        }
    }

    override fun pid(): Int {
        return ProtocolInfo.LEVEL_EVENT_GENERIC_PACKET
    }
}
