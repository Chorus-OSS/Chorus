package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityHangingSign(level: Level, nbt: CompoundTag) : BlockEntitySign(level, nbt) {
    override var name: String
        get() = if (this.hasName()) namedTag.getString("CustomName") else "Hanging Sign"
        set(name) {
            super.name = name
        }

    fun hasName(): Boolean {
        return namedTag.contains("CustomName")
    }

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound.putString("id", BlockEntityID.HANGING_SIGN)
}
