package network.encryption

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import org.chorus_oss.chorus.experimental.network.connection.encryption.DuplexCipher
import org.chorus_oss.chorus.experimental.network.connection.encryption.MonoCipher
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals

class DuplexCipherTest {
    @Test
    fun round() {
        val key = CryptographyProvider.Default.get(AES.CTR).keyGenerator().generateKeyBlocking()

        val cipher1 = DuplexCipher(key)
        val cipher2 = DuplexCipher(key)

        val data = "Hello, World!".toByteArray()

        val encrypted = cipher1.encrypt(data)
        val decrypted = cipher2.decrypt(encrypted)

        assertContentEquals(data, decrypted)

        val data2 = Random.nextBytes(7)

        val encrypted2 = cipher1.encrypt(data2)
        val decrypted2 = cipher2.decrypt(encrypted2)

        assertContentEquals(data2, decrypted2)

        val data3 = Random.nextBytes(15)

        val encrypted3 = cipher2.encrypt(data3)
        val decrypted3 = cipher1.decrypt(encrypted3)

        assertContentEquals(data3, decrypted3)
    }
}