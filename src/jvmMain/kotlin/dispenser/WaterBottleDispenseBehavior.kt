package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockDispenser
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.effect.PotionType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemPotion
import org.chorus_oss.chorus.math.BlockFace

class WaterBottleDispenseBehavior : DefaultDispenseBehavior() {
    override fun dispense(block: BlockDispenser, face: BlockFace, item: Item): Item? {
        if ((item as ItemPotion).potion !== PotionType.WATER) return super.dispense(block, face, item)
        val targetId = block.getSide(face).id
        if (targetId === BlockID.DIRT || targetId === BlockID.DIRT_WITH_ROOTS) {
            block.level.setBlock(block.position.getSideVec(face), Block.get(BlockID.MUD))
            return null
        }
        return super.dispense(block, face, item)
    }
}
