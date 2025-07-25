package org.chorus_oss.chorus.inventory

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.IHuman
import org.chorus_oss.chorus.event.entity.EntityArmorChangeEvent
import org.chorus_oss.chorus.event.entity.EntityInventoryChangeEvent
import org.chorus_oss.chorus.event.player.PlayerItemHeldEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemArmor
import org.chorus_oss.chorus.item.ItemFilledMap
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.network.protocol.types.inventory.FullContainerName
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.protocol.packets.PlayerArmorDamagePacket.Companion.FLAG_BOOTS
import org.chorus_oss.protocol.packets.PlayerArmorDamagePacket.Companion.FLAG_CHESTPLATE
import org.chorus_oss.protocol.packets.PlayerArmorDamagePacket.Companion.FLAG_HELMET
import org.chorus_oss.protocol.packets.PlayerArmorDamagePacket.Companion.FLAG_LEGGINGS
import org.chorus_oss.protocol.types.BlockPos
import org.chorus_oss.protocol.types.ContainerType
import org.chorus_oss.protocol.types.item.ItemStack
import org.jetbrains.annotations.Range
import kotlin.experimental.or
import kotlin.math.min

/**
 * 0-8 物品栏<br></br>
 * 9-35 背包<br></br>
 * 36-39 盔甲栏<br></br>
 * 想获取副手库存请用[HumanOffHandInventory]<br></br>
 *
 *
 * 0-8 hotbar<br></br>
 * 9-35 inventory<br></br>
 * 36-39 Armor Inventory<br></br>
 * To obtain the off-hand inventory, please use [HumanOffHandInventory]<br></br>
 */
class HumanInventory(human: IHuman) //9+27+4
    : BaseInventory(human, InventoryType.INVENTORY, 40) {
    protected var itemInHandIndex: Int = 0

    var armorInventory: InventorySlice? = null
        protected set

    override fun init() {
        val map = super.slotTypeMap()
        for (i in 0..8) {
            map[i] = ContainerSlotType.HOTBAR
        }
        for (i in 9..35) {
            map[i] = ContainerSlotType.INVENTORY
        }
        armorInventory = object : InventorySlice(this, ARMORS_INDEX, this.size) {
            init {
                val map1 = HashMap<Int, ContainerSlotType>()
                val biMap: BiMap<Int, Int> = HashBiMap.create()
                for (i in 0..3) {
                    map1[i] = ContainerSlotType.ARMOR
                    biMap[i] = i
                }
                this.setNetworkMapping(map1, biMap)
            }
        }
    }

    /**
     * Called when a client equips a hotbar inventorySlot. This method should not be used by plugins.
     * This method will call PlayerItemHeldEvent.
     *
     * @param slot hotbar slot Number of the hotbar slot to equip.
     * @return boolean if the equipment change was successful, false if not.
     */
    fun equipItem(slot: Int): Boolean {
        if (!isHotbarSlot(slot)) {
            this.sendContents((holder as Player))
            return false
        }

        if (holder is Player) {
            val player = holder as Player
            val ev = PlayerItemHeldEvent(player, this.getItem(slot), slot)
            Server.instance.pluginManager.callEvent(ev)

            if (ev.cancelled) {
                this.sendContents(this.viewers)
                return false
            }

            val item = getItem(slot)
            if (item is ItemFilledMap) {
                item.sendImage(player, 1)
            }

            if (player.fishing != null) {
                if (item != player.fishing!!.rod) {
                    player.stopFishing(false)
                }
            }
        }

        this.setHeldItemIndex(slot, false)
        return true
    }

    fun isHotbarSlot(slot: Int): Boolean {
        return slot >= 0 && slot < this.hotbarSize
    }

    var heldItemIndex: Int
        get() = this.itemInHandIndex
        set(index) {
            setHeldItemIndex(index, true)
        }

    fun setHeldItemIndex(index: Int, send: Boolean) {
        if (index >= 0 && index < this.hotbarSize) {
            this.itemInHandIndex = index

            if (holder is Player && send) {
                this.sendHeldItem((holder as Player))
            }

            this.sendHeldItem((holder as IHuman).getEntity().viewers.values)
        }
    }

    val itemInHand: Item
        get() = this.getItem(this.heldItemIndex)

    fun setItemInHand(item: Item): Boolean {
        return this.setItem(this.heldItemIndex, item)
    }

    fun setItemInHand(item: Item, send: Boolean): Boolean {
        return this.setItem(this.heldItemIndex, item, send)
    }

    fun setHeldItemSlot(slot: Int) {
        if (!isHotbarSlot(slot)) {
            return
        }

        this.itemInHandIndex = slot

        if (holder is Player) {
            this.sendHeldItem((holder as Player))
        }

        this.sendHeldItem(this.viewers)
    }

    fun sendHeldItem(vararg players: Player) {
        val item = this.itemInHand

        val pk = org.chorus_oss.protocol.packets.MobEquipmentPacket(
            entityRuntimeID = (holder as IHuman).getEntity().getRuntimeID().toULong(),
            newItem = ItemStack(item),
            inventorySlot = heldItemIndex.toByte(),
            hotbarSlot = heldItemIndex.toByte(),
            windowID = 0
        )
        for (player in players) {
            if (player == this.holder) {
                this.sendSlot(this.heldItemIndex, player)
            }
            player.sendPacket(pk)
        }
    }

    fun sendHeldItem(players: Collection<Player>) {
        this.sendHeldItem(*players.toTypedArray())
    }

    override fun onSlotChange(index: Int, before: Item, send: Boolean) {
        val holder: IHuman = (this.holder as IHuman)
        if (holder is Player && !holder.spawned) {
            return
        }

        if (index >= ARMORS_INDEX) {
            this.sendArmorSlot(index - ARMORS_INDEX, this.viewers)
            this.sendArmorSlot(
                index - ARMORS_INDEX,
                holder.getEntity().viewers.values
            )
            if (getItem(index) is ItemArmor) {
                holder.getEntity().level!!.vibrationManager.callVibrationEvent(
                    VibrationEvent(
                        holder,
                        holder.getEntity().position, VibrationType.EQUIP
                    )
                )
            }
        } else {
            super.onSlotChange(index, before, send)
            if (index == heldItemIndex && before != slots[index]) {
                equipItem(index)
            }
        }
    }

    override fun canAddItem(item: Item): Boolean {
        var item1 = item
        item1 = item1.clone()
        val checkDamage = item1.hasMeta()
        val checkTag = item1.compoundTag != null
        for (i in 0..<ARMORS_INDEX) {
            val slot = this.getUnclonedItem(i)
            if (item1.equals(slot, checkDamage, checkTag)) {
                val diff: Int
                if (((min(slot.maxStackSize.toDouble(), maxStackSize.toDouble()) - slot.getCount()).also {
                        diff =
                            it.toInt()
                    }) > 0) {
                    item1.setCount(item1.getCount() - diff)
                }
            } else if (slot.isNothing) {
                item1.setCount(
                    (item1.getCount() - min(
                        slot.maxStackSize.toDouble(),
                        maxStackSize.toDouble()
                    )).toInt()
                )
            }

            if (item1.getCount() <= 0) {
                return true
            }
        }

        return false
    }

    override fun addItem(vararg slots: Item): Array<Item> {
        val itemSlots: MutableList<Item> = ArrayList()
        for (slot in slots) {
            if (!slot.isNothing) {
                // TODO: clone only if necessary
                itemSlots.add(slot.clone())
            }
        }

        val emptySlots = mutableListOf<Int>()

        for (i in 0..<ARMORS_INDEX) {
            //获取未克隆Item对象
            var item = this.getUnclonedItem(i)
            if (item.isNothing || item.getCount() <= 0) {
                emptySlots.add(i)
            }

            //使用迭代器而不是新建一个ArrayList
            val iterator = itemSlots.iterator()
            while (iterator.hasNext()) {
                val slot = iterator.next()
                if (slot == item) {
                    val maxStackSize = min(this.maxStackSize.toDouble(), item.maxStackSize.toDouble()).toInt()
                    if (item.getCount() < maxStackSize) {
                        var amount =
                            min((maxStackSize - item.getCount()).toDouble(), slot.getCount().toDouble()).toInt()
                        amount = min(amount.toDouble(), this.maxStackSize.toDouble()).toInt()
                        if (amount > 0) {
                            //在需要clone时再clone
                            item = item.clone()
                            slot.setCount(slot.getCount() - amount)
                            item.setCount(item.getCount() + amount)
                            this.setItem(i, item)
                            if (slot.getCount() <= 0) {
                                iterator.remove()
                            }
                        }
                    }
                }
            }
            if (itemSlots.isEmpty()) {
                break
            }
        }

        if (itemSlots.isNotEmpty() && emptySlots.isNotEmpty()) {
            for (slotIndex in emptySlots) {
                if (itemSlots.isNotEmpty()) {
                    val slot = itemSlots[0]
                    val maxStackSize = min(slot.maxStackSize.toDouble(), this.maxStackSize.toDouble()).toInt()
                    var amount = min(maxStackSize.toDouble(), slot.getCount().toDouble()).toInt()
                    amount = min(amount.toDouble(), this.maxStackSize.toDouble()).toInt()
                    slot.setCount(slot.getCount() - amount)
                    val item = slot.clone()
                    item.setCount(amount)
                    this.setItem(slotIndex, item)
                    if (slot.getCount() <= 0) {
                        itemSlots.remove(slot)
                    }
                }
            }
        }

        return itemSlots.toTypedArray()
    }

    val hotbarSize: Int
        get() = 9

    fun getArmorItem(index: Int): Item {
        return this.getItem(ARMORS_INDEX + index)
    }

    fun setArmorItem(index: @Range(from = 0, to = 3) Int, item: Item): Boolean {
        return this.setArmorItem(index, item, false)
    }

    fun setArmorItem(index: @Range(from = 0, to = 3) Int, item: Item, ignoreArmorEvents: Boolean): Boolean {
        return this.setItem(ARMORS_INDEX + index, item, ignoreArmorEvents)
    }

    val helmet: Item
        get() = this.getItem(ARMORS_INDEX)

    val chestplate: Item
        get() = this.getItem(ARMORS_INDEX + 1)

    val leggings: Item
        get() = this.getItem(ARMORS_INDEX + 2)

    val boots: Item
        get() = this.getItem(ARMORS_INDEX + 3)

    fun setHelmet(helmet: Item): Boolean {
        return this.setItem(ARMORS_INDEX, helmet)
    }

    fun setChestplate(chestplate: Item): Boolean {
        return this.setItem(ARMORS_INDEX + 1, chestplate)
    }

    fun setLeggings(leggings: Item): Boolean {
        return this.setItem(ARMORS_INDEX + 2, leggings)
    }

    fun setBoots(boots: Item): Boolean {
        return this.setItem(ARMORS_INDEX + 3, boots)
    }

    override fun setItem(index: @Range(from = 0, to = 39) Int, item: Item): Boolean {
        return setItem(index, item, true, false)
    }

    override fun setItem(index: @Range(from = 0, to = 39) Int, item: Item, send: Boolean): Boolean {
        return setItem(index, item, send, false)
    }

    private fun setItem(
        index: @Range(from = 0, to = 39) Int,
        item: Item,
        send: Boolean,
        ignoreArmorEvents: Boolean
    ): Boolean {
        var item = item
        if (index < 0 || index >= this.size) {
            return false
        } else if (item.isNothing) {
            return this.clear(index)
        }

        //Armor change
        if (!ignoreArmorEvents && index >= ARMORS_INDEX) {
            val ev = EntityArmorChangeEvent((holder as IHuman).getEntity(), this.getItem(index), item, index)
            Server.instance.pluginManager.callEvent(ev)
            if (ev.cancelled) {
                this.sendArmorSlot(index, this.viewers)
                return false
            }
            item = ev.newItem
        } else {
            val ev = EntityInventoryChangeEvent((holder as IHuman).getEntity(), this.getItem(index), item, index)
            Server.instance.pluginManager.callEvent(ev)
            if (ev.cancelled) {
                this.sendSlot(index, this.viewers)
                return false
            }
            item = ev.newItem
        }
        val old = this.getItem(index)
        slots[index] = item.clone()
        this.onSlotChange(index, old, send)
        return true
    }

    override fun clear(index: Int, send: Boolean): Boolean {
        if (slots.containsKey(index)) {
            var item = Item.AIR
            val old = slots[index]
            if (index >= ARMORS_INDEX && index < this.size) {
                val ev = EntityArmorChangeEvent(
                    (holder as IHuman).getEntity(),
                    old!!, item, index
                )
                Server.instance.pluginManager.callEvent(ev)
                if (ev.cancelled) {
                    this.sendSlot(index, this.viewers)
                    return false
                }
                item = ev.newItem
            } else if (index < ARMORS_INDEX) {
                val ev = EntityInventoryChangeEvent(
                    (holder as IHuman).getEntity(),
                    old!!, item, index
                )
                Server.instance.pluginManager.callEvent(ev)
                if (ev.cancelled) {
                    this.sendSlot(index, this.viewers)
                    return false
                }
                item = ev.newItem
            } else {
                return false
            }

            if (!item.isNothing) {
                slots[index] = item.clone()
            } else {
                slots.remove(index)
            }

            this.onSlotChange(index, old, send)
        }

        return true
    }

    var armorContents: Array<Item>
        get() {
            return Array(4) { i ->
                this.getItem(ARMORS_INDEX + i)
            }
        }
        /**
         * Set all armor for the player
         *
         * @param items all armors
         */
        set(items) {
            var items1 = items
            if (items1.size < 4) {
                val newItems = Array(4) { Item.AIR }
                items1.copyInto(newItems)
                items1 = newItems
            }
            for (i in 0..3) {
                if (items1[i].isNothing) {
                    this.clear(ARMORS_INDEX + i)
                } else {
                    this.setItem(ARMORS_INDEX + i, items1[i])
                }
            }
        }

    override fun clearAll() {
        for (index in 0..<size) {
            this.clear(index)
        }
        (holder as IHuman).offhandInventory.clearAll()
    }

    /**
     * Send armor contents.
     *
     * @param players the players
     */
    fun sendArmorContents(players: Collection<Player>) {
        this.sendArmorContents(players.toTypedArray())
    }

    /**
     * Send armor contents.
     *
     * @param player the player
     */
    fun sendArmorContents(player: Player) {
        this.sendArmorContents(arrayOf(player))
    }

    /**
     * Send armor contents.
     *
     * @param players the players
     */
    fun sendArmorContents(players: Array<Player>) {
        val armor = this.armorContents

        val pk = org.chorus_oss.protocol.packets.MobArmorEquipmentPacket(
            entityRuntimeID = (holder as IHuman).getEntity().getRuntimeID().toULong(),
            head = ItemStack(armor[0]),
            torso = ItemStack(armor[1]),
            legs = ItemStack(armor[2]),
            feet = ItemStack(armor[3]),
            body = ItemStack(Item.AIR),
        )

        for (player in players) {
            if (player == this.holder) {
                val id = SpecialWindowId.ARMOR.id
                val packet = org.chorus_oss.protocol.packets.InventoryContentPacket(
                    windowID = id.toUInt(),
                    content = armor.toList().map { ItemStack(it) },
                    container = org.chorus_oss.protocol.types.inventory.FullContainerName(
                        org.chorus_oss.protocol.types.itemstack.ContainerSlotType.Armor,
                        id
                    ),
                    storageItem = ItemStack(Item.AIR)
                )
                player.sendPacket(packet)

                var pk2 = org.chorus_oss.protocol.packets.PlayerArmorDamagePacket(
                    bitset = 0,
                    helmetDamage = 0,
                    chestplateDamage = 0,
                    leggingsDamage = 0,
                    bootsDamage = 0,
                    bodyDamage = 0,
                )

                if (!armor[0].isNothing) {
                    pk2 = pk2.copy(
                        bitset = pk2.bitset or FLAG_HELMET,
                        helmetDamage = armor[0].damage
                    )
                }

                if (!armor[1].isNothing) {
                    pk2 = pk2.copy(
                        bitset = pk2.bitset or FLAG_CHESTPLATE,
                        chestplateDamage = armor[1].damage
                    )
                }

                if (!armor[2].isNothing) {
                    pk2 = pk2.copy(
                        bitset = pk2.bitset or FLAG_LEGGINGS,
                        leggingsDamage = armor[2].damage
                    )
                }

                if (!armor[3].isNothing) {
                    pk2 = pk2.copy(
                        bitset = pk2.bitset or FLAG_BOOTS,
                        bootsDamage = armor[3].damage
                    )
                }

                player.sendPacket(pk2)
            } else {
                player.sendPacket(pk)
            }
        }
    }

    /**
     * Send armor slot.
     *
     * @param index  the index 0~3
     * @param player the player
     */
    fun sendArmorSlot(index: Int, player: Player) {
        this.sendArmorSlot(index, arrayOf(player))
    }

    /**
     * Send armor slot.
     *
     * @param index   the index 0~3
     * @param players the players
     */
    fun sendArmorSlot(index: Int, players: Collection<Player>) {
        this.sendArmorSlot(index, players.toTypedArray())
    }

    /**
     * Send armor slot.
     *
     * @param index   the index 0~3
     * @param players the players
     */
    fun sendArmorSlot(index: Int, players: Array<Player>) {
        val armor = this.armorContents

        val pk = org.chorus_oss.protocol.packets.MobArmorEquipmentPacket(
            entityRuntimeID = (holder as IHuman).getEntity().getRuntimeID().toULong(),
            head = ItemStack(armor[0]),
            torso = ItemStack(armor[1]),
            legs = ItemStack(armor[2]),
            feet = ItemStack(armor[3]),
            body = ItemStack(Item.AIR),
        )

        for (player in players) {
            if (player == this.holder) {
                val id = SpecialWindowId.ARMOR.id
                val packet = org.chorus_oss.protocol.packets.InventorySlotPacket(
                    windowID = id.toUInt(),
                    slot = index.toUInt(),
                    container = org.chorus_oss.protocol.types.inventory.FullContainerName(
                        org.chorus_oss.protocol.types.itemstack.ContainerSlotType.Armor,
                        id
                    ),
                    storageItem = ItemStack(Item.AIR),
                    newItem = ItemStack(this.getItem(ARMORS_INDEX + index))
                )
                player.sendPacket(packet)

                val pk2 = org.chorus_oss.protocol.packets.PlayerArmorDamagePacket(
                    bitset = when (index) {
                        0 -> FLAG_HELMET
                        1 -> FLAG_CHESTPLATE
                        2 -> FLAG_LEGGINGS
                        3 -> FLAG_BOOTS
                        else -> 0
                    },
                    helmetDamage = if (!armor[0].isNothing) armor[0].damage else 0,
                    chestplateDamage = if (!armor[1].isNothing) armor[1].damage else 0,
                    leggingsDamage = if (!armor[2].isNothing) armor[2].damage else 0,
                    bootsDamage = if (!armor[3].isNothing) armor[3].damage else 0,
                    bodyDamage = 0,
                )
                player.sendPacket(pk2)
            } else {
                player.sendPacket(pk)
            }
        }
    }

    override fun sendContents(player: Player) {
        this.sendContents(*arrayOf(player))
    }

    override fun sendContents(players: Collection<Player>) {
        this.sendContents(*players.toTypedArray())
    }

    override fun sendContents(vararg players: Player) {
        val inventoryAndHotBarSize = this.size - 4

        for (player in players) {
            val id = player.getWindowId(this)
            if (id == -1 || !player.spawned) {
                if (this.holder !== player) this.close(player)
                continue
            }

            val packet = org.chorus_oss.protocol.packets.InventoryContentPacket(
                windowID = id.toUInt(),
                content = List(inventoryAndHotBarSize) { ItemStack(this.getItem(it)) },
                container = org.chorus_oss.protocol.types.inventory.FullContainerName.invoke(
                    FullContainerName(
                        this.getSlotType(id),
                        id
                    )
                ),
                storageItem = ItemStack(Item.AIR)
            )

            player.sendPacket(packet)
        }
    }

    override fun sendSlot(index: Int, player: Player) {
        this.sendSlot(index, *arrayOf(player))
    }

    override fun sendSlot(index: Int, players: Collection<Player>) {
        this.sendSlot(index, *players.toTypedArray())
    }

    override fun sendSlot(index: Int, vararg players: Player) {
        for (player in players) {
            if (player == this.holder) {
                val id = SpecialWindowId.PLAYER.id
                val packet = org.chorus_oss.protocol.packets.InventorySlotPacket(
                    windowID = id.toUInt(),
                    slot = index.toUInt(),
                    container = org.chorus_oss.protocol.types.inventory.FullContainerName.invoke(
                        FullContainerName(
                            this.getSlotType(index),
                            id
                        )
                    ),
                    storageItem = ItemStack(Item.AIR),
                    newItem = ItemStack(this.getItem(index))
                )
                player.sendPacket(packet)
            } else {
                val id = player.getWindowId(this)
                if (id == -1) {
                    this.close(player)
                    continue
                }

                val packet = org.chorus_oss.protocol.packets.InventorySlotPacket(
                    windowID = id.toUInt(),
                    slot = index.toUInt(),
                    container = org.chorus_oss.protocol.types.inventory.FullContainerName.invoke(
                        FullContainerName(
                            this.getSlotType(index),
                            id
                        )
                    ),
                    storageItem = ItemStack(Item.AIR),
                    newItem = ItemStack(this.getItem(index))
                )
                player.sendPacket(packet)
            }
        }
    }

    override fun onOpen(who: Player) {
        super.onOpen(who)
        if (who.spawned) {
            who.sendPacket(
                org.chorus_oss.protocol.packets.ContainerOpenPacket(
                    containerID = who.getWindowId(this).toByte(),
                    containerType = ContainerType(type),
                    position = BlockPos(who.vector3),
                    targetActorID = who.getUniqueID()
                )
            )
        }
    }

    override fun onClose(who: Player) {
        val containerId = who.getWindowId(this)
        who.sendPacket(
            org.chorus_oss.protocol.packets.ContainerClosePacket(
                containerID = containerId.toByte(),
                containerType = ContainerType(type),
                serverInitiatedClose = who.closingWindowId != containerId
            )
        )
        // player can never stop viewing their own inventory
        if (who !== holder) {
            super.onClose(who)
        }
    }

    companion object {
        const val ARMORS_INDEX: Int = 36
    }
}
