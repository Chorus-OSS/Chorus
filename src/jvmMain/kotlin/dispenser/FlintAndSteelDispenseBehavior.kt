package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockDispenser
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace

class FlintAndSteelDispenseBehavior : DefaultDispenseBehavior() {
    override fun dispense(block: BlockDispenser, face: BlockFace, item: Item): Item? {
        var item1 = item
        val target = block.getSide(face)
        item1 = item1.clone()

        val down = target.down()
        if (down.id === BlockID.OBSIDIAN) {
            if (down.level.dimension != Level.DIMENSION_THE_END) {
                if (down.level.createPortal(down)) {
                    item1.useOn(target)
                    return if (item1.damage >= item1.maxDurability) null else item1
                }
            }
        }

        if (target.id === BlockID.AIR) {
            block.level.addSound(block.position, Sound.RANDOM_CLICK, 1.0f, 1.0f)
            block.level.setBlock(target.position, Block.get(BlockID.FIRE))
            item1.useOn(target)
            return if (item1.damage >= item1.maxDurability) null else item1
        } else if (target.id === BlockID.TNT) {
            block.level.addSound(block.position, Sound.RANDOM_CLICK, 1.0f, 1.0f)
            target.onActivate(item1, null, face, 0f, 0f, 0f)
            item1.useOn(target)
            return if (item1.damage >= item1.maxDurability) null else item1
        } else {
            this.success = false
        }

        block.level.addSound(block.position, Sound.RANDOM_CLICK, 1.0f, 1.2f)
        return item1
    }
}
