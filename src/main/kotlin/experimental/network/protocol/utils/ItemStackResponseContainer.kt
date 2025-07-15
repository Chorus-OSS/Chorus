package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.protocol.types.itemstack.response.ItemStackResponseContainer
import org.chorus_oss.protocol.types.itemstack.response.ItemStackResponseSlot

operator fun ItemStackResponseContainer.Companion.invoke(from: org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseContainer): ItemStackResponseContainer {
    return ItemStackResponseContainer(
        container = from.containerName,
        slots = from.items.map(ItemStackResponseSlot::invoke)
    )
}