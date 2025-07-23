package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockFrame
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.event.block.ItemFrameUseEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.packets.LevelEventPacket
import java.util.concurrent.ThreadLocalRandom

open class BlockEntityItemFrame(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    override fun loadNBT() {
        super.loadNBT()
        if (!namedTag.contains("Item")) {
            namedTag.putCompound("Item", NBTIO.putItemHelper(Item.AIR))
        }
        if (!namedTag.contains("ItemRotation")) {
            namedTag.putByte("ItemRotation", 0)
        }
        if (!namedTag.contains("ItemDropChance")) {
            namedTag.putFloat("ItemDropChance", 1.0f)
        }
        level.updateComparatorOutputLevel(this.position)
    }

    override var name: String
        get() = "Item Frame"
        set(name) {
            super.name = name
        }

    override val isBlockEntityValid: Boolean
        get() = this.block is BlockFrame

    var itemRotation: Int
        get() = namedTag.getByte("ItemRotation").toInt()
        set(itemRotation) {
            namedTag.putByte("ItemRotation", itemRotation)
            level.updateComparatorOutputLevel(this.position)
            this.setDirty()
        }

    var item: Item
        get() {
            val NBTTag = namedTag.getCompound("Item")
            return NBTIO.getItemHelper(NBTTag)
        }
        set(item) {
            this.setItem(item, true)
        }

    fun setItem(item: Item, setChanged: Boolean) {
        namedTag.putCompound("Item", NBTIO.putItemHelper(item))
        if (setChanged) {
            this.setDirty()
        } else level.updateComparatorOutputLevel(this.position)
    }

    var itemDropChance: Float
        get() = namedTag.getFloat("ItemDropChance")
        set(chance) {
            namedTag.putFloat("ItemDropChance", chance)
        }

    override fun setDirty() {
        this.spawnToAll()
        super.setDirty()
    }

    override val spawnCompound: CompoundTag
        get() {
            if (!namedTag.contains("Item")) {
                this.setItem(Item.AIR, false)
            }
            val item = item
            val tag = super.spawnCompound

            if (!item.isNothing) {
                val itemTag = NBTIO.putItemHelper(item)
                val networkDamage = item.damage
                val namespacedId = item.id
                if (namespacedId != null) {
                    itemTag.remove("id")
                    itemTag.putShort("Damage", networkDamage)
                    itemTag.putString("Name", namespacedId)
                }
                if (item.isBlock()) {
                    itemTag.putCompound("Block", item.getSafeBlockState().blockStateTag)
                }
                tag.putCompound("Item", itemTag)
                    .putByte("ItemRotation", this.itemRotation)
            }
            return tag
        }

    val analogOutput: Int
        get() = if (this.item == null || item.isNothing) 0 else itemRotation % 8 + 1

    fun dropItem(player: Player?): Boolean {
        val before = this.item
        if (before == null || before.isNothing) {
            return false
        }

        val event = ItemFrameUseEvent(player, this.block, this, before, ItemFrameUseEvent.Action.DROP)
        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) return false

        val drop = dropItemAndGetEntity(player)
        if (drop != null) {
            return true
        }
        val after = this.item
        return after == null || after.isNothing
    }

    fun dropItemAndGetEntity(player: Player?): EntityItem? {
        val level = level
        val drop = item
        if (drop.isNothing) {
            if (player != null) {
                spawnTo(player)
            }
            return null
        }


        var itemEntity: EntityItem? = null
        if (this.itemDropChance > ThreadLocalRandom.current().nextFloat()) {
            itemEntity = level.dropAndGetItem(position.add(0.5, 0.25, 0.5), drop)
            if (itemEntity == null) {
                if (player != null) {
                    spawnTo(player)
                }
                return null
            }
        }

        setItem(Item.get(BlockID.AIR, 0, 1), true)
        itemRotation = 0
        spawnToAll()
        level.addLevelEvent(this.position, LevelEventPacket.SOUND_ITEMFRAME_BREAK)

        return itemEntity
    }
}
