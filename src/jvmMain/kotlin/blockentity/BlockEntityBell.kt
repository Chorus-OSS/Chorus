package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.ByteTag
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.IntTag

class BlockEntityBell(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    private var ringing = false

    @JvmField
    var direction: Int = 0

    @JvmField
    var ticks: Int = 0

    @JvmField
    val spawnExceptions: MutableList<Player> = ArrayList(2)


    override fun initBlockEntity() {
        super.initBlockEntity()
        scheduleUpdate()
    }

    override fun loadNBT() {
        super.loadNBT()
        ringing = if (!namedTag.contains("Ringing") || namedTag["Ringing"] !is ByteTag) {
            false
        } else {
            namedTag.getBoolean("Ringing")
        }

        direction = if (!namedTag.contains("Direction") || namedTag["Direction"] !is IntTag) {
            255
        } else {
            namedTag.getInt("Direction")
        }

        ticks = if (!namedTag.contains("Ticks") || namedTag["Ticks"] !is IntTag) {
            0
        } else {
            namedTag.getInt("Ticks")
        }
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putBoolean("Ringing", ringing)
        namedTag.putInt("Direction", direction)
        namedTag.putInt("Ticks", ticks)
    }

    override fun onUpdate(): Boolean {
        if (ringing) {
            if (ticks == 0) {
                level.addSound(this.position, Sound.BLOCK_BELL_HIT)
                spawnToAllWithExceptions()
                spawnExceptions.clear()
            } else if (ticks >= 50) {
                ringing = false
                ticks = 0
                spawnToAllWithExceptions()
                spawnExceptions.clear()
                return false
            }
            //spawnToAll();
            ticks++
            return true
        } else if (ticks > 0) {
            ticks = 0
            spawnToAllWithExceptions()
            spawnExceptions.clear()
        }

        return false
    }

    private fun spawnToAllWithExceptions() {
        if (this.closed) {
            return
        }

        for (player in level.getChunkPlayers(
            chunk.x,
            chunk.z
        ).values) {
            if (player.spawned && !spawnExceptions.contains(player)) {
                this.spawnTo(player)
            }
        }
    }

    fun isRinging(): Boolean {
        return ringing
    }

    fun setRinging(ringing: Boolean) {
        if (this.ringing != ringing) {
            this.ringing = ringing
            scheduleUpdate()
        }
    }

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound
            .putBoolean("isMovable", this.isMovable)
            .putBoolean("Ringing", this.ringing)
            .putInt("Direction", this.direction)
            .putInt("Ticks", this.ticks)

    override val isBlockEntityValid: Boolean
        get() = block.id === BlockID.BELL
}
