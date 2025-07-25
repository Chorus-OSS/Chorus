package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.block.BlockDispenser
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace

class ShulkerBoxDispenseBehavior : DefaultDispenseBehavior() {
    override fun dispense(block: BlockDispenser, face: BlockFace, item: Item): Item? {
        val target = block.getSide(face)

        if (!target.canBeReplaced()) {
            success = false
            return null
        }

        val shulkerBox = item.getSafeBlockState().toBlock()
        shulkerBox.level = block.level
        shulkerBox.layer = 0
        shulkerBox.position.x = target.position.x
        shulkerBox.position.y = target.position.y
        shulkerBox.position.z = target.position.z

        val shulkerBoxFace = if (shulkerBox.down().isTransparent) face else BlockFace.UP

        if (shulkerBox.place(
                item,
                target,
                target.getSide(shulkerBoxFace.getOpposite()),
                shulkerBoxFace,
                0.0,
                0.0,
                0.0,
                null
            )
                .also { success = it }
        ) {
            block.level.updateComparatorOutputLevel(target.position)
            block.level.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    this,
                    target.position.add(0.5, 0.5, 0.5),
                    VibrationType.BLOCK_PLACE
                )
            )
        }

        return null
    }
}
