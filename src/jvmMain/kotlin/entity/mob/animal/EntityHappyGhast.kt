package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.EntityFlyable
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityRideable
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.inventory.EntityArmorInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemHarness
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemShears
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.Utils

class EntityHappyGhast(chunk: IChunk?, nbt: CompoundTag) : EntityAnimal(chunk, nbt), EntityFlyable, EntityRideable,
    InventoryHolder {
    private lateinit var armorInventory: EntityArmorInventory

    override fun getEntityIdentifier(): String {
        return EntityID.HAPPY_GHAST
    }

    override fun initEntity() {
        this.armorInventory = EntityArmorInventory(this)

        setMaxHealth(20)
        super.initEntity()
        setDataFlag(EntityFlag.COLLIDABLE, true)
        setMovementSpeedF(0.5f)
        if (namedTag!!.contains("Armor")) {
            val armorList = namedTag!!.getList("Armor", CompoundTag::class.java)
            for (armor in armorList.all) {
                armorInventory.setItem(armor.getByte("Slot").toInt(), NBTIO.getItemHelper(armor))
            }
        }
    }

    override fun saveNBT() {
        super.saveNBT()
        val armorTag = ListTag<CompoundTag>()
        for (i in 0..4) {
            armorTag.add(NBTIO.putItemHelper(armorInventory.getItem(i), i))
        }
        namedTag!!.putList("Armor", armorTag)
    }

    override fun getWidth(): Float {
        return if (isBaby()) 0.95f else 4f
    }

    override fun getHeight(): Float {
        return if (isBaby()) 0.95f else 4f
    }

    override fun getOriginalName(): String {
        return "Happy Ghast"
    }

    override fun getDrops(): Array<Item> {
        return arrayOf(armorInventory.body)
    }

    override fun getExperienceDrops(): Int {
        return Utils.rand(1, 3)
    }

    override val inventory: Inventory
        get() = armorInventory

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (item.id == ItemID.NAME_TAG && !player.isAdventure) {
            return playerApplyNameTag(player, item)
        }

        if (item is ItemHarness) {
            if (armorInventory.body.isNothing) {
                armorInventory.setBody(item)
                player.inventory.decreaseCount(player.inventory.heldItemIndex)
                armorInventory.sendContents(player)
            }
            return true
        }

        if (item is ItemShears) {
            if (!armorInventory.body.isNothing) {
                player.inventory.addItem(armorInventory.body)
                armorInventory.setBody(Item.AIR)
                armorInventory.sendContents(player)
            }
            return true
        }

        return false
    }

    override fun spawnTo(player: Player) {
        super.spawnTo(player)
        armorInventory.sendContents(player)
    }
}