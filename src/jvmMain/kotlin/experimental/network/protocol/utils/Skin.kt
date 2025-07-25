package org.chorus_oss.chorus.experimental.network.protocol.utils

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.bytestring.encodeToByteString
import org.chorus_oss.chorus.utils.PersonaPieceTint
import org.chorus_oss.chorus.utils.SerializedImage
import org.chorus_oss.protocol.types.skin.PersonaPiece
import org.chorus_oss.protocol.types.skin.PersonaPieceTintColor
import org.chorus_oss.protocol.types.skin.Skin
import org.chorus_oss.protocol.types.skin.SkinAnimation

operator fun Skin.Companion.invoke(from: org.chorus_oss.chorus.entity.data.Skin): Skin {
    val skinData = from.getSkinData()
    val capeData = from.getCapeData()

    return Skin(
        skinID = from.getSkinId(),
        playFabID = from.getPlayFabId(),
        skinResourcePatch = from.getSkinResourcePatch(),
        skinImageWidth = skinData.width.toUInt(),
        skinImageHeight = skinData.height.toUInt(),
        skinData = ByteString(skinData.data),
        animations = from.getAnimations().map {
            SkinAnimation(
                imageWidth = it.image.width.toUInt(),
                imageHeight = it.image.height.toUInt(),
                imageData = ByteString(it.image.data),
                animationType = SkinAnimation.Companion.AnimationType.entries[it.type],
                frameCount = it.frames,
                expressionType = SkinAnimation.Companion.ExpressionType.entries[it.expression]
            )
        },
        capeImageWidth = capeData.width.toUInt(),
        capeImageHeight = capeData.height.toUInt(),
        capeData = ByteString(capeData.data),
        skinGeometry = from.getGeometryData(),
        geometryDataMinEngineVersion = from.getGeometryDataEngineVersion()!!,
        animationData = from.getAnimationData().encodeToByteString(),
        capeID = from.getCapeId(),
        fullID = from.getFullSkinId(),
        armSize = from.getArmSize()!!,
        skinColor = from.getSkinColor()!!,
        personaPieces = from.getPersonaPieces().map {
            PersonaPiece(
                pieceID = it.id,
                pieceType = it.type,
                packID = it.packId,
                default = it.isDefault,
                productID = it.productId,
            )
        },
        personaPieceTintColors = from.getTintColors().map {
            PersonaPieceTintColor(
                pieceType = it.pieceType,
                colors = it.colors.toList()
            )
        },
        premiumSkin = from.isPremium(),
        personaSkin = from.isPersona(),
        personaCapeOnClassicSkin = from.isCapeOnClassic(),
        primaryUser = from.isPrimaryUser(),
        overrideAppearance = from.isOverridingPlayerAppearance(),
    )
}

operator fun org.chorus_oss.chorus.entity.data.Skin.Companion.invoke(from: Skin): org.chorus_oss.chorus.entity.data.Skin {
    return org.chorus_oss.chorus.entity.data.Skin().apply {
        setSkinId(from.skinID)
        setPlayFabId(from.playFabID)
        setSkinResourcePatch(from.skinResourcePatch)
        setSkinData(
            SerializedImage(
                from.skinImageWidth.toInt(),
                from.skinImageHeight.toInt(),
                from.skinData.toByteArray()
            )
        )
        getAnimations().addAll(
            from.animations.map {
                org.chorus_oss.chorus.utils.SkinAnimation(
                    SerializedImage(
                        it.imageHeight.toInt(),
                        it.imageHeight.toInt(),
                        it.imageData.toByteArray(),
                    ),
                    it.animationType.ordinal,
                    it.frameCount,
                    it.expressionType.ordinal,
                )
            }
        )
        setCapeData(
            SerializedImage(
                from.capeImageWidth.toInt(),
                from.capeImageHeight.toInt(),
                from.capeData.toByteArray(),
            )
        )
        setGeometryData(from.skinGeometry)
        setGeometryDataEngineVersion(from.geometryDataMinEngineVersion)
        setAnimationData(from.animationData.decodeToString())
        setCapeId(from.capeID)
        setFullSkinId(from.fullID)
        setArmSize(from.armSize)
        setSkinColor(from.skinColor)
        getPersonaPieces().addAll(
            from.personaPieces.map {
                org.chorus_oss.chorus.utils.PersonaPiece(
                    it.pieceID,
                    it.pieceType,
                    it.packID,
                    it.default,
                    it.productID,
                )
            }
        )
        getTintColors().addAll(
            from.personaPieceTintColors.map {
                PersonaPieceTint(
                    it.pieceType,
                    it.colors,
                )
            }
        )
        setPremium(from.premiumSkin)
        setPersona(from.personaSkin)
        setCapeOnClassic(from.personaCapeOnClassicSkin)
        setPrimaryUser(from.primaryUser)
        setOverridingPlayerAppearance(from.overrideAppearance)
    }
}