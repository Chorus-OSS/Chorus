package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.block.BlockDispenser
import org.chorus_oss.chorus.entity.EntityShearable
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB

class ShearsDispenseBehavior : DefaultDispenseBehavior() {
    override fun dispense(block: BlockDispenser, face: BlockFace, item: Item): Item? {
        var item1 = item
        item1 = item1.clone()
        val target = block.getSide(face)
        val bb = SimpleAxisAlignedBB(
            0.0, 0.0, 0.0,
            1.0, 1.0, 1.0
        )
            .offset(target.position.x, target.position.y, target.position.z)
        for (entity in block.level.getCollidingEntities(bb)) {
            if (entity !is EntityShearable) {
                continue
            }
            if (!entity.shear()) {
                continue
            }
            item1.useOn(entity)
            return if (item1.damage >= item1.maxDurability) null else item1
        }
        return item1
    }
}
