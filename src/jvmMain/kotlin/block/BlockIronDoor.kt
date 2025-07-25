package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace

class BlockIronDoor @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDoor(blockstate) {
    override val name: String
        get() = "Iron Door Block"

    override val hardness: Double
        get() = 5.0

    override val resistance: Double
        get() = 25.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        return false
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun playOpenSound() {
        level.addSound(this.position, Sound.OPEN_IRON_DOOR)
    }

    override fun playCloseSound() {
        level.addSound(this.position, Sound.CLOSE_IRON_DOOR)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.IRON_DOOR,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.OPEN_BIT,
            CommonBlockProperties.UPPER_BLOCK_BIT,
            CommonBlockProperties.DOOR_HINGE_BIT
        )
    }
}