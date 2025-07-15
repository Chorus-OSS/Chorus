package org.chorus_oss.chorus.network.protocol

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.PacketDecoder
import org.chorus_oss.chorus.network.PacketHandler
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import org.chorus_oss.chorus.utils.*
import java.nio.charset.StandardCharsets
import java.util.*

class LoginPacket(
    var protocol: Int,
) : DataPacket() {
    var username: String? = null
    var titleId: String? = null

    var clientUUID: UUID? = null
    var clientId: Long = 0
    var skin: Skin? = null
    var issueUnixTime: Long = -1

    var buffer: BinaryStream? = null
        private set

    private fun decodeChainData(binaryStream: BinaryStream) {
        val chainString = String(
            binaryStream[binaryStream.lInt], StandardCharsets.UTF_8
        )

        val jwt = JsonParser.parseString(chainString).asJsonObject
        val certificateString = jwt["Certificate"].asString
        val certificate = JsonParser.parseString(certificateString).asJsonObject
        val chain = certificate["chain"].asJsonArray

        for (c in chain) {
            val chainMap = decodeJWT(c.asString) ?: continue
            if (chainMap.has("extraData")) {
                if (chainMap.has("iat")) {
                    this.issueUnixTime = chainMap["iat"].asLong * 1000
                }
                val extra = chainMap["extraData"].asJsonObject
                if (extra.has("displayName")) this.username = extra["displayName"].asString
                if (extra.has("identity")) this.clientUUID = UUID.fromString(extra["identity"].asString)
                if (extra.has("titleId")) this.titleId = extra["titleId"].asString
            }
        }
    }

    private fun decodeSkinData(binaryStream: BinaryStream) {
        val skinToken = decodeJWT(String(binaryStream[binaryStream.lInt]))
        if (skinToken!!.has("ClientRandomId")) this.clientId = skinToken["ClientRandomId"].asLong

        skin = Skin()

        if (skinToken.has("PlayFabId")) {
            skin!!.setPlayFabId(skinToken["PlayFabId"].asString)
        }

        if (skinToken.has("CapeId")) {
            skin!!.setCapeId(skinToken["CapeId"].asString)
        }

        if (skinToken.has("SkinId")) {
            // The "SkinId" obtained here is FullId
            // FullId = SkinId + CapeId
            // The skinId in the Skin object is not FullId, we need to subtract the CapeId

            val fullSkinId = skinToken["SkinId"].asString
            skin!!.setFullSkinId(fullSkinId)
            if (skin!!.getCapeId() != null) skin!!.setSkinId(
                fullSkinId.substring(
                    0,
                    fullSkinId.length - skin!!.getCapeId().length
                )
            )
            else skin!!.setSkinId(fullSkinId)
        }

        skin!!.setSkinData(getImage(skinToken, "Skin"))
        skin!!.setCapeData(getImage(skinToken, "Cape"))

        if (skinToken.has("PremiumSkin")) {
            skin!!.setPremium(skinToken["PremiumSkin"].asBoolean)
        }

        if (skinToken.has("PersonaSkin")) {
            skin!!.setPersona(skinToken["PersonaSkin"].asBoolean)
        }

        if (skinToken.has("CapeOnClassicSkin")) {
            skin!!.setCapeOnClassic(skinToken["CapeOnClassicSkin"].asBoolean)
        }

        if (skinToken.has("SkinResourcePatch")) {
            skin!!.setSkinResourcePatch(
                String(
                    Base64.getDecoder().decode(skinToken["SkinResourcePatch"].asString),
                    StandardCharsets.UTF_8
                )
            )
        }

        if (skinToken.has("SkinGeometryData")) {
            skin!!.setGeometryData(
                String(
                    Base64.getDecoder().decode(skinToken["SkinGeometryData"].asString),
                    StandardCharsets.UTF_8
                )
            )
        }

        if (skinToken.has("SkinAnimationData")) {
            skin!!.setAnimationData(
                String(
                    Base64.getDecoder().decode(skinToken["SkinAnimationData"].asString),
                    StandardCharsets.UTF_8
                )
            )
        }

        if (skinToken.has("AnimatedImageData")) {
            for (element in skinToken["AnimatedImageData"].asJsonArray) {
                skin!!.getAnimations().add(getAnimation(element.asJsonObject))
            }
        }

        if (skinToken.has("SkinColor")) {
            skin!!.setSkinColor(skinToken["SkinColor"].asString)
        }

        if (skinToken.has("ArmSize")) {
            skin!!.setArmSize(skinToken["ArmSize"].asString)
        }

        if (skinToken.has("PersonaPieces")) {
            for (`object` in skinToken["PersonaPieces"].asJsonArray) {
                skin!!.getPersonaPieces().add(getPersonaPiece(`object`.asJsonObject))
            }
        }

        if (skinToken.has("PieceTintColors")) {
            for (`object` in skinToken["PieceTintColors"].asJsonArray) {
                skin!!.getTintColors().add(getTint(`object`.asJsonObject))
            }
        }
    }

    private fun decodeJWT(token: String): JsonObject? {
        val base = token.split(".").dropLastWhile { it.isEmpty() }.toTypedArray()
        if (base.size < 2) return null
        return Gson().fromJson(
            String(
                Base64.getDecoder().decode(
                    base[1]
                ), StandardCharsets.UTF_8
            ),
            JsonObject::class.java
        )
    }

    override fun pid(): Int {
        return ProtocolInfo.LOGIN_PACKET
    }

    override fun handle(handler: PacketHandler) {
        handler.handle(this)
    }

    companion object : PacketDecoder<LoginPacket> {
        override fun decode(byteBuf: HandleByteBuf): LoginPacket {
            val packet = LoginPacket(
                protocol = byteBuf.readInt()
            )

            packet.buffer = BinaryStream(byteBuf.readByteArray(), 0)
            packet.decodeChainData(packet.buffer!!)
            packet.decodeSkinData(packet.buffer!!)

            return packet
        }

        private fun getAnimation(element: JsonObject): SkinAnimation {
            val frames = element["Frames"].asFloat
            val type = element["Type"].asInt
            val data = Base64.getDecoder().decode(element["Image"].asString)
            val width = element["ImageWidth"].asInt
            val height = element["ImageHeight"].asInt
            val expression = element["AnimationExpression"].asInt
            return SkinAnimation(SerializedImage(width, height, data), type, frames, expression)
        }

        private fun getImage(token: JsonObject, name: String): SerializedImage {
            if (token.has(name + "Data")) {
                val skinImage = Base64.getDecoder().decode(token[name + "Data"].asString)
                if (token.has(name + "ImageHeight") && token.has(name + "ImageWidth")) {
                    val width = token[name + "ImageWidth"].asInt
                    val height = token[name + "ImageHeight"].asInt
                    return SerializedImage(width, height, skinImage)
                } else {
                    return SerializedImage.fromLegacy(skinImage)
                }
            }
            return SerializedImage.EMPTY
        }

        private fun getPersonaPiece(`object`: JsonObject): PersonaPiece {
            val pieceId = `object`["PieceId"].asString
            val pieceType = `object`["PieceType"].asString
            val packId = `object`["PackId"].asString
            val isDefault = `object`["IsDefault"].asBoolean
            val productId = `object`["ProductId"].asString
            return PersonaPiece(pieceId, pieceType, packId, isDefault, productId)
        }

        fun getTint(`object`: JsonObject): PersonaPieceTint {
            val pieceType = `object`["PieceType"].asString
            val colors: MutableList<String> = ArrayList()
            for (element in `object`["Colors"].asJsonArray) {
                colors.add(element.asString) // remove #
            }
            return PersonaPieceTint(pieceType, colors)
        }
    }
}
