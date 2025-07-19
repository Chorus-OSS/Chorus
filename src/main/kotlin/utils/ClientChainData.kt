package org.chorus_oss.chorus.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.readIntLe
import kotlinx.io.readString
import kotlinx.io.write
import kotlinx.io.writeString
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.network.connection.util.EncryptionUtils.mojangPublicKey
import org.chorus_oss.chorus.network.connection.util.EncryptionUtils.oldMojangPublicKey
import org.jose4j.jws.JsonWebSignature
import org.jose4j.lang.JoseException
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.interfaces.ECPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.*

/**
 * ClientChainData is a container of chain data sent from clients.
 *
 *
 * Device information such as client UUID, xuid and serverAddress, can be
 * read from instances of this object.
 *
 *
 * To get chain data, you can use player.getLoginChainData() or read(loginPacket)
 */
class ClientChainData private constructor(private val stream: Source) : LoginChainData {
    override var xuid: String? = null
        get() {
            return if (this.isWaterdog) {
                waterdogXUID
            } else {
                field
            }
        }
        private set

    override var isXboxAuthed: Boolean = false
        private set

    private val isWaterdog: Boolean
        get() {
            if (waterdogXUID == null) {
                return false
            }

            return Server.instance.settings.baseSettings.waterdogpe
        }

    override var username: String? = null
        private set

    override var clientUUID: UUID? = null
        private set

    var titleId: String? = null
        private set

    override var identityPublicKey: String? = null
        private set

    override var clientId: Long = 0
        private set

    override var serverAddress: String? = null
        private set

    override var deviceModel: String? = null
        private set

    override var deviceOS: Int = 0
        private set

    override var deviceId: String? = null
        private set

    override var gameVersion: String? = null
        private set

    override var guiScale: Int = 0
        private set

    override var languageCode: String? = null
        private set

    override var currentInputMode: Int = 0
        private set

    override var defaultInputMode: Int = 0
        private set

    override var waterdogIP: String? = null
        private set

    override var waterdogXUID: String? = null
        private set

    override var maxViewDistance: Int = 0
        private set

    override var memoryTier: Int = 0
        private set

    override var uIProfile: Int = 0
        private set

    override var capeData: String? = null
        private set

    override var rawData: JsonObject? = null
        private set

    init {
        decodeChainData()
        decodeSkinData()
    }

    private fun decodeSkinData() {
        val skinToken = decodeToken(
            Buffer().apply {
                stream.readAtMostTo(this, stream.readIntLe().toLong())
            }.readString()
        ) ?: return
        if (skinToken.has("ClientRandomId")) this.clientId = skinToken["ClientRandomId"].asLong
        if (skinToken.has("ServerAddress")) this.serverAddress = skinToken["ServerAddress"].asString
        if (skinToken.has("DeviceModel")) this.deviceModel = skinToken["DeviceModel"].asString
        if (skinToken.has("DeviceOS")) this.deviceOS = skinToken["DeviceOS"].asInt
        if (skinToken.has("DeviceId")) this.deviceId = skinToken["DeviceId"].asString
        if (skinToken.has("GameVersion")) this.gameVersion = skinToken["GameVersion"].asString
        if (skinToken.has("GuiScale")) this.guiScale = skinToken["GuiScale"].asInt
        if (skinToken.has("LanguageCode")) this.languageCode = skinToken["LanguageCode"].asString
        if (skinToken.has("CurrentInputMode")) this.currentInputMode = skinToken["CurrentInputMode"].asInt
        if (skinToken.has("DefaultInputMode")) this.defaultInputMode = skinToken["DefaultInputMode"].asInt
        if (skinToken.has("UIProfile")) this.uIProfile = skinToken["UIProfile"].asInt
        if (skinToken.has("CapeData")) this.capeData = skinToken["CapeData"].asString
        if (skinToken.has("Waterdog_IP")) this.waterdogIP = skinToken["Waterdog_IP"].asString
        if (skinToken.has("Waterdog_XUID")) this.waterdogXUID = skinToken["Waterdog_XUID"].asString
        if (skinToken.has("MaxViewDistance")) this.maxViewDistance = skinToken["MaxViewDistance"].asInt
        if (skinToken.has("MemoryTier")) this.memoryTier = skinToken["MemoryTier"].asInt

        if (this.isWaterdog) {
            isXboxAuthed = true
        }

        this.rawData = skinToken
    }

    private fun decodeToken(token: String): JsonObject? {
        val base = token.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (base.size < 2) return null
        val json = String(Base64.getDecoder().decode(base[1]), StandardCharsets.UTF_8)
        //Server.instance.getLogger().debug(json);
        return JSONUtils.from(json, JsonObject::class.java)
    }

    private fun decodeChainData() {
        val chainString = Buffer().apply {
            stream.readAtMostTo(this, stream.readIntLe().toLong())
        }.readString()

        val jwt = JsonParser.parseString(chainString).asJsonObject
        val certificateString = jwt["Certificate"].asString
        val certificate = JsonParser.parseString(certificateString).asJsonObject
        val chain = certificate["chain"].asJsonArray.map { it.asString }

        this.isXboxAuthed = try {
            verifyChain(chain)
        } catch (_: Exception) {
            false
        }

        for (c in chain) {
            val chainMap = decodeToken(c) ?: continue
            if (chainMap.has("extraData")) {
                val extra = chainMap["extraData"].asJsonObject
                if (extra.has("displayName")) this.username = extra["displayName"].asString
                if (extra.has("identity")) this.clientUUID = UUID.fromString(extra["identity"].asString)
                if (extra.has("XUID")) this.xuid = extra["XUID"].asString
                if (extra.has("titleId")) this.titleId = extra["titleId"].asString
            }
            if (chainMap.has("identityPublicKey")) this.identityPublicKey = chainMap["identityPublicKey"].asString
        }

        if (!isXboxAuthed) {
            this.xuid = null
        }
    }

    @Throws(Exception::class)
    private fun verifyChain(chains: List<String>): Boolean {
        var lastKey: ECPublicKey? = null
        var mojangKeyVerified = false
        val iterator = chains.iterator()
        val epoch = Instant.now().epochSecond
        while (iterator.hasNext()) {
            val jws = JsonWebSignature.fromCompactSerialization(iterator.next()) as JsonWebSignature
            val x5us = jws.getHeader("x5u") ?: return false
            val expectedKey = generateKey(x5us)
            // First key is self-signed
            if (lastKey == null) {
                lastKey = expectedKey
            } else if (lastKey != expectedKey) {
                return false
            }

            if (!verify(lastKey, jws)) {
                return false
            }

            if (mojangKeyVerified) {
                return !iterator.hasNext()
            }

            if (lastKey == mojangPublicKey || lastKey == oldMojangPublicKey) {
                mojangKeyVerified = true
            }

            val payload = JSONUtils.from(jws.payload, object : TypeToken<Map<String, Any>>() {})

            // chain expiry check
            val chainExpiresObj = payload["exp"]
            val chainExpires: Long
            if (chainExpiresObj is Number) {
                chainExpires = chainExpiresObj.toLong()
            } else {
                throw RuntimeException("Unsupported expiry time format")
            }
            if (chainExpires < epoch) {
                // chain has already expires
                return false
            }

            val base64key =
                payload["identityPublicKey"] as? String ?: throw RuntimeException("No key found")
            lastKey = generateKey(base64key)
        }
        return mojangKeyVerified
    }

    private fun verify(key: ECPublicKey?, jws: JsonWebSignature?): Boolean {
        try {
            if (key == null || jws == null) {
                return false
            }
            jws.key = key
            return jws.verifySignature()
        } catch (e: JoseException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        fun read(pk: org.chorus_oss.protocol.packets.LoginPacket): ClientChainData {
            return ClientChainData(
                Buffer().apply {
                    write(pk.connectionRequest)
                }
            )
        }

        const val UI_PROFILE_CLASSIC: Int = 0
        const val UI_PROFILE_POCKET: Int = 1

        @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
        private fun generateKey(base64: String): ECPublicKey {
            return KeyFactory.getInstance("EC")
                .generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(base64))) as ECPublicKey
        }
    }
}
