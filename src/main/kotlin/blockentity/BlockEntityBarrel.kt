package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.inventory.BarrelInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityBarrel(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnableContainer(chunk, nbt) {
    override var inventory: Inventory = BarrelInventory(this)

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound
            .putBoolean("isMovable", this.isMovable)
            .putBoolean("Findable", false)

    override val isBlockEntityValid: Boolean
        get() = block.id === BlockID.BARREL

    override var name: String
        get() = if (this.hasName()) namedTag.getString("CustomName") else "Barrel"
        set(name) {
            if (name.isNullOrEmpty()) {
                namedTag.remove("CustomName")
                return
            }

            namedTag.putString("CustomName", name)
        }

    override fun hasName(): Boolean {
        return namedTag.contains("CustomName")
    }

    init {
        isMovable = true
        loadNBT()
    }
}
