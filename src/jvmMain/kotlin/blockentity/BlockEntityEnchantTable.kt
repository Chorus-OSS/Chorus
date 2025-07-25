package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.inventory.EnchantInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag


class BlockEntityEnchantTable(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt),
    BlockEntityInventoryHolder {
    override val isBlockEntityValid: Boolean
        get() = block.id == BlockID.ENCHANTING_TABLE

    override var name: String
        get() = if (this.hasName()) namedTag.getString("CustomName") else "Enchanting Table"
        set(name) {
            if (name == null || name.isBlank()) {
                namedTag.remove("CustomName")
                return
            }

            namedTag.putString("CustomName", name)
        }

    override fun hasName(): Boolean {
        return namedTag.contains("CustomName")
    }

    override val spawnCompound: CompoundTag
        get() {
            val c = super.spawnCompound
                .putBoolean("isMovable", false)

            if (this.hasName()) {
                c.put("CustomName", namedTag["CustomName"]!!)
            }

            return c
        }

    override val inventory: Inventory
        get() = EnchantInventory(this)
}
