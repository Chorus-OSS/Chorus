package org.chorus_oss.chorus.utils

import dev.whyoleg.cryptography.algorithms.ECDSA
import kotlinx.io.*
import kotlinx.serialization.json.*
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.experimental.network.connection.encryption.EncryptionUtils
import java.util.*
import kotlin.io.encoding.Base64

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
        if (skinToken.contains("ClientRandomId")) this.clientId = skinToken["ClientRandomId"]!!.jsonPrimitive.long
        if (skinToken.contains("ServerAddress")) this.serverAddress = skinToken["ServerAddress"]!!.jsonPrimitive.content
        if (skinToken.contains("DeviceModel")) this.deviceModel = skinToken["DeviceModel"]!!.jsonPrimitive.content
        if (skinToken.contains("DeviceOS")) this.deviceOS = skinToken["DeviceOS"]!!.jsonPrimitive.int
        if (skinToken.contains("DeviceId")) this.deviceId = skinToken["DeviceId"]!!.jsonPrimitive.content
        if (skinToken.contains("GameVersion")) this.gameVersion = skinToken["GameVersion"]!!.jsonPrimitive.content
        if (skinToken.contains("GuiScale")) this.guiScale = skinToken["GuiScale"]!!.jsonPrimitive.int
        if (skinToken.contains("LanguageCode")) this.languageCode = skinToken["LanguageCode"]!!.jsonPrimitive.content
        if (skinToken.contains("CurrentInputMode")) this.currentInputMode =
            skinToken["CurrentInputMode"]!!.jsonPrimitive.int
        if (skinToken.contains("DefaultInputMode")) this.defaultInputMode =
            skinToken["DefaultInputMode"]!!.jsonPrimitive.int
        if (skinToken.contains("UIProfile")) this.uIProfile = skinToken["UIProfile"]!!.jsonPrimitive.int
        if (skinToken.contains("CapeData")) this.capeData = skinToken["CapeData"]!!.jsonPrimitive.content
        if (skinToken.contains("Waterdog_IP")) this.waterdogIP = skinToken["Waterdog_IP"]!!.jsonPrimitive.content
        if (skinToken.contains("Waterdog_XUID")) this.waterdogXUID = skinToken["Waterdog_XUID"]!!.jsonPrimitive.content
        if (skinToken.contains("MaxViewDistance")) this.maxViewDistance =
            skinToken["MaxViewDistance"]!!.jsonPrimitive.int
        if (skinToken.contains("MemoryTier")) this.memoryTier = skinToken["MemoryTier"]!!.jsonPrimitive.int

        if (this.isWaterdog) {
            isXboxAuthed = true
        }

        this.rawData = skinToken
    }

    private fun decodeToken(token: String): JsonObject {
        val base = token.split(".", limit = 3)
        val json = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).decode(base[1]).decodeToString()
        return Json.parseToJsonElement(json).jsonObject
    }

    private fun decodeChainData() {
        val chainString = Buffer().apply {
            stream.readAtMostTo(this, stream.readIntLe().toLong())
        }.readString()

        val jwt = Json.parseToJsonElement(chainString).jsonObject
        val certificateString = jwt["Certificate"]!!.jsonPrimitive.content
        val certificate = Json.parseToJsonElement(certificateString).jsonObject
        val chain = certificate["chain"]!!.jsonArray.map { it.jsonPrimitive.content }

        this.isXboxAuthed = try {
            verifyChain(chain)
        } catch (_: Exception) {
            false
        }

        for (c in chain) {
            val chainMap = decodeToken(c) ?: continue
            if (chainMap.contains("extraData")) {
                val extra = chainMap["extraData"]!!.jsonObject
                if (extra.contains("displayName")) this.username = extra["displayName"]!!.jsonPrimitive.content
                if (extra.contains("identity")) this.clientUUID =
                    UUID.fromString(extra["identity"]!!.jsonPrimitive.content)
                if (extra.contains("XUID")) this.xuid = extra["XUID"]!!.jsonPrimitive.content
                if (extra.contains("titleId")) this.titleId = extra["titleId"]!!.jsonPrimitive.content
            }
            if (chainMap.contains("identityPublicKey")) this.identityPublicKey =
                chainMap["identityPublicKey"]!!.jsonPrimitive.content
        }

        if (!isXboxAuthed) {
            this.xuid = null
        }
    }

    @Throws(Exception::class)
    private fun verifyChain(chains: List<String>): Boolean {
        var lastKey: ECDSA.PublicKey? = null
        var mojangKeyVerified = false
        val iterator = chains.iterator()
        val epoch = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        while (iterator.hasNext()) {
            val jwt = iterator.next()

            val parts = jwt.split(".", limit = 3)
            val header = Json.parseToJsonElement(
                Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).decode(parts[0]).decodeToString()
            ).jsonObject
            val payload = Json.parseToJsonElement(
                Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).decode(parts[1]).decodeToString()
            ).jsonObject

            val x5us = header["x5u"]?.jsonPrimitive?.content ?: return false
            val expectedKey = EncryptionUtils.parseKey(x5us)
            // First key is self-signed
            if (lastKey == null) {
                lastKey = expectedKey
            } else if (lastKey != expectedKey) {
                return false
            }

            if (mojangKeyVerified) {
                return !iterator.hasNext()
            }

            if (lastKey == EncryptionUtils.mojangPublicKey) {
                mojangKeyVerified = true
            }

            // chain expiry check
            val chainExpires: Long = payload["exp"]!!.jsonPrimitive.long
            if (chainExpires < epoch) {
                // chain has already expires
                return false
            }

            val base64key = payload["identityPublicKey"]!!.jsonPrimitive.content
            lastKey = EncryptionUtils.parseKey(base64key)
        }
        return mojangKeyVerified
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
    }
}
