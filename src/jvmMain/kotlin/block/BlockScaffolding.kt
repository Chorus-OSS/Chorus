package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.item.EntityFallingBlock
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockScaffolding @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFallable(blockstate) {
    override val name: String
        get() = "Scaffolding"

    var stability: Int
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.STABILITY)
        set(stability) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.STABILITY, stability)
        }

    var stabilityCheck: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.STABILITY_CHECK)
        set(check) {
            setPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.STABILITY_CHECK,
                check
            )
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
        if (block is BlockFlowingLava) {
            return false
        }

        val down = down()
        if ((target!!.id != BlockID.SCAFFOLDING) && (down.id != BlockID.SCAFFOLDING) && !down.isAir && !down.isSolid) {
            var scaffoldOnSide = false
            for (i in 0..3) {
                val sideFace = fromHorizontalIndex(i)
                if (sideFace != face) {
                    val side = getSide(sideFace)
                    if (side.id == BlockID.SCAFFOLDING) {
                        scaffoldOnSide = true
                        break
                    }
                }
            }
            if (!scaffoldOnSide) {
                return false
            }
        }

        stabilityCheck = true
        level.setBlock(this.position, this, true, true)
        return true
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            val down = down()
            if (down.isSolid) {
                if (!isDefaultState) {
                    setPropertyValues(
                        CommonBlockProperties.STABILITY.createValue(0),
                        CommonBlockProperties.STABILITY_CHECK.createValue(false)
                    )
                    level.setBlock(this.position, this, true, true)
                }
                return type
            }

            var stability = 7
            for (face in BlockFace.entries) {
                if (face == BlockFace.UP) {
                    continue
                }

                val otherBlock = getSide(face)
                if (otherBlock.id == BlockID.SCAFFOLDING) {
                    val other = otherBlock as BlockScaffolding
                    val otherStability = other.stability
                    if (otherStability < stability) {
                        stability = if (face == BlockFace.DOWN) {
                            otherStability
                        } else {
                            otherStability + 1
                        }
                    }
                }
            }

            if (stability >= 7) {
                if (stabilityCheck) {
                    super.onUpdate(type)
                } else {
                    level.scheduleUpdate(this, 0)
                }
                return type
            }

            stabilityCheck = false
            this.stability = stability
            level.setBlock(this.position, this, true, true)
            return type
        } else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            level.useBreakOn(this.position)
            return type
        }

        return 0
    }

    override fun createFallingEntity(customNbt: CompoundTag): EntityFallingBlock? {
        setPropertyValues(
            CommonBlockProperties.STABILITY.createValue(0),
            CommonBlockProperties.STABILITY_CHECK.createValue(false)
        )
        customNbt.putBoolean("BreakOnLava", true)
        return super.createFallingEntity(customNbt)
    }

    override val hardness: Double
        get() = 0.5

    override val resistance: Double
        get() = 0.0

    override val burnChance: Int
        get() = 60

    override val burnAbility: Int
        get() = 60

    override val waterloggingLevel: Int
        get() = 1

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun canBeClimbed(): Boolean {
        return true
    }

    override fun canBeFlowedInto(): Boolean {
        return false
    }

    override fun recalculateBoundingBox(): AxisAlignedBB {
        return SimpleAxisAlignedBB(
            position.x,
            position.y + (2.0 / 16),
            position.z, position.x + 1, position.y + 1, position.z + 1
        )
    }

    override fun onEntityCollide(entity: Entity) {
        entity.resetFallDistance()
    }

    override fun hasEntityCollision(): Boolean {
        return true
    }

    override val boundingBox: AxisAlignedBB
        get() = this

    override val collisionBoundingBox: AxisAlignedBB
        get() = this

    override var minY: Double
        get() = position.y + (14.0 / 16)
        set(minY) {
            super.minY = minY
        }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB {
        return this
    }

    override fun canPassThrough(): Boolean {
        return false
    }

    override val isTransparent: Boolean
        get() = true

    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return side == BlockFace.UP
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SCAFFOLDING, CommonBlockProperties.STABILITY, CommonBlockProperties.STABILITY_CHECK)
    }
}
