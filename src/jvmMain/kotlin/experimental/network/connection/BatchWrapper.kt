package org.chorus_oss.chorus.experimental.network.connection

import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import org.chorus_oss.protocol.core.ProtoCodec
import org.chorus_oss.protocol.core.ProtoVAR
import org.chorus_oss.protocol.core.types.UInt

data class BatchWrapper(
    val packets: MutableList<PacketWrapper>
) {
    companion object : ProtoCodec<BatchWrapper> {
        override fun serialize(value: BatchWrapper, stream: Sink) {
            value.packets.forEach {
                val buffer = Buffer().also { buf ->
                    PacketWrapper.serialize(it, buf)
                }
                ProtoVAR.UInt.serialize(buffer.size.toUInt(), stream)
                stream.write(buffer, buffer.size)
            }
        }

        override fun deserialize(stream: Source): BatchWrapper {
            return BatchWrapper(
                packets = mutableListOf<PacketWrapper>().also {
                    while (!stream.exhausted()) {
                        val size = ProtoVAR.UInt.deserialize(stream)
                        val buffer = Buffer().also { buf ->
                            stream.readTo(buf, size.toLong())
                        }
                        val packetWrapper = PacketWrapper.deserialize(buffer)
                        it.add(packetWrapper)
                    }
                }
            )
        }
    }
}
