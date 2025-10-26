package org.chorus_oss.chorus.experimental.network.connection.encryption

import dev.whyoleg.cryptography.algorithms.AES

class DuplexCipher(val key: AES.CTR.Key) {
    private val enc = MonoCipher(key)
    private val dec = MonoCipher(key)

    fun encrypt(data: ByteArray): ByteArray = enc.encrypt(data)

    fun decrypt(data: ByteArray): ByteArray = dec.decrypt(data)
}