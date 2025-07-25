package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.inventory.ChestBoatInventory
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.inventory.InventoryType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag

class EntityChestBoat(chunk: IChunk?, nbt: CompoundTag?) : EntityBoat(chunk, nbt), InventoryHolder {
    override fun getEntityIdentifier(): String {
        return EntityID.CHEST_BOAT
    }

    override var inventory = ChestBoatInventory(this)

    override fun getOriginalName(): String {
        return "Chest Boat"
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (player.isSneaking()) {
            player.addWindow(inventory)
            return false
        }

        if (passengers.size >= 1 || getWaterLevel() < -SINKING_DEPTH) {
            return false
        }

        super.mountEntity(player)
        return false
    }

    override fun getInteractButtonText(player: Player): String {
        if (player.isSneaking()) {
            return "action.interact.opencontainer"
        }
        return "action.interact.ride.boat"
    }

    public override fun initEntity() {
        super.initEntity()

        if (namedTag!!.contains("Items") && namedTag!!.get("Items") is ListTag<*>) {
            val inventoryList: ListTag<CompoundTag> = namedTag!!.getList(
                "Items",
                CompoundTag::class.java
            )
            for (item: CompoundTag in inventoryList.all) {
                inventory.setItem(item.getByte("Slot").toInt(), NBTIO.getItemHelper(item))
            }
        }

        entityDataMap.put(EntityDataTypes.CONTAINER_TYPE, InventoryType.CHEST_BOAT.networkType)
        entityDataMap.put(EntityDataTypes.CONTAINER_SIZE, inventory.size)
        entityDataMap.put(EntityDataTypes.CONTAINER_STRENGTH_MODIFIER, 0)
    }

    override fun saveNBT() {
        super.saveNBT()

        namedTag!!.putList("Items", ListTag<CompoundTag>())
        if (this.inventory != null) {
            for (slot in 0..26) {
                val item = inventory.getItem(slot)
                if (item != null && !item.isNothing) {
                    namedTag!!.getList("Items", CompoundTag::class.java)
                        .add(NBTIO.putItemHelper(item, slot))
                }
            }
        }
    }

    override fun dropItem() {
        when (this.getBoatVariant()) {
            0 -> level!!.dropItem(this.position, Item.get(ItemID.OAK_CHEST_BOAT))
            1 -> level!!.dropItem(this.position, Item.get(ItemID.SPRUCE_CHEST_BOAT))
            2 -> level!!.dropItem(this.position, Item.get(ItemID.BIRCH_CHEST_BOAT))
            3 -> level!!.dropItem(this.position, Item.get(ItemID.JUNGLE_CHEST_BOAT))
            4 -> level!!.dropItem(this.position, Item.get(ItemID.ACACIA_CHEST_BOAT))
            5 -> level!!.dropItem(this.position, Item.get(ItemID.DARK_OAK_CHEST_BOAT))
            6 -> level!!.dropItem(this.position, Item.get(ItemID.MANGROVE_CHEST_BOAT))
            else -> level!!.dropItem(this.position, Item.get(ItemID.CHEST_BOAT))
        }

        for (item in inventory.contents.values) {
            level!!.dropItem(this.position, item)
        }
        inventory.clearAll()
    }
}
