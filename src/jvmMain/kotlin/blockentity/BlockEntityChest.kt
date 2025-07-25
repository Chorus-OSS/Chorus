package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.inventory.ChestInventory
import org.chorus_oss.chorus.inventory.DoubleChestInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.function.Consumer


class BlockEntityChest(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnableContainer(chunk, nbt) {
    protected var doubleInventory: DoubleChestInventory? = null

    override fun close() {
        if (!closed) {
            unpair()
            inventory.viewers.forEach(Consumer { p: Player -> p.removeWindow(this.inventory) })
            realInventory.viewers.forEach(Consumer { p: Player ->
                p.removeWindow(
                    realInventory
                )
            })

            this.closed = true
            chunk.removeBlockEntity(this)
            level.removeBlockEntity(this)
        }
    }

    override val isBlockEntityValid: Boolean
        get() {
            val blockId = this.block.id
            return blockId == BlockID.CHEST || blockId == BlockID.TRAPPED_CHEST
        }

    val size: Int
        get() = if (this.doubleInventory != null) doubleInventory!!.size else inventory.size

    override var inventory: Inventory = ChestInventory(this)
        set(_) = Unit
        get() {
            if (this.doubleInventory == null && this.isPaired) {
                this.checkPairing()
            }

            return doubleInventory ?: field
        }

    val realInventory: ChestInventory
        get() = inventory as ChestInventory

    protected fun checkPairing() {
        val pair = this.pair

        if (pair != null) {
            if (!pair.isPaired) {
                pair.pairWith(this)
                this.pairWith(pair)
            }

            if (pair.doubleInventory != null) {
                this.doubleInventory = pair.doubleInventory
                namedTag.putBoolean("pairlead", false)
            } else if (this.doubleInventory == null) {
                namedTag.putBoolean("pairlead", true)
                if ((pair.position.x + (pair.position.z.toInt() shl 15)) > (position.x + (position.z.toInt() shl 15))) { //Order them correctly
                    this.doubleInventory = DoubleChestInventory(pair, this)
                } else {
                    this.doubleInventory = DoubleChestInventory(this, pair)
                }
            }
        } else {
            if (level.isChunkLoaded(namedTag.getInt("pairx") shr 4, namedTag.getInt("pairz") shr 4)) {
                this.doubleInventory = null
                namedTag.remove("pairx")
                namedTag.remove("pairz")
                namedTag.remove("pairlead")
            }
        }
    }

    val isPaired: Boolean
        get() = namedTag.contains("pairx") && namedTag.contains("pairz")

    val pair: BlockEntityChest?
        get() {
            if (this.isPaired) {
                val blockEntity = level
                    .getBlockEntityIfLoaded(
                        Vector3(
                            namedTag.getInt("pairx").toDouble(),
                            position.y,
                            namedTag.getInt("pairz").toDouble()
                        )
                    )
                if (blockEntity is BlockEntityChest) {
                    return blockEntity
                }
            }

            return null
        }

    fun pairWith(chest: BlockEntityChest): Boolean {
        if ((this.isPaired || chest.isPaired)) {
            val x1 = namedTag.getInt("pairx")
            val x2 = chest.namedTag.getInt("pairx")

            val z1 = namedTag.getInt("pairz")
            val z2 = chest.namedTag.getInt("pairz")

            if (!(chest.isPaired && (position.x == x2.toDouble() && position.z == z2.toDouble())) || !(this.isPaired && (chest.position.x == x1.toDouble() && chest.position.z == z1.toDouble()))) {
                return false
            }
        }

        this.createPair(chest)
        this.checkPairing()

        chest.spawnToAll()
        this.spawnToAll()

        return true
    }

    fun createPair(chest: BlockEntityChest) {
        namedTag.putInt("pairx", chest.position.x.toInt())
        namedTag.putInt("pairz", chest.position.z.toInt())
        chest.namedTag.putInt("pairx", position.x.toInt())
        chest.namedTag.putInt("pairz", position.z.toInt())
    }

    fun unpair(): Boolean {
        if (!this.isPaired) {
            return false
        }
        val chest = this.pair

        this.doubleInventory = null
        namedTag.remove("pairx")
        namedTag.remove("pairz")

        this.spawnToAll()

        if (chest != null) {
            chest.namedTag.remove("pairx")
            chest.namedTag.remove("pairz")
            chest.doubleInventory = null
            chest.checkPairing()
            chest.spawnToAll()
        }
        this.checkPairing()

        return true
    }

    override val spawnCompound: CompoundTag
        get() {
            val spawnCompound = super.spawnCompound
                .putBoolean("isMovable", this.isMovable)
            if (this.isPaired) {
                spawnCompound.putBoolean("pairlead", namedTag.getBoolean("pairlead"))
                    .putInt("pairx", namedTag.getInt("pairx"))
                    .putInt("pairz", namedTag.getInt("pairz"))
            }
            if (this.hasName()) {
                spawnCompound.put("CustomName", namedTag["CustomName"]!!)
            }
            return spawnCompound
        }

    override val cleanedNBT: CompoundTag
        get() = super.cleanedNBT!!.remove("pairx").remove("pairz")

    override var name: String
        get() = if (this.hasName()) namedTag.getString("CustomName") else "Chest"
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

    override fun onBreak(isSilkTouch: Boolean) {
        unpair()
        super.onBreak(isSilkTouch)
    }

    init {
        isMovable = true
        loadNBT()
    }
}
