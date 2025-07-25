package org.chorus_oss.chorus.network.connection.netty.codec.batch

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import org.chorus_oss.chorus.network.connection.netty.BedrockBatchWrapper
import org.chorus_oss.chorus.utils.ByteBufVarInt

@Sharable
class BedrockBatchDecoder : MessageToMessageDecoder<BedrockBatchWrapper>() {
    override fun decode(ctx: ChannelHandlerContext, msg: BedrockBatchWrapper, out: MutableList<Any>) {
        checkNotNull(msg.uncompressed) { "Batch packet was not decompressed" }

        val buffer = msg.uncompressed!!.slice()
        while (buffer.isReadable) {
            val packetLength = ByteBufVarInt.readUnsignedInt(buffer)
            val packetBuf = buffer.readRetainedSlice(packetLength)
            out.add(packetBuf)
        }
    }

    companion object {
        const val NAME: String = "bedrock-batch-decoder"
    }
}
