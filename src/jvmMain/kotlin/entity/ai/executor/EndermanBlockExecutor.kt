package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockAir
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.Natural
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.EntityEnderman
import org.chorus_oss.chorus.item.Item
import java.util.*

class EndermanBlockExecutor : IBehaviorExecutor {
    override fun execute(entity: EntityMob): Boolean {
        if (entity is EntityEnderman) {
            if (entity.itemInHand.isNothing) {
                val optionalBlock = Arrays.stream(
                    entity.level!!.getCollisionBlocks(
                        entity.getBoundingBox().grow(3.7, 0.0, 3.7)
                    )
                ).filter { block: Block -> block is Natural && block.canBePickedUp() }.findAny()
                if (optionalBlock.isPresent) {
                    val block = optionalBlock.get()
                    entity.setItemInHand(block.toItem())
                    entity.setDataProperty(EntityDataTypes.CARRY_BLOCK_STATE, block.blockState)
                    entity.level!!.setBlock(block.position, Block.get(BlockID.AIR))
                }
            } else {
                if (entity.itemInHand.isBlock()) {
                    val optionalBlock = Arrays.stream(
                        entity.level!!.getCollisionBlocks(
                            entity.getBoundingBox().addCoord(0.7, -1.0, 0.7)
                        )
                    ).filter { block: Block -> block.isSolid && block.up().canBeReplaced() }.findAny()
                    if (optionalBlock.isPresent) {
                        val block = optionalBlock.get()
                        block.level.setBlock(block.up().position, entity.itemInHand.getSafeBlockState().toBlock())
                        entity.setItemInHand(Item.AIR)
                        entity.setDataProperty(EntityDataTypes.CARRY_BLOCK_STATE, BlockAir.STATE)
                    }
                }
            }
        }
        return true
    }
}
