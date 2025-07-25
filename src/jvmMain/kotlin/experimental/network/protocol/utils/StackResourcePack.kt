package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.resourcepacks.ResourcePack
import org.chorus_oss.protocol.types.StackResourcePack

operator fun StackResourcePack.Companion.invoke(from: ResourcePack): StackResourcePack {
    return StackResourcePack(
        uuid = from.packId.toString(),
        version = from.packVersion,
        subPackName = ""
    )
}