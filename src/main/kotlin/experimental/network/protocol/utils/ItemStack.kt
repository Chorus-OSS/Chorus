package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.protocol.types.item.ItemInstance
import org.chorus_oss.protocol.types.item.ItemStack

operator fun ItemStack.Companion.invoke(value: Item): ItemStack {
    return ItemStack(
        stackNetID = value.getNetId(),
        item = ItemInstance.invoke(value)
    )
}