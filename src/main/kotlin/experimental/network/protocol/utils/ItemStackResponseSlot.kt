package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.protocol.types.itemstack.response.ItemStackResponseSlot

operator fun ItemStackResponseSlot.Companion.invoke(from: org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseSlot): ItemStackResponseSlot {
    return ItemStackResponseSlot(
        slot = from.slot.toByte(),
        hotbarSlot = from.hotbarSlot.toByte(),
        count = from.count.toByte(),
        stackNetworkID = from.stackNetworkId,
        customName = from.customName,
        filteredCustomName = from.filteredCustomName,
        durabilityCorrection = from.durabilityCorrection,
    )
}