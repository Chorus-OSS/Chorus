package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool

class BlockObsidian(blockState: BlockState = properties.defaultState) : BlockSolid(blockState) {
    override val name: String
        get() = "Obsidian"

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_DIAMOND

    override val hardness: Double
        get() = 35.0 //TODO Should be 50 but the break time calculation is broken

    override val resistance: Double
        get() = 6000.0

    override fun onBreak(item: Item?): Boolean {
        //destroy the nether portal
        val nearby = arrayOf(
            this.up(), this.down(),
            this.north(), south(),
            this.west(), this.east(),
        )
        for (aNearby in nearby) {
            if (aNearby != null && aNearby.id == BlockID.PORTAL) {
                aNearby.onBreak(item)
            }
        }
        return super.onBreak(item)
    }

    override fun afterRemoval(newBlock: Block, update: Boolean) {
        if (update) {
            onBreak(Item.AIR)
        }
    }

    override fun canBePushed(): Boolean {
        return false
    }

    override fun canBePulled(): Boolean {
        return false
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.OBSIDIAN)
    }
}
