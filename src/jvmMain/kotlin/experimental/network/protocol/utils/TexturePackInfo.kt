package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.resourcepacks.ResourcePack
import org.chorus_oss.protocol.types.TexturePackInfo
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
operator fun TexturePackInfo.Companion.invoke(from: ResourcePack): TexturePackInfo {
    return TexturePackInfo(
        uuid = Uuid(from.packId),
        version = from.packVersion,
        size = from.packSize.toULong(),
        contentKey = from.encryptionKey,
        subPackName = "",
        contentIdentity = if (!from.encryptionKey.isEmpty()) from.packId.toString() else "",
        hasScripts = false,
        addonPack = false,
        rtxEnabled = true,
        downloadURL = from.cdnUrl()
    )
}