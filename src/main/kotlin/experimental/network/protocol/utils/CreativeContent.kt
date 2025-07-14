package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.network.protocol.types.inventory.creative.CreativeItemData
import org.chorus_oss.protocol.types.creative.CreativeItem
import org.chorus_oss.protocol.types.item.ItemInstance

operator fun CreativeItem.Companion.invoke(from: CreativeItemData): CreativeItem {
    return CreativeItem(
        creativeItemNetworkID = from.netID.toUInt(),
        item = ItemInstance(from.item),
        groupIndex = from.groupId.toUInt(),
    )
}