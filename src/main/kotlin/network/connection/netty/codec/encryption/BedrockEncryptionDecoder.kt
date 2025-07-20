package org.chorus_oss.chorus.network.connection.netty.codec.encryption

import dev.whyoleg.cryptography.algorithms.AES
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.CorruptedFrameException
import io.netty.handler.codec.MessageToMessageDecoder
import org.chorus_oss.chorus.network.connection.netty.BedrockBatchWrapper
import java.util.concurrent.atomic.AtomicLong

class BedrockEncryptionDecoder(val key: AES.CTR.Key) :
    MessageToMessageDecoder<BedrockBatchWrapper>() {
    private val packetCounter = AtomicLong()

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, msg: BedrockBatchWrapper, out: MutableList<Any>) {
        val inBuffer = msg.compressed ?: return
        val encrypted = ByteArray(inBuffer.readableBytes()).also { inBuffer.readBytes(it) }

        val decrypted = key.cipher().decryptBlocking(encrypted)

        val payload = decrypted.copyOfRange(0, decrypted.size - 8)

        if (VALIDATE) {
            val trailer = decrypted.copyOfRange(decrypted.size - 8, decrypted.size)

            val expected: ByteArray = BedrockEncryptionEncoder.Companion.generateTrailer(
                ctx.alloc().buffer(payload.size).writeBytes(payload),
                key, this.packetCounter
            )

            if (!expected.contentEquals(trailer)) {
                throw CorruptedFrameException("Invalid encryption trailer")
            }
        }

        val outBuf = ctx.alloc().buffer(payload.size).writeBytes(payload)
        msg.setCompressed(outBuf.retain())
        out.add(msg.retain())
    }

    companion object {
        const val NAME: String = "bedrock-encryption-decoder"
        private val VALIDATE = java.lang.Boolean.getBoolean("cloudburst.validateEncryption")
    }
}
