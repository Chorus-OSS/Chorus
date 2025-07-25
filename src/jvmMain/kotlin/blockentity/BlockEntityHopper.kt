package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockComposter
import org.chorus_oss.chorus.block.BlockHopper
import org.chorus_oss.chorus.block.BlockHopper.IHopper
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.event.block.HopperSearchItemEvent
import org.chorus_oss.chorus.event.inventory.InventoryMoveItemEvent
import org.chorus_oss.chorus.inventory.HopperInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.inventory.RecipeInventoryHolder
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemPotion
import org.chorus_oss.chorus.item.ItemSplashPotion
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.registry.Registries

class BlockEntityHopper(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt), BlockEntityInventoryHolder,
    IHopper {
    override var inventory: Inventory = HopperInventory(this)

    var transferCooldown: Int = 0

    private lateinit var pickupArea: AxisAlignedBB

    var isDisabled: Boolean = false

    private val temporalVector = BlockVector3()

    // The container mine cart detects the funnel and notifies the update, which greatly optimizes performance
    var minecartInvPickupFrom: InventoryHolder? = null
    var minecartInvPushTo: InventoryHolder? = null

    override fun initBlockEntity() {
        super.initBlockEntity()
        this.scheduleUpdate()
    }

    override fun loadNBT() {
        super.loadNBT()
        if (namedTag.contains("TransferCooldown")) {
            this.transferCooldown = namedTag.getInt("TransferCooldown")
        } else {
            this.transferCooldown = 8
        }

        this.inventory = HopperInventory(this)

        if (!namedTag.contains("Items") || namedTag["Items"] !is ListTag<*>) {
            namedTag.putList("Items", ListTag<CompoundTag>())
        }

        for (i in 0..<this.size) {
            inventory.setItem(i, this.getItem(i))
        }

        this.pickupArea = generatePickupArea()

        checkDisabled()
    }

    protected fun generatePickupArea(): SimpleAxisAlignedBB {
        return SimpleAxisAlignedBB(
            position.x,
            position.y,
            position.z, position.x + 1, position.y + 2, position.z + 1
        )
    }

    protected fun checkDisabled() {
        if (block is BlockHopper) {
            isDisabled = !(block as BlockHopper).isEnabled
        }
    }

    val cooldownTick: Int
        /**
         * @return How much ticks does it take for the hopper to transfer an item
         */
        get() = 8

    protected fun checkBlockStateValid(levelBlockState: BlockState): Boolean {
        return levelBlockState.identifier == BlockID.HOPPER
    }

    override val isBlockEntityValid: Boolean
        get() = level.getBlockIdAt(
            position.floorX,
            position.floorY,
            position.floorZ
        ) == BlockID.HOPPER

    override var name: String
        get() = if (this.hasName()) namedTag.getString("CustomName") else "Hopper"
        set(name) {
            if (name == null || name.isEmpty()) {
                namedTag.remove("CustomName")
                return
            }

            namedTag.putString("CustomName", name)
        }

    override fun hasName(): Boolean {
        return namedTag.contains("CustomName")
    }

    val isOnTransferCooldown: Boolean
        get() = this.transferCooldown > 0

    val size: Int
        get() = 5

    protected fun getSlotIndex(index: Int): Int {
        val list = namedTag.getList("Items", CompoundTag::class.java)
        for (i in 0..<list.size()) {
            if (list[i].getByte("Slot").toInt() == index) {
                return i
            }
        }

        return -1
    }

    fun getItem(index: Int): Item {
        val i = this.getSlotIndex(index)
        if (i < 0) {
            return Item.AIR
        } else {
            val data = namedTag.getList("Items")[i] as CompoundTag
            return NBTIO.getItemHelper(data)
        }
    }

    fun setItem(index: Int, item: Item) {
        val i = this.getSlotIndex(index)

        val d = NBTIO.putItemHelper(item, index)

        if (item.isNothing || item.getCount() <= 0) {
            if (i >= 0) {
                namedTag.getList("Items").remove(i)
            }
        } else if (i < 0) {
            (namedTag.getList("Items", CompoundTag::class.java)).add(d)
        } else {
            (namedTag.getList("Items", CompoundTag::class.java)).add(i, d)
        }
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putList("Items", ListTag<CompoundTag>())
        for (index in 0..<this.size) {
            this.setItem(index, inventory.getItem(index))
        }

        namedTag.putInt("TransferCooldown", this.transferCooldown)
    }

    override fun onUpdate(): Boolean {
        if (this.closed) {
            return false
        }

        if (isOnTransferCooldown) {
            transferCooldown--
            return true
        }

        if (isDisabled) {
            return false
        }

        val blockSide = getSide(BlockFace.UP).tickCachedLevelBlock
        val blockEntity =
            level.getBlockEntity(temporalVector.setComponentsAdding(this.position, BlockFace.UP))

        var changed = pushItems() || pushItemsIntoMinecart()

        val event = HopperSearchItemEvent(this, false)
        Server.instance.pluginManager.callEvent(event)
        if (!event.cancelled) {
            changed = if (blockEntity is InventoryHolder || blockSide is BlockComposter) {
                pullItems(this, this) || changed
            } else {
                pullItemsFromMinecart() || pickupItems(this, this, pickupArea) || changed
            }
        }

        if (changed) {
            this.transferCooldown = (this.cooldownTick)
            setDirty()
        }

        return true
    }

    override val isObservable: Boolean
        get() = false

    fun pullItemsFromMinecart(): Boolean {
        if (inventory.isFull) {
            return false
        }

        if (minecartInvPickupFrom != null) {
            val inv: Inventory = minecartInvPickupFrom!!.inventory

            for (i in 0..<inv.size) {
                val item = inv.getItem(i)

                if (!item.isNothing) {
                    val itemToAdd = item.clone()
                    itemToAdd.count = 1
                    if (!inventory.canAddItem(itemToAdd)) continue

                    val ev = InventoryMoveItemEvent(
                        inv,
                        this.inventory,
                        this,
                        itemToAdd,
                        InventoryMoveItemEvent.Action.SLOT_CHANGE
                    )
                    Server.instance.pluginManager.callEvent(ev)
                    if (ev.cancelled) continue

                    val items = inventory.addItem(itemToAdd)
                    if (items.size >= 1) continue

                    item.count--
                    inv.setItem(i, item)

                    //归位为null
                    minecartInvPickupFrom = (null)
                    return true
                }
            }
        }

        return false
    }

    override fun close() {
        if (!closed) {
            for (player in HashSet(inventory.viewers)) {
                player.removeWindow(this.inventory)
            }
            super.close()
        }
    }

    override fun onBreak(isSilkTouch: Boolean) {
        for (content in inventory.contents.values) {
            level.dropItem(this.position, content)
        }
        inventory.clearAll()
    }


    fun pushItemsIntoMinecart(): Boolean {
        if (minecartInvPushTo != null) {
            val holderInventory: Inventory = minecartInvPushTo!!.inventory

            if (holderInventory.isFull) return false

            for (i in 0..<inventory.size) {
                val item = inventory.getItem(i)

                if (!item.isNothing) {
                    val itemToAdd = item.clone()
                    itemToAdd.setCount(1)

                    if (!holderInventory.canAddItem(itemToAdd)) continue

                    val ev = InventoryMoveItemEvent(
                        this.inventory, holderInventory,
                        this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE
                    )
                    Server.instance.pluginManager.callEvent(ev)

                    if (ev.cancelled) continue

                    val items = holderInventory.addItem(itemToAdd)

                    if (items.size > 0) continue

                    item.count--
                    inventory.setItem(i, item)

                    //归位为null
                    minecartInvPushTo = (null)
                    return true
                }
            }
        }

        return false
    }

    fun pushItems(): Boolean {
        if (inventory.isEmpty) {
            return false
        }

        val levelBlockState = levelBlockState
        if (!checkBlockStateValid(levelBlockState)) {
            return false
        }

        val side = BlockFace.fromIndex(levelBlockState.getPropertyValue(CommonBlockProperties.FACING_DIRECTION))
        val sidePos = this.getSide(side)
        val blockSide = sidePos.getLevelBlock(false)
        if (blockSide.isAir) return false
        val be = level.getBlockEntity(temporalVector.setComponentsAdding(this.position, side))

        //漏斗应该有主动向被锁住的漏斗推送物品的能力
        if (be is BlockEntityHopper && levelBlockState.isDefaultState && !be.isDisabled || be !is InventoryHolder && blockSide !is BlockComposter) {
            return false
        }

        var event: InventoryMoveItemEvent

        //Fix for furnace inputs
        if (be is BlockEntityFurnace) {
            val inventory = be.inventory
            if (inventory.isFull) {
                return false
            }

            var pushedItem = false

            for (i in 0..<this.inventory.size) {
                val item = this.inventory.getItem(i)
                if (!item.isNothing) {
                    val itemToAdd = item.clone()
                    itemToAdd.setCount(1)

                    //Check direction of hopper
                    if (this.block.getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION) == 0) {
                        val smelting = inventory.smelting
                        if (smelting.isNothing) {
                            event = InventoryMoveItemEvent(
                                this.inventory, inventory,
                                this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE
                            )
                            Server.instance.pluginManager.callEvent(event)

                            if (!event.cancelled) {
                                inventory.setSmelting(itemToAdd)
                                item.count--
                                pushedItem = true
                            }
                        } else if (inventory.smelting.id == itemToAdd.id && inventory.smelting.damage == itemToAdd.damage && inventory.smelting.id == itemToAdd.id && smelting.count < smelting.maxStackSize) {
                            event = InventoryMoveItemEvent(
                                this.inventory, inventory,
                                this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE
                            )
                            Server.instance.pluginManager.callEvent(event)

                            if (!event.cancelled) {
                                smelting.count++
                                inventory.setSmelting(smelting)
                                item.count--
                                pushedItem = true
                            }
                        }
                    } else if (Registries.FUEL.isFuel(itemToAdd)) {
                        val fuel = inventory.fuel
                        if (fuel.isNothing) {
                            event = InventoryMoveItemEvent(
                                this.inventory, inventory,
                                this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE
                            )
                            Server.instance.pluginManager.callEvent(event)

                            if (!event.cancelled) {
                                inventory.setFuel(itemToAdd)
                                item.count--
                                pushedItem = true
                            }
                        } else if (fuel.id == itemToAdd.id && fuel.damage == itemToAdd.damage && fuel.id == itemToAdd.id && fuel.count < fuel.maxStackSize) {
                            event = InventoryMoveItemEvent(
                                this.inventory, inventory,
                                this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE
                            )
                            Server.instance.pluginManager.callEvent(event)

                            if (!event.cancelled) {
                                fuel.count++
                                inventory.setFuel(fuel)
                                item.count--
                                pushedItem = true
                            }
                        }
                    }

                    if (pushedItem) {
                        this.inventory.setItem(i, item)
                    }
                }
            }

            return pushedItem
        } else if (be is BlockEntityBrewingStand) {
            val inventory = be.inventory
            if (inventory.isFull) {
                return false
            }

            var pushedItem = false

            for (i in 0..<this.inventory.size) {
                val item = this.inventory.getItem(i)
                if (!item.isNothing) {
                    val itemToAdd = item.clone()
                    itemToAdd.setCount(1)

                    //Check direction of hopper
                    if (this.block.getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION) == 0) {
                        val ingredient = inventory.ingredient
                        if (ingredient.isNothing) {
                            event = InventoryMoveItemEvent(
                                this.inventory, inventory,
                                this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE
                            )
                            Server.instance.pluginManager.callEvent(event)

                            if (!event.cancelled) {
                                inventory.ingredient = itemToAdd
                                item.count--
                                pushedItem = true
                            }
                        } else if (ingredient.id == itemToAdd.id && ingredient.damage == itemToAdd.damage && ingredient.id == itemToAdd.id && ingredient.count < ingredient.maxStackSize) {
                            event = InventoryMoveItemEvent(
                                this.inventory, inventory,
                                this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE
                            )
                            Server.instance.pluginManager.callEvent(event)

                            if (!event.cancelled) {
                                ingredient.count++
                                inventory.ingredient = ingredient
                                item.count--
                                pushedItem = true
                            }
                        }
                    } else if (itemToAdd is ItemPotion || itemToAdd is ItemSplashPotion) {
                        val productView = be.productView
                        if (productView.canAddItem(itemToAdd)) {
                            for (j in 1..3) {
                                if (inventory.getItem(j).isNothing) {
                                    inventory.setItem(j, itemToAdd)
                                    item.count--
                                    pushedItem = true
                                    break
                                }
                            }
                        }
                    }

                    if (pushedItem) {
                        this.inventory.setItem(i, item)
                    }
                }
            }

            return pushedItem
        } else if (blockSide is BlockComposter) {
            if (blockSide.isFull) {
                return false
            }

            for (i in 0..<inventory.size) {
                val item = inventory.getItem(i)

                if (item.isNothing) {
                    continue
                }

                val itemToAdd = item.clone()
                itemToAdd.setCount(1)

                if (!blockSide.onActivate(item, null, BlockFace.DOWN, 0f, 0f, 0f)) {
                    return false
                }
                item.count--
                inventory.setItem(i, item)
                return true
            }
        } else {
            val inventory = if (be is RecipeInventoryHolder) be.ingredientView else (be as InventoryHolder).inventory

            if (inventory!!.isFull) {
                return false
            }

            for (i in 0..<this.inventory.size) {
                val item = this.inventory.getItem(i)

                if (!item.isNothing) {
                    val itemToAdd = item.clone()
                    itemToAdd.setCount(1)

                    if (!inventory.canAddItem(itemToAdd)) {
                        continue
                    }

                    val ev = InventoryMoveItemEvent(
                        this.inventory,
                        inventory,
                        this,
                        itemToAdd,
                        InventoryMoveItemEvent.Action.SLOT_CHANGE
                    )
                    Server.instance.pluginManager.callEvent(ev)

                    if (ev.cancelled) {
                        continue
                    }

                    val items = inventory.addItem(itemToAdd)

                    if (items.size > 0) {
                        continue
                    }

                    item.count--
                    this.inventory.setItem(i, item)
                    return true
                }
            }
        }

        return false
    }

    override val spawnCompound: CompoundTag
        get() {
            val c = super.spawnCompound.putBoolean("isMovable", this.isMovable)

            if (this.hasName()) {
                c.put("CustomName", namedTag["CustomName"]!!)
            }

            return c
        }
}