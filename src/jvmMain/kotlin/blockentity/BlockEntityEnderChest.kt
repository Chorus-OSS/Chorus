package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityEnderChest(level: Level, nbt: CompoundTag) : BlockEntitySpawnable(level, nbt), BlockEntityNameable {
    init {
        isMovable = true
    }

    override val isBlockEntityValid: Boolean
        get() = this.block.id == BlockID.ENDER_CHEST

    override val spawnCompound: CompoundTag
        get() {
            val spawnCompound = super.spawnCompound
                .putBoolean("isMovable", this.isMovable)
            if (this.hasName()) {
                spawnCompound.put("CustomName", namedTag["CustomName"]!!)
            }
            return spawnCompound
        }

    override var name: String
        get() = if (this.hasName()) namedTag.getString("CustomName") else "EnderChest"
        set(name) {
            if (name.isBlank()) {
                namedTag.remove("CustomName")
                return
            }

            namedTag.putString("CustomName", name)
        }

    override fun hasName(): Boolean {
        return namedTag.contains("CustomName")
    }
}
