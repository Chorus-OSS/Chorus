package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.inventory.MinecartChestInventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.MinecartType
import org.chorus_oss.protocol.types.ActorLink

class EntityChestMinecart(chunk: IChunk?, nbt: CompoundTag) : EntityMinecartAbstract(chunk, nbt), InventoryHolder {
    override fun getEntityIdentifier(): String {
        return EntityID.CHEST_MINECART
    }

    override var inventory = MinecartChestInventory(this)

    init {
        setDisplayBlock(Block.get(BlockID.CHEST), false)
    }

    override fun getOriginalName(): String {
        return getType().name
    }

    override fun getType(): MinecartType {
        return MinecartType.valueOf(1)
    }

    override fun isRideable(): Boolean {
        return false
    }


    override fun dropItem() {
        for (item in inventory.contents.values) {
            level!!.dropItem(this.position, item)
        }

        if (lastDamageCause is EntityDamageByEntityEvent) {
            val damager: Entity = (lastDamageCause as EntityDamageByEntityEvent).damager
            if (damager is Player && damager.isCreative) {
                return
            }
        }
        level!!.dropItem(this.position, Item.get(ItemID.CHEST_MINECART))
    }

    override fun kill() {
        super.kill()
        inventory.clearAll()
    }

    override fun mountEntity(entity: Entity, mode: ActorLink.Companion.Type): Boolean {
        return false
    }

    override fun onInteract(p: Player, item: Item, clickedPos: Vector3): Boolean {
        p.addWindow(inventory)
        return false // If true, the count of items player has in hand decreases
    }

    override fun initEntity() {
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

        entityDataMap.put(EntityDataTypes.CONTAINER_TYPE, 10)
        entityDataMap.put(EntityDataTypes.CONTAINER_SIZE, inventory.size)
        entityDataMap.put(EntityDataTypes.CONTAINER_STRENGTH_MODIFIER, 0)
    }

    override fun saveNBT() {
        super.saveNBT()

        namedTag!!.putList("Items", ListTag<CompoundTag>())
        if (this.inventory != null) {
            for (slot in 0..26) {
                val item: Item = inventory.getItem(slot)
                if (item != null && !item.isNothing) {
                    namedTag!!.getList("Items", CompoundTag::class.java)
                        .add(NBTIO.putItemHelper(item, slot))
                }
            }
        }
    }
}
