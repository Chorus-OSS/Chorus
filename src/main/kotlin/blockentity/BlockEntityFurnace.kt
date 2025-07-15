package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.event.inventory.FurnaceBurnEvent
import org.chorus_oss.chorus.event.inventory.FurnaceSmeltEvent
import org.chorus_oss.chorus.inventory.*
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemLavaBucket
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.recipe.SmeltingRecipe
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.ceil
import kotlin.math.floor

open class BlockEntityFurnace(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt),
    RecipeInventoryHolder,
    BlockEntityInventoryHolder {
    override lateinit var inventory: SmeltingInventory

    var burnTime: Int = 0
    var burnDuration: Int = 0
    var cookTime: Int = 0
    var maxTime: Int = 0

    @JvmField
    var storedXP: Float = 0f
    private var crackledTime = 0

    protected open fun createInventory(): SmeltingInventory {
        return FurnaceTypeInventory(this)
    }

    override fun initBlockEntity() {
        super.initBlockEntity()
        if (burnTime > 0) {
            this.scheduleUpdate()
        }
    }

    override fun loadNBT() {
        super.loadNBT()
        this.inventory = createInventory()

        if (!namedTag.contains("Items") || namedTag["Items"] !is ListTag<*>) {
            namedTag.putList("Items", ListTag<CompoundTag>())
        }

        for (i in 0..<this.size) {
            inventory.setItem(i, this.getItem(i))
        }

        burnTime =
            if (!namedTag.contains("BurnTime") || namedTag.getShort("BurnTime") < 0) {
                0
            } else {
                namedTag.getShort("BurnTime").toInt()
            }

        cookTime =
            if (!namedTag.contains("CookTime") || namedTag.getShort("CookTime") < 0 || (namedTag.getShort(
                    "BurnTime"
                ).toInt() == 0 && namedTag.getShort("CookTime") > 0)
            ) {
                0
            } else {
                namedTag.getShort("CookTime").toInt()
            }

        burnDuration =
            if (!namedTag.contains("BurnDuration") || namedTag.getShort("BurnDuration") < 0) {
                0
            } else {
                namedTag.getShort("BurnDuration").toInt()
            }

        if (!namedTag.contains("MaxTime")) {
            maxTime = burnTime
            burnDuration = 0
        } else {
            maxTime = namedTag.getShort("MaxTime").toInt()
        }

        if (namedTag.contains("BurnTicks")) {
            burnDuration = namedTag.getShort("BurnTicks").toInt()
            namedTag.remove("BurnTicks")
        }

        storedXP = if (namedTag.contains("StoredXpInt")) {
            namedTag.getShort("StoredXpInt").toFloat()
        } else {
            0f
        }
    }

    protected open val furnaceName: String
        get() = "Furnace"

    protected open val clientName: String
        get() = BlockEntityID.FURNACE

    override var name: String
        get() = if (this.hasName()) namedTag.getString("CustomName") else furnaceName
        set(name) {
            if (name == null || name == "") {
                namedTag.remove("CustomName")
                return
            }

            namedTag.putString("CustomName", name)
        }

    override fun hasName(): Boolean {
        return namedTag.contains("CustomName")
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
        val xp = calculateXpDrop()
        if (xp > 0) {
            storedXP = 0f
            level.dropExpOrb(this.position, xp.toInt())
        }
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putList("Items", ListTag<CompoundTag>())
        for (index in 0..<this.size) {
            this.setItem(index, inventory.getItem(index))
        }
        namedTag.putShort("CookTime", cookTime)
        namedTag.putShort("BurnTime", burnTime)
        namedTag.putShort("BurnDuration", burnDuration)
        namedTag.putShort("MaxTime", maxTime)
        namedTag.putShort("StoredXpInt", storedXP.toInt())
    }

    override val isBlockEntityValid: Boolean
        get() {
            val blockId = block.id
            return blockId === idleBlockId || blockId === burningBlockId
        }

    val size: Int
        get() = 3

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

        if (item.isNothing) {
            if (i >= 0) {
                namedTag.getList("Items").all.toMutableList().removeAt(i)
            }
        } else if (i < 0) {
            (namedTag.getList("Items", CompoundTag::class.java)).add(d)
        } else {
            (namedTag.getList("Items", CompoundTag::class.java)).add(i, d)
        }
    }

    protected open val idleBlockId: String
        get() = BlockID.FURNACE

    protected open val burningBlockId: String
        get() = BlockID.LIT_FURNACE

    protected fun setBurning(burning: Boolean) {
        if (burning) {
            if (this.block.id == this.idleBlockId) {
                level.setBlock(
                    this.position, Block.getWithState(
                        burningBlockId, this.block.blockState
                    ), true
                )
            }
        } else if (this.block.id == this.burningBlockId) {
            level.setBlock(
                this.position, Block.getWithState(
                    idleBlockId, this.block.blockState
                ), true
            )
        }
    }

    protected fun checkFuel(fuel: Item) {
        var fuel1 = fuel
        val ev = FurnaceBurnEvent(this, fuel1, if (fuel1.fuelTime == null) 0 else fuel1.fuelTime!!)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            return
        }

        maxTime = ceil((ev.burnTime / speedMultiplier.toFloat()).toDouble()).toInt()
        burnTime = ceil((ev.burnTime / speedMultiplier.toFloat()).toDouble()).toInt()
        burnDuration = 0
        setBurning(true)

        if (burnTime > 0 && ev.isBurning) {
            fuel1.setCount(fuel1.getCount() - 1)
            if (fuel1.getCount() == 0) {
                if (fuel1 is ItemLavaBucket) {
                    fuel1.damage = (0)
                    fuel1.setCount(1)
                } else {
                    fuel1 = Item.AIR
                }
            }
            inventory.setFuel(fuel1)
        }
    }

    protected open fun matchRecipe(raw: Item): SmeltingRecipe? {
        return Server.instance.recipeRegistry.findFurnaceRecipe(raw)
    }

    protected open val speedMultiplier: Int
        get() = 1

    override fun onUpdate(): Boolean {
        if (this.closed) {
            return false
        }

        var ret = false
        val fuel = inventory.fuel
        var raw = inventory.smelting
        var product = inventory.result
        val smelt = matchRecipe(raw)

        var canSmelt = false
        if (smelt != null) {
            canSmelt = (raw.getCount() > 0 && ((smelt.result.equals(
                product,
                true
            ) && product.getCount() < product.maxStackSize) || product.id === BlockID.AIR))
            //检查输入
            if (!smelt.input.toItem().equals(raw, true, false)) {
                canSmelt = false
            }
        }

        if (burnTime <= 0 && canSmelt && fuel.fuelTime != null && fuel.getCount() > 0) {
            this.checkFuel(fuel)
        }

        if (burnTime > 0) {
            burnTime--
            val readyAt = 200 / speedMultiplier
            burnDuration = ceil((burnTime.toFloat() / maxTime * readyAt).toDouble()).toInt()

            if (crackledTime-- <= 0) {
                this.crackledTime = ThreadLocalRandom.current().nextInt(20, 100)
                level.addSound(position.add(0.5, 0.5, 0.5), Sound.BLOCK_FURNACE_LIT)
            }

            if (smelt != null && canSmelt) {
                cookTime++
                if (cookTime >= readyAt) {
                    val count = product.getCount() + 1
                    product = smelt.result.clone()
                    product.setCount(count)

                    val ev = FurnaceSmeltEvent(
                        this, raw, product,
                        Server.instance.recipeRegistry.getRecipeXp(smelt).toFloat()
                    )
                    Server.instance.pluginManager.callEvent(ev)
                    if (!ev.cancelled) {
                        inventory.setResult(ev.result)
                        raw.setCount(raw.getCount() - 1)
                        if (raw.getCount() == 0) {
                            raw = Item.AIR
                        }
                        this.storedXP += ev.xp
                        inventory.setSmelting(raw)
                    }

                    cookTime -= readyAt
                }
            } else if (burnTime <= 0) {
                burnTime = 0
                cookTime = 0
                burnDuration = 0
            } else {
                cookTime = 0
            }
            ret = true
        } else {
            setBurning(false)
            burnTime = 0
            cookTime = 0
            burnDuration = 0
            this.crackledTime = 0
        }

        for (player in inventory.viewers) {
            val windowId = player.getWindowId(this.inventory)
            if (windowId > 0) {
                var pk = org.chorus_oss.protocol.packets.ContainerSetDataPacket(
                    containerID = windowId.toByte(),
                    property = org.chorus_oss.protocol.packets.ContainerSetDataPacket.FURNACE_TICK_COUNT,
                    value = cookTime,
                )
                player.sendPacket(pk)

                pk = org.chorus_oss.protocol.packets.ContainerSetDataPacket(
                    containerID = windowId.toByte(),
                    property = org.chorus_oss.protocol.packets.ContainerSetDataPacket.FURNACE_LIT_TIME,
                    value = burnDuration,
                )
                player.sendPacket(pk)
            }
        }

        return ret
    }

    override val spawnCompound: CompoundTag
        get() {
            val c = super.spawnCompound
                .putBoolean("isMovable", this.isMovable)
                .putShort("BurnDuration", burnDuration)
                .putShort("BurnTime", burnTime)
                .putShort("CookTime", cookTime)
                .putShort("StoredXpInt", storedXP.toInt())
            if (this.hasName()) {
                c.put("CustomName", namedTag["CustomName"]!!)
            }
            return c
        }

    fun calculateXpDrop(): Short {
        return (floor(storedXP.toDouble()) + (if (ThreadLocalRandom.current()
                .nextFloat() < (this.storedXP % 1)
        ) 1 else 0)).toInt().toShort()
    }

    override val ingredientView: Inventory?
        get() = InventorySlice(inventory, 0, 1)

    override val productView: Inventory?
        get() = InventorySlice(inventory, 2, 3)
}
