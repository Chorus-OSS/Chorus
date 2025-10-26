package org.chorus_oss.chorus.experimental.network.connection.encryption

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.*
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlinx.io.Buffer
import kotlinx.io.bytestring.decodeToByteString
import kotlinx.io.bytestring.encode
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.chorus_oss.protocol.core.ProtoLE
import org.chorus_oss.protocol.core.types.Long
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object EncryptionUtils {
    @Suppress("SpellCheckingInspection")
    val mojangPublicKey: ECDSA.PublicKey by lazy {
        parseKey("MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAECRXueJeTDqNRRgJi/vlRufByu/2G0i2Ebt6YMar5QX/R0DIIyrJMcUpruK4QveTfJSTp3Shlq4Gk34cD/4GUWwkv0DVuzeuB+tXija7HBxii03NHDbPAD0AKnLr2wdAp")
    }

    val ecdsa by lazy { CryptographyProvider.Default.get(ECDSA.Companion) }
    val aes by lazy { CryptographyProvider.Default.get(AES.CTR) }
    val digest by lazy { CryptographyProvider.Default.get(SHA256) }
    val ecdh by lazy { CryptographyProvider.Default.get(ECDH.Companion) }

    val curve = EC.Curve.P384
    val publicFormat = EC.PublicKey.Format.DER
    val privateFormat = EC.PrivateKey.Format.DER

    val keyGenerator by lazy { ecdsa.keyPairGenerator(curve) }
    val publicKeyDecoder by lazy { ecdsa.publicKeyDecoder(curve) }

    val ecdhPrivateKeyDecoder by lazy { ecdh.privateKeyDecoder(curve) }
    val ecdhPublicKeyDecoder by lazy { ecdh.publicKeyDecoder(curve) }

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

    @Serializable
    data class JWTHeader(
        val alg: String,
        val x5u: String,
    )

    @Serializable
    data class JWTClaims(
        val salt: String,
    )

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun createHandshakeJWT(serverKeyPair: ECDSA.KeyPair, token: ByteArray): String {
        val header = JWTHeader(
            alg = "ES384",
            x5u = Base64.encode(
                serverKeyPair.publicKey.encodeToByteString(EC.PublicKey.Format.DER)
            )
        )

        val claims = JWTClaims(
            salt = Base64.encode(token)
        )

        val headerJSON = Json.encodeToString(header)
        val claimsJSON = Json.encodeToString(claims)

        val headerB64 = Base64.UrlSafe.encode(headerJSON.encodeToByteString())
        val claimsB64 = Base64.UrlSafe.encode(claimsJSON.encodeToByteString())

        val unsigned = "$headerB64.$claimsB64"

        val signature = serverKeyPair.privateKey.signatureGenerator(
            digest = SHA384,
            format = ECDSA.SignatureFormat.DER
        ).generateSignature(unsigned.encodeToByteString())

        val signatureB64 = Base64.UrlSafe.encode(signature)

        return "$unsigned.$signatureB64"
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun createTrailer(data: ByteArray, key: AES.CTR.Key, counter: AtomicLong): ByteArray {
        val hasher = digest.hasher()

        val counterBytes = Buffer().also { ProtoLE.Long.serialize(counter.fetchAndIncrement(), it) }.readByteArray()
        val keyBytes = key.encodeToByteArrayBlocking(AES.Key.Format.RAW)

        return hasher.hashBlocking(counterBytes + data + keyBytes).copyOf(8)
    }

    fun createIv(key: AES.CTR.Key): ByteArray {
        val keyBytes = key.encodeToByteArrayBlocking(AES.Key.Format.RAW)

        return ByteArray(16).apply {
            keyBytes.copyInto(this, endIndex = 12)
            this[15] = 2
        }
    }
}