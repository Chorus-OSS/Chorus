package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.block.BlockDispenser
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace

class DropperDispenseBehavior : DefaultDispenseBehavior() {
    override fun dispense(block: BlockDispenser, face: BlockFace, item: Item): Item? {
        val target = block.getSide(face)

        if (block.level.getBlockEntityIfLoaded(target.position) is InventoryHolder) {
            val invHolder = block.level.getBlockEntityIfLoaded(target.position) as InventoryHolder
            val inv = invHolder.inventory
            val clone = item.clone()
            clone.count = 1

            if (inv.canAddItem(clone)) {
                inv.addItem(clone)
            } else {
                return clone
            }
        } else {
            block.level.addSound(block.position, Sound.RANDOM_CLICK)
            return super.dispense(block, face, item)
        }
        return null
    }
}
