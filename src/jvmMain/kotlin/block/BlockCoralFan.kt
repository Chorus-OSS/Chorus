package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.event.block.BlockFadeEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.utils.Faceable
import java.util.concurrent.ThreadLocalRandom

abstract class BlockCoralFan(blockstate: BlockState) : BlockFlowable(blockstate), Faceable {
    override val waterloggingLevel: Int
        get() = 1

    open val isDead: Boolean
        get() = false

    abstract fun getDeadCoralFan(): Block

    override var blockFace: BlockFace
        get() = fromHorizontalIndex(getPropertyValue(CommonBlockProperties.CORAL_FAN_DIRECTION) + 1)
        set(blockFace) {}

    open val rootsFace: BlockFace
        get() = BlockFace.DOWN

    override fun onUpdate(type: Int): Int {
        when (type) {
            Level.BLOCK_UPDATE_NORMAL -> {
                val side = getSide(rootsFace)
                if (!side.isSolid || side.id == BlockID.MAGMA || side.id == BlockID.SOUL_SAND) {
                    level.useBreakOn(this.position)
                } else {
                    level.scheduleUpdate(this, 60 + ThreadLocalRandom.current().nextInt(40))
                }
                return type
            }

            Level.BLOCK_UPDATE_SCHEDULED -> {
                val side = getSide(rootsFace)
                if (side.id == BlockID.ICE) {
                    level.useBreakOn(this.position)
                    return type
                }

                if (!isDead && (getLevelBlockAtLayer(1) !is BlockFlowingWater) && (getLevelBlockAtLayer(1) !is BlockFrostedIce)) {
                    val event = BlockFadeEvent(this, getDeadCoralFan())
                    if (!event.cancelled) {
                        level.setBlock(this.position, event.newState, direct = true, update = true)
                    }
                }
                return type
            }

            Level.BLOCK_UPDATE_RANDOM -> {
                if (getPropertyValue(CommonBlockProperties.CORAL_FAN_DIRECTION) == 0) {
                    setPropertyValue(CommonBlockProperties.CORAL_FAN_DIRECTION, 1)
                } else {
                    setPropertyValue(CommonBlockProperties.CORAL_FAN_DIRECTION, 0)
                }
                level.setBlock(this.position, this, direct = true, update = true)
                return type
            }

            else -> return 0
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
        if (face == BlockFace.DOWN) {
            return false
        }

        val layer1 = block.getLevelBlockAtLayer(1)
        val hasWater = layer1 is BlockFlowingWater
        if (!layer1.isAir && (!hasWater || layer1.blockState.specialValue()
                .toInt() != 0 && layer1.blockState.specialValue().toInt() != 8)
        ) {
            return false
        }

        if (hasWater && layer1.blockState.specialValue().toInt() == 8) {
            level.setBlock(this.position, 1, BlockFlowingWater(), direct = true, update = false)
        }

        if (!target!!.isSolid || target.id == BlockID.MAGMA || target.id == BlockID.SOUL_SAND) {
            return false
        }

        if (face == BlockFace.UP) {
            var rotation = player!!.rotation.yaw % 360
            if (rotation < 0) {
                rotation += 360.0
            }
            val axisBit = if (rotation >= 0 && rotation < 12 || (342 <= rotation && rotation < 360)) 0 else 1
            setPropertyValue(CommonBlockProperties.CORAL_FAN_DIRECTION, axisBit)
            level.setBlock(
                this.position, 0, if (hasWater) this.clone() else getDeadCoralFan().setPropertyValues(
                    blockState.blockPropertyValues
                ), direct = true, update = true
            )
        } else {
            val deadBlock = getDeadCoralFan()
            level.setBlock(this.position, 0, deadBlock, direct = true, update = true)
        }

        return true
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override fun getDrops(item: Item): Array<Item> {
        return if (item.getEnchantment(Enchantment.ID_SILK_TOUCH) != null) {
            super.getDrops(item)
        } else {
            Item.EMPTY_ARRAY
        }
    }
}
