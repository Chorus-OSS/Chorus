package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.item.*
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace

class BucketDispenseBehavior : DefaultDispenseBehavior() {
    override fun dispense(block: BlockDispenser, face: BlockFace, item: Item): Item? {
        if (item !is ItemBucket) {
            return super.dispense(block, face, item)
        }

        val target = block.getSide(face)

        if (!item.isEmpty) {
            if (target.canBeFlowedInto() || target.id === BlockID.PORTAL) {
                val replace = item.targetBlock
                val fishEntityId = item.fishEntityId
                if (item is ItemLavaBucket) target.level.addSound(block.position, Sound.BUCKET_EMPTY_LAVA)
                else if (item is ItemPowderSnowBucket) target.level.addSound(
                    block.position,
                    Sound.BUCKET_EMPTY_POWDER_SNOW
                )
                else if (fishEntityId != null) target.level.addSound(block.position, Sound.BUCKET_EMPTY_FISH)
                else target.level.addSound(block.position, Sound.BUCKET_EMPTY_WATER)

                if (target.id == BlockID.PORTAL) {
                    target.onBreak(null)
                    target.level.vibrationManager.callVibrationEvent(
                        VibrationEvent(
                            this,
                            target.position.add(0.5, 0.5, 0.5),
                            VibrationType.BLOCK_DESTROY
                        )
                    )
                }

                if (replace is BlockLiquid || replace.id === BlockID.POWDER_SNOW) {
                    block.level.setBlock(target.position, replace)
                    if (fishEntityId != null) item.spawnFishEntity(target.add(0.5, 0.5, 0.5))
                    if (replace is BlockLiquid) target.level.vibrationManager.callVibrationEvent(
                        VibrationEvent(
                            this, target.position.add(0.5, 0.5, 0.5), VibrationType.FLUID_PLACE
                        )
                    )
                    else target.level.vibrationManager.callVibrationEvent(
                        VibrationEvent(
                            this, target.position.add(0.5, 0.5, 0.5), VibrationType.BLOCK_PLACE
                        )
                    )
                    return Item.get(ItemID.BUCKET, 0, 1, item.compoundTag)
                }
            }
        } else {
            if (target is BlockFlowingWater && target.liquidDepth == 0) {
                target.level.setBlock(target.position, Block.get(BlockID.AIR))
                target.level.vibrationManager.callVibrationEvent(
                    VibrationEvent(
                        this,
                        target.position.add(0.5, 0.5, 0.5),
                        VibrationType.FLUID_PICKUP
                    )
                )
                target.level.addSound(block.position, Sound.BUCKET_FILL_WATER)
                return Item.get(ItemID.WATER_BUCKET, 0, 1, item.compoundTag)
            } else if (target is BlockFlowingLava && target.liquidDepth == 0) {
                target.level.setBlock(target.position, Block.get(BlockID.AIR))
                target.level.vibrationManager.callVibrationEvent(
                    VibrationEvent(
                        this,
                        target.position.add(0.5, 0.5, 0.5),
                        VibrationType.FLUID_PICKUP
                    )
                )
                target.level.addSound(block.position, Sound.BUCKET_FILL_LAVA)
                return Item.get(ItemID.LAVA_BUCKET, 0, 1, item.compoundTag)
            } else if (target is BlockPowderSnow) {
                target.level.setBlock(target.position, Block.get(BlockID.AIR))
                target.level.addSound(block.position, Sound.BUCKET_FILL_POWDER_SNOW)
                target.level.vibrationManager.callVibrationEvent(
                    VibrationEvent(
                        this,
                        target.position.add(0.5, 0.5, 0.5),
                        VibrationType.FLUID_PICKUP
                    )
                )
                return Item.get(ItemID.POWDER_SNOW_BUCKET, 0, 1, item.compoundTag)
            }
        }

        return super.dispense(block, face, item)
    }
}
