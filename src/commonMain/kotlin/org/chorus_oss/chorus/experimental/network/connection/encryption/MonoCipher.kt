package org.chorus_oss.chorus.experimental.network.connection.encryption

import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

const val blockSize = 16

@OptIn(ExperimentalAtomicApi::class, DelicateCryptographyApi::class)
class MonoCipher(val key: AES.CTR.Key) {
    private val cipher = key.cipher()

    private val ivHigh: AtomicLong
    private val ivLow: AtomicLong

    private val offset: AtomicInt = AtomicInt(0)

    init {
        val baseIv = Buffer().apply { write(EncryptionUtils.createIv(key)) }

        this.ivHigh = AtomicLong(baseIv.readLong())
        this.ivLow = AtomicLong(baseIv.readLong())
    }

    fun encrypt(data: ByteArray): ByteArray {
        val iv = buildIv()
        val padding = offset.load()
        val padded = ByteArray(padding) + data
        val encrypted = cipher.encryptWithIvBlocking(iv, padded)
        val result = encrypted.copyOfRange(padding, encrypted.size)

        updateIv(data.size)
        return result
    }

    fun decrypt(data: ByteArray): ByteArray {
        val iv = buildIv()
        val padding = offset.load()
        val padded = ByteArray(padding) + data
        val decrypted = cipher.decryptWithIvBlocking(iv, padded)
        val result = decrypted.copyOfRange(padding, decrypted.size)

        updateIv(data.size)
        return result
    }

    fun buildIv(): ByteArray {
        val buffer = Buffer()
        buffer.writeLong(ivHigh.load())
        buffer.writeLong(ivLow.load())
        return buffer.readByteArray()
    }

    private fun updateIv(processed: Int) {
        val total = offset.load() + processed

        val used = total / blockSize
        offset.store(total % blockSize)

        val low = ivLow.load()
        val newLow = low + used
        ivLow.store(newLow)
        if (newLow < low) {
            ivHigh.incrementAndFetch()
        }
    }
}