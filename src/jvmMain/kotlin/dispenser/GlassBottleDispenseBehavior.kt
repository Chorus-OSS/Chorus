package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.block.BlockDispenser
import org.chorus_oss.chorus.block.BlockFlowingWater
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.math.BlockFace

class GlassBottleDispenseBehavior : DefaultDispenseBehavior() {
    override fun dispense(block: BlockDispenser, face: BlockFace, item: Item): Item? {
        val target = block.getSide(face)
        if (target is BlockFlowingWater && target.liquidDepth == 0) return Item.get(ItemID.POTION)
        return super.dispense(block, face, item)
    }
}
