package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemMusicDisc
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.Vector3f
import java.util.*


class BlockEntityJukebox(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    private var recordItem: Item = Item.AIR

    override fun loadNBT() {
        super.loadNBT()
        if (namedTag.contains("RecordItem")) {
            this.recordItem = NBTIO.getItemHelper(namedTag.getCompound("RecordItem"))
        } else {
            this.recordItem = Item.AIR
        }
    }

    override val isBlockEntityValid: Boolean
        get() = level
            .getBlockIdAt(floorX, floorY, floorZ) === BlockID.JUKEBOX

    fun setRecordItem(recordItem: Item) {
        Objects.requireNonNull(recordItem, "Record item cannot be null")
        this.recordItem = recordItem
    }

    fun getRecordItem(): Item {
        return recordItem
    }


    fun play() {
        if (recordItem is ItemMusicDisc) {
            val disc = recordItem as ItemMusicDisc
            val packet = org.chorus_oss.protocol.packets.PlaySoundPacket(
                soundName = disc.soundId,
                position = Vector3f(position),
                volume = 1f,
                pitch = 1f,
            )
            level.addChunkPacket(
                position.floorX shr 4,
                position.floorZ shr 4, packet
            )
        }
    }

    // TODO: Transfer the stop sound to the new sound method
    fun stop() {
        if (recordItem is ItemMusicDisc) {
            val disc = recordItem as ItemMusicDisc
            val packet = org.chorus_oss.protocol.packets.StopSoundPacket(
                soundName = disc.soundId,
                stopAll = false,
                stopLegacyMusic = false,
            )
            level.addChunkPacket(
                position.floorX shr 4,
                position.floorZ shr 4, packet
            )
        }
    }

    fun dropItem() {
        if (!recordItem.isNothing) {
            stop()
            level.dropItem(position.up(), this.recordItem)
            this.recordItem = Item.AIR
        }
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putCompound("RecordItem", NBTIO.putItemHelper(this.recordItem))
    }

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound
            .putCompound("RecordItem", NBTIO.putItemHelper(this.recordItem))

    override fun onBreak(isSilkTouch: Boolean) {
        this.dropItem()
    }
}
