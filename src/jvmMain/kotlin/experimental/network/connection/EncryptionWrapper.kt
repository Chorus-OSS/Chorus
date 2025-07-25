package org.chorus_oss.chorus.experimental.network.connection

import dev.whyoleg.cryptography.algorithms.AES
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import org.chorus_oss.protocol.core.ProtoCodec

data class EncryptionWrapper(
    val raw: Buffer,
    val key: AES.CTR.Key,
) {
    companion object : ProtoCodec<EncryptionWrapper> {
        override fun serialize(value: EncryptionWrapper, stream: Sink) {
            TODO("Not yet implemented")
        }

        override fun deserialize(stream: Source): EncryptionWrapper {
            TODO("Not yet implemented")
        }
    }
}
