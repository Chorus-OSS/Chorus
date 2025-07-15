package org.chorus_oss.chorus.experimental.network.connection

import com.appstractive.jwt.UnsignedJWT
import com.appstractive.jwt.jwt
import com.appstractive.jwt.sign
import com.appstractive.jwt.signatures.es384
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.*
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlinx.io.bytestring.decodeToByteString
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object EncryptionUtils {
    @Suppress("SpellCheckingInspection")
    val mojangPublicKey: ECDSA.PublicKey =
        parseKey("MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAECRXueJeTDqNRRgJi/vlRufByu/2G0i2Ebt6YMar5QX/R0DIIyrJMcUpruK4QveTfJSTp3Shlq4Gk34cD/4GUWwkv0DVuzeuB+tXija7HBxii03NHDbPAD0AKnLr2wdAp")

    val ecdsa = CryptographyProvider.Default.get(ECDSA)
    val aes = CryptographyProvider.Default.get(AES.CTR)
    val digest = CryptographyProvider.Default.get(SHA256)
    val ecdh = CryptographyProvider.Default.get(ECDH)

    val curve = EC.Curve.P384
    val publicFormat = EC.PublicKey.Format.DER
    val privateFormat = EC.PrivateKey.Format.DER

    val keyGenerator = ecdsa.keyPairGenerator(curve)
    val publicKeyDecoder = ecdsa.publicKeyDecoder(curve)

    val ecdhPrivateKeyDecoder = ecdh.privateKeyDecoder(curve)
    val ecdhPublicKeyDecoder = ecdh.publicKeyDecoder(curve)

    @OptIn(ExperimentalEncodingApi::class)
    fun parseKey(b64: String): ECDSA.PublicKey {
        val byteString = Base64.decodeToByteString(b64)
        return publicKeyDecoder.decodeFromByteStringBlocking(publicFormat, byteString)
    }

    fun generateKeyPair(): ECDSA.KeyPair {
        return keyGenerator.generateKeyBlocking()
    }

    fun generateRandomToken(): ByteArray {
        return CryptographyRandom.nextBytes(16)
    }

    fun getSecretKey(
        localPrivateKey: ECDSA.PrivateKey,
        remotePublicKey: ECDSA.PublicKey,
        token: ByteArray
    ): AES.CTR.Key {
        val localECDHPrivateKey = ecdhPrivateKeyDecoder.decodeFromByteArrayBlocking(
            privateFormat,
            localPrivateKey.encodeToByteArrayBlocking(privateFormat)
        )
        val remoteECDHPublicKey = ecdhPublicKeyDecoder.decodeFromByteArrayBlocking(
            publicFormat,
            remotePublicKey.encodeToByteArrayBlocking(publicFormat)
        )

        val shared =
            localECDHPrivateKey.sharedSecretGenerator().generateSharedSecretToByteArrayBlocking(remoteECDHPublicKey)

        val hasher = digest.hasher()
        val hash = hasher.hashBlocking(token + shared)
        return aes.keyDecoder().decodeFromByteArrayBlocking(AES.Key.Format.RAW, hash)
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun createHandshakeJWT(serverKeyPair: ECDSA.KeyPair, token: ByteArray): String {
        val jwt: UnsignedJWT = jwt {
            claims {
                claim("salt", Base64.encode(token))
            }
        }

        val signed = jwt.sign {
            es384 { der(serverKeyPair.privateKey.encodeToByteArrayBlocking(privateFormat), curve) }
        }

        return signed.signedData
    }
}