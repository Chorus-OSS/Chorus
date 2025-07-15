package org.chorus_oss.chorus.network.connection.netty

import io.netty.buffer.ByteBuf
import io.netty.util.AbstractReferenceCounted
import io.netty.util.ReferenceCountUtil
import io.netty.util.ReferenceCounted
import io.netty.util.internal.ObjectPool
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.protocol.types.CompressionAlgorithm
import java.util.function.Consumer

class BedrockBatchWrapper private constructor(private val handle: ObjectPool.Handle<BedrockBatchWrapper>) :
    AbstractReferenceCounted() {

    var compressed: ByteBuf? = null
        private set

    var uncompressed: ByteBuf? = null
        private set

    fun setCompressed(compressed: ByteBuf?) {
        if (this.compressed != null) {
            this.compressed!!.release()
        }

        this.compressed = compressed
        if (compressed == null) {
            this.algorithm = null
        }
    }

    fun setUncompressed(value: ByteBuf?) {
        if (this.uncompressed != null) {
            this.uncompressed!!.release()
        }
        this.uncompressed = value
    }

    var algorithm: CompressionAlgorithm? = null

    private val packets: MutableList<BedrockPacketWrapper?> = mutableListOf()

    var modified = false

    override fun deallocate() {
        packets.forEach(Consumer { msg: BedrockPacketWrapper? -> ReferenceCountUtil.safeRelease(msg) })
        ReferenceCountUtil.safeRelease(this.uncompressed)
        ReferenceCountUtil.safeRelease(this.compressed)
        this.compressed = null
        this.uncompressed = null
        packets.clear()
        this.modified = false
        this.algorithm = null
        handle.recycle(this)
    }

    fun addPacket(wrapper: BedrockPacketWrapper?) {
        packets.add(wrapper)
        this.modify()
    }

    fun modify() {
        this.modified = true
    }

    fun setCompressed(compressed: ByteBuf?, algorithm: CompressionAlgorithm?) {
        if (this.compressed != null) {
            this.compressed!!.release()
        }

        this.compressed = compressed
        this.algorithm = algorithm
    }

    override fun touch(o: Any): ReferenceCounted {
        return this
    }

    override fun retain(): BedrockBatchWrapper {
        return super.retain() as BedrockBatchWrapper
    }

    override fun retain(increment: Int): BedrockBatchWrapper {
        return super.retain(increment) as BedrockBatchWrapper
    }

    companion object {
        private val RECYCLER: ObjectPool<BedrockBatchWrapper> =
            ObjectPool.newPool { handle: ObjectPool.Handle<BedrockBatchWrapper> ->
                BedrockBatchWrapper(
                    handle
                )
            }

        @JvmOverloads
        fun newInstance(compressed: ByteBuf? = null, uncompressed: ByteBuf? = null): BedrockBatchWrapper {
            val batch = RECYCLER.get()
            batch.compressed = compressed
            batch.uncompressed = uncompressed
            batch.setRefCnt(1)

            check(!(batch.packets.isNotEmpty())) { "Batch was not deallocated" }
            return batch
        }

        fun create(subClientId: Int, vararg packets: DataPacket?): BedrockBatchWrapper {
            val batch = newInstance()
            for (packet in packets) {
                batch.packets.add(BedrockPacketWrapper(0, subClientId, 0, packet, null))
            }
            return batch
        }
    }
}
