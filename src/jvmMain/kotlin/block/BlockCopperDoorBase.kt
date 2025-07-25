package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.enums.OxidizationLevel
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.registry.Registries

abstract class BlockCopperDoorBase(blockState: BlockState) : BlockDoor(blockState), Oxidizable,
    Waxable {
    override val hardness: Double
        get() = 3.0

    override val resistance: Double
        get() = 3.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_STONE

    override fun onUpdate(type: Int): Int {
        super<BlockDoor>.onUpdate(type)
        return super<Oxidizable>.onUpdate(type)
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (player != null) {
            if (player.isSneaking()) {
                return super<Waxable>.onActivate(item, player, blockFace, fx, fy, fz)
                        || super<Oxidizable>.onActivate(item, player, blockFace, fx, fy, fz)
            }
        }

        return super<BlockDoor>.onActivate(item, player, blockFace, fx, fy, fz)
    }

    override fun getBlockWithOxidizationLevel(oxidizationLevel: OxidizationLevel): Block {
        return Registries.BLOCK.getBlockProperties(getCopperId(isWaxed, oxidizationLevel)).defaultState.toBlock()
    }

    override fun setOxidizationLevel(oxidizationLevel: OxidizationLevel): Boolean {
        if (this.oxidizationLevel == oxidizationLevel) {
            return true
        }
        return level.setBlock(this.position, get(getCopperId(isWaxed, oxidizationLevel)))
    }

    override fun setWaxed(waxed: Boolean): Boolean {
        if (isWaxed == waxed) {
            return true
        }
        return level.setBlock(
            this.position, get(
                getCopperId(
                    waxed,
                    oxidizationLevel
                )
            )
        )
    }

    override val isWaxed: Boolean
        get() = false


    protected fun getCopperId(waxed: Boolean, oxidizationLevel: OxidizationLevel?): String {
        if (oxidizationLevel == null) {
            return id
        }
        return when (oxidizationLevel) {
            OxidizationLevel.UNAFFECTED -> if (waxed) BlockID.WAXED_COPPER_DOOR else BlockID.COPPER_DOOR
            OxidizationLevel.EXPOSED -> if (waxed) BlockID.WAXED_EXPOSED_COPPER_DOOR else BlockID.EXPOSED_COPPER_DOOR
            OxidizationLevel.WEATHERED -> if (waxed) BlockID.WAXED_WEATHERED_COPPER_DOOR else BlockID.WEATHERED_COPPER_DOOR
            OxidizationLevel.OXIDIZED -> if (waxed) BlockID.WAXED_OXIDIZED_COPPER_DOOR else BlockID.OXIDIZED_COPPER_DOOR
        }
    }
}
