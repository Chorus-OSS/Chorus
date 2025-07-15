package org.chorus_oss.chorus.network

import org.chorus_oss.chorus.network.connection.util.HandleByteBuf

abstract class DataPacket {
    open fun decode(byteBuf: HandleByteBuf) {}

    open fun encode(byteBuf: HandleByteBuf) {}

    abstract fun pid(): Int

    open fun handle(handler: PacketHandler) {}
}

interface PacketDecoder<T : DataPacket> {
    fun decode(byteBuf: HandleByteBuf): T
}

interface PacketEncoder {
    fun encode(byteBuf: HandleByteBuf)
}
