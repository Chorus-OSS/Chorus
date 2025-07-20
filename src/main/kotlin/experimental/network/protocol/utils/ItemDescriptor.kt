package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.recipe.descriptor.ComplexAliasDescriptor
import org.chorus_oss.chorus.recipe.descriptor.DefaultDescriptor
import org.chorus_oss.chorus.recipe.descriptor.DeferredDescriptor
import org.chorus_oss.chorus.recipe.descriptor.InvalidDescriptor
import org.chorus_oss.chorus.recipe.descriptor.ItemTagDescriptor
import org.chorus_oss.chorus.recipe.descriptor.MolangDescriptor
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.protocol.types.item.desciptor.ComplexAliasItemDescriptor
import org.chorus_oss.protocol.types.item.desciptor.DefaultItemDescriptor
import org.chorus_oss.protocol.types.item.desciptor.DeferredItemDescriptor
import org.chorus_oss.protocol.types.item.desciptor.InvalidItemDescriptor
import org.chorus_oss.protocol.types.item.desciptor.ItemDescriptor
import org.chorus_oss.protocol.types.item.desciptor.ItemTagItemDescriptor
import org.chorus_oss.protocol.types.item.desciptor.MoLangItemDescriptor

operator fun ItemDescriptor.Companion.invoke(from: org.chorus_oss.chorus.recipe.descriptor.ItemDescriptor): ItemDescriptor {
    return when (from) {
        is DefaultDescriptor -> {
            val item = from.toItem()
            when (item.isNothing) {
                true -> DefaultItemDescriptor(
                    networkID = 0,
                    metadataValue = 0
                )
                false -> DefaultItemDescriptor(
                    networkID = item.runtimeId.toShort(),
                    metadataValue = (if (item.hasMeta()) item.damage else 0x7fff).toShort()
                )
            }
        }
        is MolangDescriptor -> MoLangItemDescriptor(
            expression = from.tagExpression,
            version = from.molangVersion.toByte(),
        )
        is ComplexAliasDescriptor -> ComplexAliasItemDescriptor(
            name = from.name,
        )
        is ItemTagDescriptor -> ItemTagItemDescriptor(
            tag = from.itemTag
        )
        is DeferredDescriptor -> DeferredItemDescriptor(
            name = from.fullName,
            metadataValue = from.auxValue.toShort()
        )
        is InvalidDescriptor -> InvalidItemDescriptor()

        else -> throw IllegalArgumentException("Unknown ItemDescriptor: $from")
    }
}

fun ItemDescriptor.toItem(): Item {
    return when (this) {
        is DefaultItemDescriptor -> if (this.networkID == 0.toShort()) Item.AIR else get(
            Registries.ITEM_RUNTIMEID.getIdentifier(this.networkID.toInt()),
            (this.metadataValue ?: 0).toInt()
        )

        else -> throw UnsupportedOperationException()
    }
}