package org.chorus_oss.chorus.network.connection.netty.codec.encryption

import dev.whyoleg.cryptography.algorithms.AES
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import io.netty.util.concurrent.FastThreadLocal
import org.chorus_oss.chorus.network.connection.netty.BedrockBatchWrapper
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicLong

class BedrockEncryptionEncoder(val key: AES.CTR.Key) :
    MessageToMessageEncoder<BedrockBatchWrapper>() {
    private val packetCounter = AtomicLong()

    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, `in`: BedrockBatchWrapper, out: MutableList<Any>) {
        val inBuffer = `in`.compressed ?: return
        val trailer = generateTrailer(inBuffer, key, this.packetCounter)

        val inBytes = ByteArray(inBuffer.readableBytes()).also { inBuffer.readBytes(it) }

        val encrypted = key.cipher().encryptBlocking(inBytes + trailer)

        val outBuf = ctx.alloc().ioBuffer(encrypted.size).writeBytes(encrypted)
        `in`.setCompressed(outBuf.retain())
        out.add(`in`.retain())
    }

    companion object {
        const val NAME: String = "bedrock-encryption-encoder"
        private val DIGEST: FastThreadLocal<MessageDigest> = object : FastThreadLocal<MessageDigest>() {
            override fun initialValue(): MessageDigest {
                try {
                    return MessageDigest.getInstance("SHA-256")
                } catch (e: Exception) {
                    throw AssertionError(e)
                }
            }
        }

        fun generateTrailer(buf: ByteBuf, key: AES.CTR.Key, counter: AtomicLong): ByteArray {
            val digest = DIGEST.get()
            val counterBuf = ByteBufAllocator.DEFAULT.directBuffer(8)
            try {
                counterBuf.writeLongLE(counter.getAndIncrement())
                val keyBuffer = ByteBuffer.wrap(key.encodeToByteArrayBlocking(AES.Key.Format.RAW))

                digest.update(counterBuf.nioBuffer(0, 8))
                digest.update(buf.nioBuffer(buf.readerIndex(), buf.readableBytes()))
                digest.update(keyBuffer)
                val hash = digest.digest()
                return hash.copyOf(8)
            } finally {
                counterBuf.release()
                digest.reset()
            }
        }
    }
}
