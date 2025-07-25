package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityMusic(chunk: IChunk, nbt: CompoundTag) : BlockEntity(chunk, nbt) {
    override fun loadNBT() {
        super.loadNBT()
        if (!namedTag.contains("note")) {
            namedTag.putByte("note", 0)
        }
        if (!namedTag.contains("powered")) {
            namedTag.putBoolean("powered", false)
        }
    }

    override val isBlockEntityValid: Boolean
        get() = this.block.id === BlockID.NOTEBLOCK

    fun changePitch() {
        namedTag.putByte("note", (namedTag.getByte("note") + 1) % 25)
    }

    val pitch: Int
        get() = namedTag.getByte("note").toInt()

    var isPowered: Boolean
        get() = namedTag.getBoolean("powered")
        set(powered) {
            namedTag.putBoolean("powered", powered)
        }
}
