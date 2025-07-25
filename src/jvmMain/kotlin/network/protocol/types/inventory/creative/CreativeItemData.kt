package org.chorus_oss.chorus.network.protocol.types.inventory.creative

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.registry.Registries

data class CreativeItemData(
    val item: Item,
    val groupId: Int,
) {
    val netID: Int
        get() = Registries.CREATIVE.getCreativeItemIndex(item)

    override fun toString(): String {
        return "CreativeItemData(item=$item, groupId=$groupId, netID=$netID)"
    }
}
