package org.chorus_oss.chorus.network.connection.util

import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwx.HeaderParameterNames
import org.jose4j.lang.JoseException
import java.security.*
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {
    /**
     * Mojang's public key used to verify the JWT during login.
     *
     * @return Mojang's public EC key
     */
    var mojangPublicKey: ECPublicKey? = null
    var oldMojangPublicKey: ECPublicKey? = null

    private val SECURE_RANDOM = SecureRandom()
    private const val MOJANG_PUBLIC_KEY_BASE64 =
        "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAECRXueJeTDqNRRgJi/vlRufByu/2G0i2Ebt6YMar5QX/R0DIIyrJMcUpruK4QveTfJSTp3Shlq4Gk34cD/4GUWwkv0DVuzeuB+tXija7HBxii03NHDbPAD0AKnLr2wdAp"
    private const val OLD_MOJANG_PUBLIC_KEY_BASE64 =
        "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE8ELkixyLcwlZryUQcu1TvPOmI2B7vX83ndnWRUaXm74wFfa5f/lwQNTfrLVHa2PmenpGI6JhIMUJaWZrjmMj90NoKNFSNBuKdm8rYiXsfaz3K36x/1U26HpG0ZxK/V1V"
    private const val ALGORITHM_TYPE: String = AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384

    private var KEY_PAIR_GEN: KeyPairGenerator = KeyPairGenerator.getInstance("EC")
    private val ALGORITHM_CONSTRAINTS = AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, ALGORITHM_TYPE)

    init {
        // DO NOT REMOVE THIS
        // Since Java 8u231, secp384r1 is deprecated and will throw an exception.
        val namedGroups = System.getProperty("jdk.tls.namedGroups")
        System.setProperty(
            "jdk.tls.namedGroups",
            if (namedGroups == null || namedGroups.isEmpty()) "secp384r1" else ", secp384r1"
        )

        try {
            KEY_PAIR_GEN.initialize(ECGenParameterSpec("secp384r1"))
            mojangPublicKey = parseKey(MOJANG_PUBLIC_KEY_BASE64)
            oldMojangPublicKey = parseKey(OLD_MOJANG_PUBLIC_KEY_BASE64)
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError("Unable to initialize required encryption", e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw AssertionError("Unable to initialize required encryption", e)
        } catch (e: InvalidKeySpecException) {
            throw AssertionError("Unable to initialize required encryption", e)
        }
    }

    /**
     * Generate EC public key from base 64 encoded string
     *
     * @param b64 base 64 encoded key
     * @return key generated
     * @throws NoSuchAlgorithmException runtime does not support the EC key spec
     * @throws InvalidKeySpecException  input does not conform with EC key spec
     */
    @JvmStatic
    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun parseKey(b64: String?): ECPublicKey {
        return KeyFactory.getInstance("EC")
            .generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(b64))) as ECPublicKey
    }

    /**
     * Create EC key pair to be used for handshake and encryption
     *
     * @return EC KeyPair
     */
    @JvmStatic
    fun createKeyPair(): KeyPair {
        return KEY_PAIR_GEN.generateKeyPair()
    }

    /**
     * Generate the secret key used to encrypt the connection
     *
     * @param localPrivateKey local private key
     * @param remotePublicKey remote public key
     * @param token           token generated or received from the server
     * @return secret key used to encrypt connection
     * @throws InvalidKeyException keys provided are not EC spec
     */
    @JvmStatic
    @Throws(InvalidKeyException::class)
    fun getSecretKey(localPrivateKey: PrivateKey, remotePublicKey: PublicKey, token: ByteArray?): SecretKey {
        val sharedSecret = getEcdhSecret(localPrivateKey, remotePublicKey)

        val digest: MessageDigest
        try {
            digest = MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError(e)
        }

        digest.update(token)
        digest.update(sharedSecret)
        val secretKeyBytes = digest.digest()
        return SecretKeySpec(secretKeyBytes, "AES")
    }

    @Throws(InvalidKeyException::class)
    private fun getEcdhSecret(localPrivateKey: PrivateKey, remotePublicKey: PublicKey): ByteArray {
        val agreement: KeyAgreement
        try {
            agreement = KeyAgreement.getInstance("ECDH")
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError(e)
        }

        agreement.init(localPrivateKey)
        agreement.doPhase(remotePublicKey, true)
        return agreement.generateSecret()
    }

    /**
     * Create handshake JWS used in the [org.chorus_oss.protocol.packets.ServerToClientHandshakePacket]
     * which completes the encryption handshake.
     *
     * @param serverKeyPair used to sign the JWT
     * @param token         salt for the encryption handshake
     * @return signed JWS object
     * @throws JoseException invalid key pair provided
     */
    @JvmStatic
    @Throws(JoseException::class)
    fun createHandshakeJwt(serverKeyPair: KeyPair, token: ByteArray?): String {
        val signature = JsonWebSignature()
        signature.algorithmHeaderValue = ALGORITHM_TYPE
        signature.setHeader(
            HeaderParameterNames.X509_URL,
            Base64.getEncoder().encodeToString(serverKeyPair.public.encoded)
        )
        signature.key = serverKeyPair.private

        val claims = JwtClaims()
        claims.setClaim("salt", Base64.getEncoder().encodeToString(token))
        signature.payload = claims.toJson()

        return signature.compactSerialization
    }

    /**
     * Generate 16 bytes of random data for the handshake token using a [SecureRandom]
     *
     * @return 16 byte token
     */
    @JvmStatic
    fun generateRandomToken(): ByteArray {
        val token = ByteArray(16)
        SECURE_RANDOM.nextBytes(token)
        return token
    }

    fun createCipher(encrypt: Boolean, key: SecretKey): Cipher {
        try {
            val iv = ByteArray(16)
            val transformation = "AES/CTR/NoPadding"

            System.arraycopy(key.encoded, 0, iv, 0, 12)
            iv[15] = 2

            val cipher = Cipher.getInstance(transformation)
            cipher.init(if (encrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            return cipher
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError("Unable to initialize required encryption", e)
        } catch (e: NoSuchPaddingException) {
            throw AssertionError("Unable to initialize required encryption", e)
        } catch (e: InvalidKeyException) {
            throw AssertionError("Unable to initialize required encryption", e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw AssertionError("Unable to initialize required encryption", e)
        }
    }
}
