package org.chorus_oss.chorus.network.connection.netty

import io.netty.buffer.ByteBuf
import io.netty.util.AbstractReferenceCounted
import io.netty.util.ReferenceCountUtil
import org.chorus_oss.chorus.network.DataPacket

class BedrockPacketWrapper(
    var packetId: Int,
    var senderSubClientId: Int,
    var targetSubClientId: Int,
    var packet: DataPacket?,
    var packetBuffer: ByteBuf?
) : AbstractReferenceCounted() {
    var headerLength = 0

    override fun deallocate() {
        ReferenceCountUtil.safeRelease(this.packetBuffer)
        this.packet = null
    }

    override fun touch(hint: Any): BedrockPacketWrapper {
        ReferenceCountUtil.touch(this.packetBuffer)
        return this
    }

    override fun retain(): BedrockPacketWrapper {
        return super.retain() as BedrockPacketWrapper
    }
}
