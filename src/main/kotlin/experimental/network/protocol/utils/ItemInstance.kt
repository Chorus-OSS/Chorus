package org.chorus_oss.chorus.experimental.network.protocol.utils

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemDurable
import org.chorus_oss.chorus.nbt.NBTIO.read
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.nbt.TagSerialization
import org.chorus_oss.protocol.types.item.ItemInstance
import java.nio.ByteOrder

operator fun ItemInstance.Companion.invoke(value: Item): ItemInstance {
    return ItemInstance(
        netID = when {
            value.isNothing -> 0
            else -> value.runtimeId
        },
        metadata = value.meta.toUInt(),
        blockRuntimeID = value.blockState?.blockStateHash() ?: 0,
        count = value.count.toUShort(),
        nbtData = when {
            value is ItemDurable && value.meta != 0 -> {
                val nbt = value.compoundTag
                val tag = if (nbt.isEmpty()) {
                    CompoundTag()
                } else {
                    read(nbt, ByteOrder.LITTLE_ENDIAN)
                }
                if (tag.contains("Damage")) {
                    tag.put("__DamageConflict__", tag.removeAndGet("Damage")!!)
                }
                tag.putInt("Damage", value.meta)
                org.chorus_oss.nbt.tags.CompoundTag.invoke(tag)
            }

            value.hasCompoundTag() -> {
                val tag = value.namedTag!!
                org.chorus_oss.nbt.tags.CompoundTag.invoke(tag)
            }

            else -> org.chorus_oss.nbt.tags.CompoundTag()
        },
        canBePlacedOn = HandleByteBuf.extractStringList(value, "CanPlaceOn"),
        canBreak = HandleByteBuf.extractStringList(value, "CanDestroy"),
    )
}

operator fun Item.Companion.invoke(from: ItemInstance): Item {
    return if (from.netID == 0) Item.AIR else {
        Item.get(
            Registries.ITEM_RUNTIMEID.getIdentifier(from.netID),
            from.metadata.toInt(),
            from.count.toInt(),
            Buffer().apply {
                org.chorus_oss.nbt.Tag.serialize(from.nbtData, this, TagSerialization.LE, isRoot = true)
            }.readByteArray()
        )
    }
}