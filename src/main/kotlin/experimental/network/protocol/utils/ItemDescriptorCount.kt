package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.protocol.types.item.desciptor.ItemDescriptor
import org.chorus_oss.protocol.types.item.desciptor.ItemDescriptorCount

operator fun ItemDescriptorCount.Companion.invoke(from: org.chorus_oss.chorus.recipe.descriptor.ItemDescriptor): ItemDescriptorCount {
    return ItemDescriptorCount(
        descriptor = ItemDescriptor(from),
        count = from.count
    )
}