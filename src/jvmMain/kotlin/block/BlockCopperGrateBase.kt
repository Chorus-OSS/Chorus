package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.enums.OxidizationLevel
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.registry.Registries

abstract class BlockCopperGrateBase(blockState: BlockState) : BlockFlowable(blockState), Oxidizable,
    Waxable {
    override val hardness: Double
        get() = 3.0

    override val resistance: Double
        get() = 6.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_STONE

    override fun canPassThrough(): Boolean {
        return false
    }

    override fun breaksWhenMoved(): Boolean {
        return false
    }

    override fun canBeFlowedInto(): Boolean {
        return false
    }

    override fun isSolid(side: BlockFace): Boolean {
        return true
    }

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
        for (p in level.getPlayers().values) {
            // Check if a player's position matches the target block's position
            if (p.position.floor().equals(block.position.floor())) {
                // Prevent block placement if a player is found at the target position
                return false
            }
        }

        val down = this.down()
        if (!down.isAir) {
            level.setBlock(block.position, this, true, true)
            return true
        }
        return false
    }

    override val waterloggingLevel: Int
        get() = 1

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        return super<Waxable>.onActivate(item, player, blockFace, fx, fy, fz)
                || super<Oxidizable>.onActivate(item, player, blockFace, fx, fy, fz)
    }

    override fun onUpdate(type: Int): Int {
        return super<Oxidizable>.onUpdate(type)
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun canHarvestWithHand(): Boolean {
        return false
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
            OxidizationLevel.UNAFFECTED -> if (waxed) BlockID.WAXED_COPPER_GRATE else BlockID.COPPER_GRATE
            OxidizationLevel.EXPOSED -> if (waxed) BlockID.WAXED_EXPOSED_COPPER_GRATE else BlockID.EXPOSED_COPPER_GRATE
            OxidizationLevel.WEATHERED -> if (waxed) BlockID.WAXED_WEATHERED_COPPER_GRATE else BlockID.WEATHERED_COPPER_GRATE
            OxidizationLevel.OXIDIZED -> if (waxed) BlockID.WAXED_OXIDIZED_COPPER_GRATE else BlockID.OXIDIZED_COPPER_GRATE
        }
    }
}
