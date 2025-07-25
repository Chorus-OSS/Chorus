package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityHopper
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.event.inventory.InventoryMoveItemEvent
import org.chorus_oss.chorus.inventory.ContainerInventory.Companion.calculateRedstone
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.inventory.RecipeInventoryHolder
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.RedstoneComponent

class BlockHopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), RedstoneComponent, Faceable, BlockEntityHolder<BlockEntityHopper> {

    override fun getBlockEntityClass() = BlockEntityHopper::class.java

    override fun getBlockEntityType(): String {
        return BlockEntityID.HOPPER
    }

    override val name: String
        get() = "Hopper Block"

    override val hardness: Double
        get() = 3.0

    override val resistance: Double
        get() = 24.0

    override val waterloggingLevel: Int
        get() = 1

    override fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        var facing = face.getOpposite()

        if (facing == BlockFace.UP) {
            facing = BlockFace.DOWN
        }

        blockFace = facing

        if (Server.instance.settings.levelSettings.enableRedstone) {
            val powered = this.isGettingPower

            if (powered == this.isEnabled) {
                this.isEnabled = !powered
            }
        }

        val nbt = CompoundTag().putList("Items", ListTag<Tag<*>>())
        return BlockEntityHolder.setBlockAndCreateEntity(this, direct = true, update = true, initialData = nbt) != null
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (isNotActivate(player)) return false

        val blockEntity = getOrCreateBlockEntity()

        return player?.addWindow(blockEntity.inventory) != -1
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() {
            val blockEntity = blockEntity

            if (blockEntity != null) {
                return calculateRedstone(blockEntity.inventory)
            }

            return super.comparatorInputOverride
        }

    var isEnabled: Boolean
        get() = !getPropertyValue(CommonBlockProperties.TOGGLE_BIT)
        set(enabled) {
            setPropertyValue(CommonBlockProperties.TOGGLE_BIT, !enabled)
        }

    override fun onUpdate(type: Int): Int {
        if (!Server.instance.settings.levelSettings.enableRedstone) {
            return 0
        }

        if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) {
            val disabled = level.isBlockPowered(this.position)

            if (disabled == this.isEnabled) {
                this.isEnabled = !disabled
                level.setBlock(this.position, this, direct = false, update = true)
                val be = blockEntity
                if (be != null) {
                    be.isDisabled = disabled
                    if (!disabled) {
                        be.scheduleUpdate()
                    }
                }
            }

            return type
        }

        return 0
    }

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override var blockFace: BlockFace
        get() = fromIndex(getPropertyValue(CommonBlockProperties.FACING_DIRECTION))
        set(face) {
            setPropertyValue(CommonBlockProperties.FACING_DIRECTION, face.index)
        }

    override fun isSolid(side: BlockFace): Boolean {
        return side == BlockFace.UP
    }

    interface IHopper {
        val locator: Locator

        fun pullItems(hopperHolder: InventoryHolder, hopperPos: Locator): Boolean {
            val hopperInv = hopperHolder.inventory

            if (hopperInv.isFull) return false

            val blockSide = hopperPos.getSide(BlockFace.UP).tickCachedLevelBlock
            val blockEntity =
                hopperPos.level.getBlockEntity(Vector3().setComponentsAdding(hopperPos.position, BlockFace.UP))

            if (blockEntity is InventoryHolder) {
                val inv = if (blockEntity is RecipeInventoryHolder) blockEntity.productView!! else blockEntity.inventory

                for (i in 0..<inv.size) {
                    val item = inv.getItem(i)

                    if (!item.isNothing) {
                        val itemToAdd: Item = item.clone()
                        itemToAdd.count = 1

                        if (!hopperInv.canAddItem(itemToAdd)) continue

                        val ev = InventoryMoveItemEvent(
                            inv,
                            hopperInv,
                            hopperHolder,
                            itemToAdd,
                            InventoryMoveItemEvent.Action.SLOT_CHANGE
                        )
                        Server.instance.pluginManager.callEvent(ev)

                        if (ev.cancelled) continue

                        val items: Array<Item> = hopperInv.addItem(itemToAdd)

                        if (items.isNotEmpty()) continue

                        item.count--

                        inv.setItem(i, item)
                        return true
                    }
                }
            } else if (blockSide is BlockComposter) {
                if (blockSide.isFull) {
                    //检查是否能输入
                    if (!hopperInv.canAddItem(blockSide.outPutItem)) return false

                    //Will call BlockComposterEmptyEvent
                    val item = blockSide.empty()

                    if (item == null || item.isNothing) return false

                    val itemToAdd: Item = item.clone()
                    itemToAdd.count = 1

                    val items: Array<Item> = hopperInv.addItem(itemToAdd)

                    return items.size < 1
                }
            }
            return false
        }

        fun pickupItems(hopperHolder: InventoryHolder, hopperPos: Locator, pickupArea: AxisAlignedBB): Boolean {
            val hopperInv = hopperHolder.inventory

            if (hopperInv.isFull) return false

            var pickedUpItem = false

            for (entity in hopperPos.level.getCollidingEntities(pickupArea)) {
                if (entity.isClosed() || entity !is EntityItem) continue

                val item = entity.item

                if (item.isNothing || !hopperInv.canAddItem(item)) continue

                val originalCount = item.getCount()

                val ev = InventoryMoveItemEvent(
                    hopperInv, // TODO: FROM
                    hopperInv, hopperHolder,
                    item, InventoryMoveItemEvent.Action.PICKUP
                )
                Server.instance.pluginManager.callEvent(ev)

                if (ev.cancelled) continue

                val items: Array<Item> = hopperInv.addItem(item)

                if (items.isEmpty()) {
                    entity.close()
                    pickedUpItem = true
                    continue
                }

                if (items[0].getCount() != originalCount) {
                    pickedUpItem = true
                    item.setCount(items[0].getCount())
                }
            }

            return pickedUpItem
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(
                BlockID.HOPPER,
                CommonBlockProperties.FACING_DIRECTION,
                CommonBlockProperties.TOGGLE_BIT
            )
    }
}
