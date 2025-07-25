package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockCampfire
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.event.inventory.CampfireSmeltEvent
import org.chorus_oss.chorus.inventory.CampfireInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.recipe.CampfireRecipe
import java.util.concurrent.ThreadLocalRandom

class BlockEntityCampfire(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt),
    BlockEntityInventoryHolder {
    override lateinit var inventory: Inventory
    private lateinit var burnTime: IntArray
    private lateinit var recipes: Array<CampfireRecipe?>
    private lateinit var keepItem: BooleanArray


    override fun initBlockEntity() {
        super.initBlockEntity()
        scheduleUpdate()
    }

    override fun loadNBT() {
        super.loadNBT()
        this.inventory = CampfireInventory(this)
        this.burnTime = IntArray(4)
        this.recipes = arrayOfNulls(4)
        this.keepItem = BooleanArray(4)
        for (i in 1..burnTime.size) {
            burnTime[i - 1] = namedTag.getInt("ItemTime$i")
            keepItem[i - 1] = namedTag.getBoolean("KeepItem" + 1)

            if (namedTag.contains("Item$i") && namedTag["Item$i"] is CompoundTag) {
                inventory.setItem(i - 1, NBTIO.getItemHelper(namedTag.getCompound("Item$i")))
            }
        }
    }

    override fun onUpdate(): Boolean {
        var needsUpdate = false
        val block = block
        val isLit = block is BlockCampfire && !block.isExtinguished
        for (slot in 0..<inventory.size) {
            val item = inventory.getItem(slot)
            if (item.isNothing) {
                burnTime[slot] = 0
                recipes[slot] = null
            } else if (!keepItem[slot]) {
                var recipe = recipes[slot]
                if (recipe == null) {
                    recipe = Server.instance.recipeRegistry.findCampfireRecipe(item)
                    if (recipe == null) {
                        inventory.setItem(slot, Item.AIR)
                        val random = ThreadLocalRandom.current()
                        level.dropItem(
                            position.add(random.nextFloat().toDouble(), 0.5, random.nextFloat().toDouble()),
                            item
                        )
                        burnTime[slot] = 0
                        recipes[slot] = null
                        continue
                    } else {
                        burnTime[slot] = 600
                        recipes[slot] = recipe
                    }
                }

                val burnTimeLeft = burnTime[slot]
                if (burnTimeLeft <= 0) {
                    val product = Item.get(recipe.result.id, recipe.result.damage, item.getCount())
                    val event = CampfireSmeltEvent(this, item, product)
                    if (!event.cancelled) {
                        inventory.setItem(slot, Item.AIR)
                        val random = ThreadLocalRandom.current()
                        level.dropItem(
                            position.add(random.nextFloat().toDouble(), 0.5, random.nextFloat().toDouble()),
                            event.result
                        )
                        burnTime[slot] = 0
                        recipes[slot] = null
                    } else if (event.keepItem) {
                        keepItem[slot] = true
                        burnTime[slot] = 0
                        recipes[slot] = null
                    }
                } else if (isLit) {
                    burnTime[slot]--
                    needsUpdate = true
                } else {
                    burnTime[slot] = 600
                }
            }
        }

        return needsUpdate
    }

    fun getKeepItem(slot: Int): Boolean {
        if (slot < 0 || slot >= keepItem.size) {
            return false
        }
        return keepItem[slot]
    }

    fun setKeepItem(slot: Int, keep: Boolean) {
        if (slot < 0 || slot >= keepItem.size) {
            return
        }
        keepItem[slot] = keep
    }

    override fun saveNBT() {
        super.saveNBT()
        for (i in 1..burnTime.size) {
            val item = inventory.getItem(i - 1)
            if (item.isNothing) {
                namedTag.remove("Item$i")
                namedTag.putInt("ItemTime$i", 0)
                namedTag.remove("KeepItem$i")
            } else {
                namedTag.putCompound("Item$i", NBTIO.putItemHelper(item))
                namedTag.putInt("ItemTime$i", burnTime[i - 1])
                namedTag.putBoolean("KeepItem$i", keepItem[i - 1])
            }
        }
    }

    fun setRecipe(index: Int, recipe: CampfireRecipe?) {
        recipes[index] = recipe
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
    }

    override var name: String
        get() = if (this.hasName()) namedTag.getString("CustomName") else "Campfire"
        set(name) {
            if (name.isNullOrEmpty()) {
                namedTag.remove("CustomName")
                return
            }
            namedTag.putString("CustomName", name)
        }

    override fun hasName(): Boolean {
        return namedTag.contains("CustomName")
    }

    override val spawnCompound: CompoundTag
        get() {
            val c = super.spawnCompound

            for (i in 1..burnTime.size) {
                val item = inventory.getItem(i - 1)
                if (item.isNothing) {
                    c.remove("Item$i")
                } else {
                    c.putCompound("Item$i", NBTIO.putItemHelper(item))
                }
            }

            return c
        }

    override val isBlockEntityValid: Boolean
        get() = block.id === BlockID.CAMPFIRE

    val size: Int
        get() = 4

    fun getItem(index: Int): Item {
        if (index < 0 || index >= size) {
            return Item.AIR
        } else {
            val data = namedTag.getCompound("Item" + (index + 1))
            return NBTIO.getItemHelper(data)
        }
    }

    fun setItem(index: Int, item: Item) {
        if (index < 0 || index >= size) {
            return
        }

        val nbt = NBTIO.putItemHelper(item)
        namedTag.putCompound("Item" + (index + 1), nbt)
    }
}
