package org.chorus_oss.chorus.experimental.network.protocol.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.io.*
import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.utils.PersonaPiece
import org.chorus_oss.chorus.utils.PersonaPieceTint
import org.chorus_oss.chorus.utils.SerializedImage
import org.chorus_oss.chorus.utils.SkinAnimation
import org.chorus_oss.protocol.packets.LoginPacket
import java.nio.charset.StandardCharsets
import java.util.*

fun LoginPacket.decode(): LoginData {
    val data = Buffer().also { buf ->
        buf.write(this.connectionRequest)
    }

    return LoginData(
        chainData = decodeChainData(data),
        skinData = decodeSkinData(data),
    )
}

data class LoginData(
    val chainData: ChainData,
    val skinData: SkinData,
)

data class ChainData(
    val issueUnixTime: Long,
    val username: String,
    val clientUUID: UUID,
    val titleID: String,
)

private fun decodeChainData(stream: Source): ChainData {
    val chainString = stream.readString(stream.readIntLe().toLong())

    val jwt = JsonParser.parseString(chainString).asJsonObject
    val certificateString = jwt["Certificate"].asString
    val certificate = JsonParser.parseString(certificateString).asJsonObject
    val chain = certificate["chain"].asJsonArray

    var issueUnixTime: Long = -1
    var username: String = ""
    var clientUUID: UUID = UUID.randomUUID()
    var titleID: String = ""

    for (c in chain) {
        val chainMap = decodeJWT(c.asString) ?: continue
        if (chainMap.has("extraData")) {
            if (chainMap.has("iat")) {
                issueUnixTime = chainMap["iat"].asLong * 1000
            }
            val extra = chainMap["extraData"].asJsonObject
            if (extra.has("displayName")) username = extra["displayName"].asString
            if (extra.has("identity")) clientUUID = UUID.fromString(extra["identity"].asString)
            if (extra.has("titleId")) titleID = extra["titleId"].asString
        }
    }

    return ChainData(
        issueUnixTime,
        username,
        clientUUID,
        titleID,
    )
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

data class SkinData(
    val clientID: Long,
    val skin: Skin,
)

private fun decodeSkinData(stream: Source): SkinData {
    var clientID: Long = 0

    val skinToken = decodeJWT(stream.readString(stream.readIntLe().toLong()))
    if (skinToken!!.has("ClientRandomId")) clientID = skinToken["ClientRandomId"].asLong

    val skin = Skin()

    if (skinToken.has("PlayFabId")) {
        skin.setPlayFabId(skinToken["PlayFabId"].asString)
    }

    if (skinToken.has("CapeId")) {
        skin.setCapeId(skinToken["CapeId"].asString)
    }

    if (skinToken.has("SkinId")) {
        // The "SkinId" obtained here is FullID.
        // FullID = SkinId + CapeId
        // The skinId in the Skin object is not FullId, we need to subtract the CapeId

        val fullSkinId = skinToken["SkinId"].asString
        skin.setFullSkinId(fullSkinId)
        skin.setSkinId(
            fullSkinId.substring(
                0,
                fullSkinId.length - skin.getCapeId().length
            )
        )
    }

    skin.setSkinData(getImage(skinToken, "Skin"))
    skin.setCapeData(getImage(skinToken, "Cape"))

    if (skinToken.has("PremiumSkin")) {
        skin.setPremium(skinToken["PremiumSkin"].asBoolean)
    }

    if (skinToken.has("PersonaSkin")) {
        skin.setPersona(skinToken["PersonaSkin"].asBoolean)
    }

    if (skinToken.has("CapeOnClassicSkin")) {
        skin.setCapeOnClassic(skinToken["CapeOnClassicSkin"].asBoolean)
    }

    if (skinToken.has("SkinResourcePatch")) {
        skin.setSkinResourcePatch(
            String(
                Base64.getDecoder().decode(skinToken["SkinResourcePatch"].asString),
                StandardCharsets.UTF_8
            )
        )
    }

    if (skinToken.has("SkinGeometryData")) {
        skin.setGeometryData(
            String(
                Base64.getDecoder().decode(skinToken["SkinGeometryData"].asString),
                StandardCharsets.UTF_8
            )
        )
    }

    if (skinToken.has("SkinAnimationData")) {
        skin.setAnimationData(
            String(
                Base64.getDecoder().decode(skinToken["SkinAnimationData"].asString),
                StandardCharsets.UTF_8
            )
        )
    }

    if (skinToken.has("AnimatedImageData")) {
        for (element in skinToken["AnimatedImageData"].asJsonArray) {
            skin.getAnimations().add(getAnimation(element.asJsonObject))
        }
    }

    if (skinToken.has("SkinColor")) {
        skin.setSkinColor(skinToken["SkinColor"].asString)
    }

    if (skinToken.has("ArmSize")) {
        skin.setArmSize(skinToken["ArmSize"].asString)
    }

    if (skinToken.has("PersonaPieces")) {
        for (`object` in skinToken["PersonaPieces"].asJsonArray) {
            skin.getPersonaPieces().add(getPersonaPiece(`object`.asJsonObject))
        }
    }

    if (skinToken.has("PieceTintColors")) {
        for (`object` in skinToken["PieceTintColors"].asJsonArray) {
            skin.getTintColors().add(getTint(`object`.asJsonObject))
        }
    }

    return SkinData(clientID, skin)
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