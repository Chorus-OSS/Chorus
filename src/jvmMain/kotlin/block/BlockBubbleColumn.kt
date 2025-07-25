package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityPhysical
import org.chorus_oss.chorus.event.block.BlockFadeEvent
import org.chorus_oss.chorus.event.block.BlockFromToEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.particle.BubbleParticle
import org.chorus_oss.chorus.level.particle.SplashParticle
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min

class BlockBubbleColumn @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate) {
    override val name: String
        get() = "Bubble Column"

    override val waterloggingLevel: Int
        get() = 2

    override fun canPassThrough(): Boolean {
        return true
    }

    override fun canBeFlowedInto(): Boolean {
        return true
    }

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override fun toItem(): Item {
        return Item.AIR
    }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB {
        return this
    }

    override fun isBreakable(vector: Vector3, layer: Int, face: BlockFace?, item: Item?, player: Player?): Boolean {
        return false
    }

    override fun canBeReplaced(): Boolean {
        return true
    }

    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override val boundingBox: AxisAlignedBB?
        get() = null

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        return null
    }

    override fun onEntityCollide(entity: Entity) {
        if (entity != null) {
            if (entity.canBeMovedByCurrents()) {
                if (up().isAir) {
                    if (isDragDown) {
                        entity.motion.y = max(-0.9, entity.motion.y - 0.03)
                    } else {
                        if (entity is EntityPhysical && entity.motion.y < -entity.getGravity() * 8) {
                            entity.motion.y = (-entity.getGravity() * 2).toDouble()
                        }
                        entity.motion.y = min(1.8, entity.motion.y + 0.1)
                    }

                    val random = ThreadLocalRandom.current()
                    for (i in 0..1) {
                        level.addParticle(
                            SplashParticle(
                                position.add(
                                    random.nextFloat().toDouble(),
                                    (random.nextFloat() + 1).toDouble(),
                                    random.nextFloat().toDouble()
                                )
                            )
                        )
                        level.addParticle(
                            BubbleParticle(
                                position.add(
                                    random.nextFloat().toDouble(),
                                    (random.nextFloat() + 1).toDouble(),
                                    random.nextFloat().toDouble()
                                )
                            )
                        )
                    }
                } else {
                    if (isDragDown) {
                        entity.motion.y = max(-0.3, entity.motion.y - 0.3)
                    } else {
                        entity.motion.y = min(0.7, entity.motion.y + 0.06)
                    }
                }
                entity.motionChanged = true
                entity.resetFallDistance()
            }
        }
    }

    override fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        if (down().id == BlockID.MAGMA) {
            isDragDown = true
        }
        level.setBlock(this.position, 1, BlockFlowingWater(), direct = true, update = false)
        level.setBlock(this.position, this, direct = true, update = true)
        return true
    }

    override val hardness: Double
        get() = 100.0

    override val resistance: Double
        get() = 500.0

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            val water = getLevelBlockAtLayer(1)
            if (water !is BlockFlowingWater || water.liquidDepth != 0 && water.liquidDepth != 8) {
                fadeOut(water)
                return type
            }

            if (water.blockState.specialValue().toInt() == 8) {
                water.liquidDepth = 0
                level.setBlock(this.position, 1, water, direct = true, update = false)
            }

            val down = down()
            if (down is BlockBubbleColumn) {
                if (down.isDragDown != this.isDragDown) {
                    level.setBlock(this.position, down, direct = true, update = true)
                }
            } else if (down.id == BlockID.MAGMA) {
                if (!this.isDragDown) {
                    isDragDown = true
                    level.setBlock(this.position, this, direct = true, update = true)
                }
            } else if (down.id == BlockID.SOUL_SAND) {
                if (this.isDragDown) { //!= false == true
                    isDragDown = false
                    level.setBlock(this.position, this, direct = true, update = true)
                }
            } else {
                fadeOut(water)
                return type
            }

            val up = up()
            if (up is BlockFlowingWater && (up.blockState.specialValue()
                    .toInt() == 0 || up.blockState.specialValue().toInt() == 8)
            ) {
                val event = BlockFromToEvent(this, up)
                if (!event.cancelled) {
                    level.setBlock(up.position, 1, BlockFlowingWater(), direct = true, update = false)
                    level.setBlock(up.position, 0, BlockBubbleColumn(this.blockState), direct = true, update = true)
                }
            }

            return type
        }

        return 0
    }

    var isDragDown: Boolean
        get() = getPropertyValue(CommonBlockProperties.DRAG_DOWN)
        set(dragDown) {
            setPropertyValue(CommonBlockProperties.DRAG_DOWN, dragDown)
        }

    private fun fadeOut(water: Block) {
        val event = BlockFadeEvent(this, water.clone())
        if (!event.cancelled) {
            level.setBlock(this.position, 1, BlockAir(), direct = true, update = false)
            level.setBlock(this.position, 0, event.newState, direct = true, update = true)
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BUBBLE_COLUMN, CommonBlockProperties.DRAG_DOWN)
    }
}
