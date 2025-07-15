package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.protocol.types.item.desciptor.DefaultItemDescriptor
import org.chorus_oss.protocol.types.item.desciptor.ItemDescriptor

fun ItemDescriptor.toItem(): Item {
    return when (this) {
        is DefaultItemDescriptor -> if (this.networkID == 0.toShort()) Item.AIR else get(
            Registries.ITEM_RUNTIMEID.getIdentifier(this.networkID.toInt()),
            (this.metadataValue ?: 0).toInt()
        )

        else -> throw UnsupportedOperationException()
    }
}