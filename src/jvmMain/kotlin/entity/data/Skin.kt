package org.chorus_oss.chorus.entity.data


import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.chorus_oss.chorus.utils.PersonaPiece
import org.chorus_oss.chorus.utils.PersonaPieceTint
import org.chorus_oss.chorus.utils.SerializedImage
import org.chorus_oss.chorus.utils.SkinAnimation
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*

class Skin {
    private val animations: MutableList<SkinAnimation> = mutableListOf()
    private val personaPieces: MutableList<PersonaPiece> = mutableListOf()
    private val tintColors: MutableList<PersonaPieceTint> = mutableListOf()
    private var skinId: String? = null
    private var fullSkinId: String? = null

    private var playFabId: String? = ""
    private var skinResourcePatch: String = GEOMETRY_CUSTOM
    private var skinData: SerializedImage? = null
    private var capeData: SerializedImage? = null
    private var geometryData: String? = null
    private var animationData: String? = null
    private var premium: Boolean = false
    private var persona: Boolean = false
    private var capeOnClassic: Boolean = false
    private var primaryUser: Boolean = true
    private var capeId: String? = null
    private var skinColor: String? = "#0"
    private var armSize: String? = "wide"
    private var trusted: Boolean = true
    private var geometryDataEngineVersion: String? = "0.0.0"
    private var overridingPlayerAppearance: Boolean = true

    fun isValid(): Boolean {
        return isValidSkin() && isValidResourcePatch()
    }

    private fun isValidSkin(): Boolean {
        return skinId != null && skinId!!.trim { it <= ' ' }
            .isNotEmpty() && skinId!!.length < 100 && skinData != null && skinData!!.width >= 32 && skinData!!.height >= 32 && skinData!!.data.size >= SINGLE_SKIN_SIZE &&
                (playFabId == null || playFabId!!.length < 100) &&
                (capeId == null || capeId!!.length < 100) &&
                (skinColor == null || skinColor!!.length < 100) &&
                (armSize == null || armSize!!.length < 100) &&
                (fullSkinId == null || fullSkinId!!.length < 200) &&
                (geometryDataEngineVersion == null || geometryDataEngineVersion!!.length < 100) &&
                (animationData == null || animationData!!.length < 1000) && animations.size <= 100 && personaPieces.size <= 100 && tintColors.size <= 100
    }

    private fun isValidResourcePatch(): Boolean {
        if (skinResourcePatch.length > 1000) {
            return false
        }
        try {
            val json = Json.parseToJsonElement(skinResourcePatch).jsonObject
            val geometry = json["geometry"]!!.jsonObject
            return geometry.containsKey("default") && geometry["default"]!!.jsonPrimitive.isString
        } catch (_: Throwable) {
            return false
        }
    }

    fun getSkinData(): SerializedImage {
        if (skinData == null) {
            return SerializedImage.EMPTY
        }
        return skinData!!
    }

    fun setSkinData(skinData: ByteArray) {
        setSkinData(SerializedImage.fromLegacy(skinData))
    }

    fun setSkinData(image: BufferedImage) {
        setSkinData(parseBufferedImage(image))
    }

    fun setSkinData(skinData: SerializedImage) {
        this.skinData = skinData
    }

    fun getSkinId(): String {
        if (this.skinId == null) {
            this.generateSkinId("Custom")
        }
        return skinId!!
    }

    fun setSkinId(skinId: String?) {
        if (skinId == null || skinId.trim { it <= ' ' }.isEmpty()) {
            return
        }
        this.skinId = skinId
    }

    fun generateSkinId(name: String) {
        val data: ByteArray = getSkinData().data + getSkinResourcePatch().toByteArray()
        this.skinId = UUID.nameUUIDFromBytes(data).toString() + "." + name
    }

    fun setGeometryName(geometryName: String?) {
        if (geometryName == null || geometryName.trim { it <= ' ' }.isEmpty()) {
            skinResourcePatch = GEOMETRY_CUSTOM
            return
        }

        this.skinResourcePatch = "{\"geometry\" : {\"default\" : \"$geometryName\"}}"
    }

    fun getSkinResourcePatch(): String {
        return this.skinResourcePatch
    }

    fun setSkinResourcePatch(skinResourcePatch: String?) {
        var skinResourcePatch1 = skinResourcePatch
        if (skinResourcePatch1 == null || skinResourcePatch1.trim { it <= ' ' }.isEmpty()) {
            skinResourcePatch1 = GEOMETRY_CUSTOM
        }
        this.skinResourcePatch = skinResourcePatch1
    }

    fun getCapeData(): SerializedImage {
        return capeData ?: SerializedImage.EMPTY
    }

    fun setCapeData(capeData: ByteArray) {
        check(capeData.size == SINGLE_SKIN_SIZE || capeData.isEmpty()) { "Invalid legacy cape" }
        setCapeData(SerializedImage(64, 32, capeData))
    }

    fun setCapeData(image: BufferedImage) {
        setCapeData(parseBufferedImage(image))
    }

    fun setCapeData(capeData: SerializedImage) {
        this.capeData = capeData
    }

    fun getCapeId(): String {
        return capeId ?: ""
    }

    fun setCapeId(capeId: String?) {
        var capeId1 = capeId
        if (capeId1 == null || capeId1.trim { it <= ' ' }.isEmpty()) {
            capeId1 = null
        }
        this.capeId = capeId1
    }

    fun getGeometryData(): String {
        return geometryData ?: ""
    }

    fun setGeometryData(geometryData: String) {
        if (geometryData != this.geometryData) {
            this.geometryData = geometryData
        }
    }

    fun getAnimationData(): String {
        return animationData ?: ""
    }

    fun setAnimationData(animationData: String) {
        if (animationData != this.animationData) {
            this.animationData = animationData
        }
    }

    fun getAnimations(): MutableList<SkinAnimation> {
        return animations
    }

    fun getPersonaPieces(): MutableList<PersonaPiece> {
        return personaPieces
    }

    fun getTintColors(): MutableList<PersonaPieceTint> {
        return tintColors
    }

    fun isPremium(): Boolean {
        return premium
    }

    fun setPremium(premium: Boolean) {
        this.premium = premium
    }

    fun isPersona(): Boolean {
        return persona
    }

    fun setPersona(persona: Boolean) {
        this.persona = persona
    }

    fun isCapeOnClassic(): Boolean {
        return capeOnClassic
    }

    fun setCapeOnClassic(capeOnClassic: Boolean) {
        this.capeOnClassic = capeOnClassic
    }

    fun isPrimaryUser(): Boolean {
        return primaryUser
    }

    fun setPrimaryUser(primaryUser: Boolean) {
        this.primaryUser = primaryUser
    }

    fun getGeometryDataEngineVersion(): String? {
        return geometryDataEngineVersion
    }

    fun setGeometryDataEngineVersion(geometryDataEngineVersion: String?) {
        this.geometryDataEngineVersion = geometryDataEngineVersion
    }

    fun isTrusted(): Boolean {
        return trusted
    }

    fun setTrusted(trusted: Boolean) {
        this.trusted = trusted
    }

    fun getSkinColor(): String? {
        return skinColor
    }

    fun setSkinColor(skinColor: String?) {
        this.skinColor = skinColor
    }

    fun getArmSize(): String? {
        return armSize
    }

    fun setArmSize(armSize: String?) {
        this.armSize = armSize
    }

    fun getFullSkinId(): String {
        if (fullSkinId == null) fullSkinId = skinId + (if (capeId != null) capeId else "")
        return fullSkinId!!
    }

    fun setFullSkinId(fullSkinId: String?) {
        this.fullSkinId = fullSkinId
    }

    fun getPlayFabId(): String {
        if (this.persona && (this.playFabId == null || playFabId!!.isEmpty())) {
            try {
                this.playFabId =
                    skinId!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[5]
            } catch (e: Exception) {
                this.playFabId = getFullSkinId().replace("-", "").substring(16)
            }
        }
        return this.playFabId!!
    }

    fun setPlayFabId(playFabId: String?) {
        this.playFabId = playFabId
    }

    fun isOverridingPlayerAppearance(): Boolean {
        return this.overridingPlayerAppearance
    }

    fun setOverridingPlayerAppearance(override: Boolean) {
        this.overridingPlayerAppearance = override
    }

    companion object {
        val GEOMETRY_CUSTOM: String = convertLegacyGeometryName("geometry.humanoid.custom")
        val GEOMETRY_CUSTOM_SLIM: String = convertLegacyGeometryName("geometry.humanoid.customSlim")
        private const val PIXEL_SIZE: Int = 4

        const val SINGLE_SKIN_SIZE: Int = 32 * 32 * PIXEL_SIZE
        const val SKIN_64_32_SIZE: Int = 64 * 32 * PIXEL_SIZE
        const val DOUBLE_SKIN_SIZE: Int = 64 * 64 * PIXEL_SIZE
        const val SKIN_128_64_SIZE: Int = 128 * 64 * PIXEL_SIZE
        const val SKIN_128_128_SIZE: Int = 128 * 128 * PIXEL_SIZE

        private fun parseBufferedImage(image: BufferedImage): SerializedImage {
            val outputStream = ByteArrayOutputStream()
            for (y in 0..<image.height) {
                for (x in 0..<image.width) {
                    val color: Color = Color(image.getRGB(x, y), true)
                    outputStream.write(color.red)
                    outputStream.write(color.green)
                    outputStream.write(color.blue)
                    outputStream.write(color.alpha)
                }
            }
            image.flush()
            return SerializedImage(image.width, image.height, outputStream.toByteArray())
        }

        private fun convertLegacyGeometryName(geometryName: String): String {
            return "{\"geometry\" : {\"default\" : \"$geometryName\"}}"
        }
    }
}
