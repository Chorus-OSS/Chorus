package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.entity.EntityEquipment
import org.chorus_oss.chorus.item.Item


interface EntityInventoryHolder : InventoryHolder {
    val equipment: EntityEquipment

    fun canEquipByDispenser(): Boolean {
        return false
    }

    val helmet: Item
        get() = equipment.getHead()

    fun setHelmet(item: Item): Boolean {
        return equipment.setHead(item)
    }

    val chestplate: Item
        get() = equipment.getChest()

    fun setChestplate(item: Item): Boolean {
        return equipment.setChest(item)
    }

    val leggings: Item
        get() = equipment.getLegs()

    fun setLeggings(item: Item): Boolean {
        return equipment.setLegs(item)
    }

    val boots: Item
        get() = equipment.getFeet()

    fun setBoots(item: Item): Boolean {
        return equipment.setFeet(item)
    }

    val itemInHand: Item
        get() = equipment.getMainHand()

    val itemInOffhand: Item
        get() = equipment.getOffHand()

    fun setItemInHand(item: Item): Boolean {
        return equipment.setMainHand(item)
    }

    fun setItemInHand(item: Item, send: Boolean): Boolean {
        return equipment.setMainHand(item, send)
    }

    fun setItemInOffhand(item: Item): Boolean {
        return equipment.setOffHand(item, true)
    }

    fun setItemInOffhand(item: Item, send: Boolean): Boolean {
        return equipment.setOffHand(item, send)
    }

    fun equip(item: Item): Boolean {
        return equipment.equip(item)
    }
}
