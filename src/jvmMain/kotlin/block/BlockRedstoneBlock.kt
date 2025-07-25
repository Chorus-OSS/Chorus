package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.utils.RedstoneComponent

class BlockRedstoneBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate), RedstoneComponent {
    override val resistance: Double
        get() = 10.0

    override val hardness: Double
        get() = 5.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val name: String
        get() = "Redstone Block"

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

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
        if (super.place(item, block, target, face, fx, fy, fz, player)) {
            updateAroundRedstone()

            return true
        }
        return false
    }

    override fun onBreak(item: Item?): Boolean {
        if (!super.onBreak(item)) {
            return false
        }

        updateAroundRedstone()
        return true
    }

    override val isPowerSource: Boolean
        get() = true

    override fun getWeakPower(face: BlockFace): Int {
        return 15
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.REDSTONE_BLOCK)
    }
}