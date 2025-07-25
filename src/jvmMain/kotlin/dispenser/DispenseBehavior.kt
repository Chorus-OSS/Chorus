package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.block.BlockDispenser
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace


interface DispenseBehavior {
    fun dispense(block: BlockDispenser, face: BlockFace, item: Item): Item?
}
