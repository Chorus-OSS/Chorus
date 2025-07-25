package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.inventory.BaseInventory
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.inventory.InventoryType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.protocol.types.item.ItemStack

class EntityEquipment(holder: InventoryHolder) : BaseInventory(holder, InventoryType.INVENTORY, 6) {
    private val entity: Entity

    /**
     * @param holder an Entity which implements [InventoryHolder].
     * @throws ClassCastException if the entity does not implements [InventoryHolder]
     */
    init {
        this.entity = holder as Entity
    }

    override val size: Int
        get() = 6

    fun getEntity(): Entity {
        return entity
    }

    override val viewers: MutableSet<Player> = super.viewers
        get() {
            val viewers: MutableSet<Player> = HashSet(field)
            viewers.addAll(entity.viewers.values)
            return viewers
        }

    override fun open(who: Player): Boolean {
        return viewers.add(who)
    }

    fun getMainHand(): Item {
        return this.getItem(MAIN_HAND)
    }

    fun getOffHand(): Item {
        return this.getItem(OFF_HAND)
    }

    fun setMainHand(item: Item): Boolean {
        return this.setMainHand(item, true)
    }

    fun setMainHand(item: Item, send: Boolean): Boolean {
        return this.setItem(MAIN_HAND, item, send)
    }

    fun setOffHand(item: Item): Boolean {
        return this.setOffHand(item, true)
    }

    fun setOffHand(item: Item, send: Boolean): Boolean {
        return this.setItem(OFF_HAND, item, send)
    }

    fun getArmor(): List<Item> {
        val armor = mutableListOf<Item>()
        armor.add(HEAD, this.getHead())
        armor.add(CHEST, this.getChest())
        armor.add(LEGS, this.getLegs())
        armor.add(FEET, this.getFeet())
        return armor
    }

    fun getHead(): Item {
        return this.getItem(HEAD)
    }

    fun getChest(): Item {
        return this.getItem(CHEST)
    }

    fun getLegs(): Item {
        return this.getItem(LEGS)
    }

    fun getFeet(): Item {
        return this.getItem(FEET)
    }

    fun setArmor(items: List<Item>): Boolean {
        return this.setArmor(items, true)
    }

    fun setArmor(items: List<Item>, send: Boolean): Boolean {
        val head: Boolean = this.setHead(items[HEAD], send)
        val chest: Boolean = this.setChest(items[CHEST], send)
        val legs: Boolean = this.setLegs(items[LEGS], send)
        val feet: Boolean = this.setFeet(items[FEET], send)
        return head && chest && legs && feet
    }

    fun setHead(item: Item): Boolean {
        return this.setHead(item, true)
    }

    fun setHead(item: Item, send: Boolean): Boolean {
        return this.setItem(HEAD, item, send)
    }

    fun setChest(item: Item): Boolean {
        return this.setChest(item, true)
    }

    fun setChest(item: Item, send: Boolean): Boolean {
        return this.setItem(CHEST, item, send)
    }

    fun setLegs(item: Item): Boolean {
        return this.setLegs(item, true)
    }

    fun setLegs(item: Item, send: Boolean): Boolean {
        return this.setItem(LEGS, item, send)
    }

    fun setFeet(item: Item): Boolean {
        return this.setFeet(item, true)
    }

    fun setFeet(item: Item, send: Boolean): Boolean {
        return this.setItem(FEET, item, send)
    }

    fun canEquipByDispenser(): Boolean {
        return true
    }

    fun equip(item: Item): Boolean {
        if (item.isHelmet) {
            if (item.tier > getHead().tier) {
                entity.level!!.dropItem(entity.position, getHead())
                this.setHead(item)
                return true
            }
        } else if (item.isChestplate) {
            if (item.tier > getChest().tier) {
                entity.level!!.dropItem(entity.position, getChest())
                this.setChest(item)
                return true
            }
        } else if (item.isLeggings) {
            if (item.tier > getLegs().tier) {
                entity.level!!.dropItem(entity.position, getLegs())
                this.setLegs(item)
                return true
            }
        } else if (item.isBoots) {
            if (item.tier > getFeet().tier) {
                entity.level!!.dropItem(entity.position, getFeet())
                this.setFeet(item)
                return true
            }
        } else if (item.tier > getMainHand().tier) {
            entity.level!!.dropItem(entity.position, getMainHand())
            this.setMainHand(item)
            return true
        }
        return false
    }

    override fun sendSlot(index: Int, vararg players: Player) {
        for (player: Player in players) {
            this.sendSlot(index, player)
        }
    }

    override fun sendSlot(index: Int, player: Player) {
        when (index) {
            MAIN_HAND, OFF_HAND -> {
                val packet = org.chorus_oss.protocol.packets.MobEquipmentPacket(
                    entityRuntimeID = entity.getRuntimeID().toULong(),
                    newItem = ItemStack(this.getItem(index)),
                    inventorySlot = (index - 4).toByte(),
                    hotbarSlot = 0,
                    windowID = 0
                )
                player.sendPacket(packet)
            }

            HEAD, CHEST, LEGS, FEET -> {
                val armor = getArmor()
                val packet = org.chorus_oss.protocol.packets.MobArmorEquipmentPacket(
                    entityRuntimeID = entity.getRuntimeID().toULong(),
                    head = ItemStack(armor[0]),
                    torso = ItemStack(armor[1]),
                    legs = ItemStack(armor[2]),
                    feet = ItemStack(armor[3]),
                    body = ItemStack(Item.AIR)
                )
                player.sendPacket(packet)
            }

            else -> throw IllegalStateException("Unexpected value: $index")
        }
    }

    override fun sendContents(player: Player) {
        this.sendSlot(HEAD, player)
        this.sendSlot(CHEST, player)
        this.sendSlot(LEGS, player)
        this.sendSlot(FEET, player)
        this.sendSlot(MAIN_HAND, player)
        this.sendSlot(OFF_HAND, player)
    }

    override fun sendContents(vararg players: Player) {
        for (player: Player in players) {
            this.sendContents(player)
        }
    }

    companion object {
        const val HEAD: Int = 0
        const val CHEST: Int = 1
        const val LEGS: Int = 2
        const val FEET: Int = 3

        const val MAIN_HAND: Int = 4
        const val OFF_HAND: Int = 5
    }
}
