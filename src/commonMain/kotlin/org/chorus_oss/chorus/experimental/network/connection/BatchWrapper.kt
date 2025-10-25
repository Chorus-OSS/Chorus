package org.chorus_oss.chorus.experimental.network.connection

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteString
import kotlinx.io.write
import org.chorus_oss.protocol.core.ProtoCodec
import org.chorus_oss.protocol.core.ProtoVAR
import org.chorus_oss.protocol.core.types.UInt

data class BatchWrapper(
    val packets: List<ByteString>
) {
    companion object : ProtoCodec<BatchWrapper> {
        override fun serialize(value: BatchWrapper, stream: Sink) {
            value.packets.forEach {
                ProtoVAR.UInt.serialize(it.size.toUInt(), stream)
                stream.write(it)
            }
        }

        override fun deserialize(stream: Source): BatchWrapper {
            return BatchWrapper(
                packets = mutableListOf<ByteString>().also {
                    while (!stream.exhausted()) {
                        val size = ProtoVAR.UInt.deserialize(stream)
                        it.add(stream.readByteString(size.toInt()))
                    }
                }
            )
        }
    }
}
