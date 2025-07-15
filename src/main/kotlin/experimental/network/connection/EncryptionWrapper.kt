package org.chorus_oss.chorus.experimental.network.connection

import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import org.chorus_oss.protocol.core.ProtoCodec
import javax.crypto.Cipher
import javax.crypto.SecretKey

data class EncryptionWrapper(
    val raw: Buffer,
    val key: SecretKey,
    val cipher: Cipher,
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
