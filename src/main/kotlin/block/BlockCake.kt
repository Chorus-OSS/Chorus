package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace

class BlockCake @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockTransparent(blockState) {
    override val name: String
        get() = "Cake Block"

    override fun canBeActivated(): Boolean {
        return true
    }

    override val hardness: Double
        get() = 0.5

    override val resistance: Double
        get() = 0.5

    override val waterloggingLevel: Int
        get() = 1

    override var minX: Double
        get() = position.x + (1 + biteCount * 2) / 16
        set(minX) {
            super.minX = minX
        }

    override var minY: Double
        get() = position.y
        set(minY) {
            super.minY = minY
        }

    override var minZ: Double
        get() = position.z + 0.0625
        set(minZ) {
            super.minZ = minZ
        }

    override var maxX: Double
        get() = position.x - 0.0625 + 1
        set(maxX) {
            super.maxX = maxX
        }

    override var maxY: Double
        get() = position.y + 0.5
        set(maxY) {
            super.maxY = maxY
        }

    override var maxZ: Double
        get() = position.z - 0.0625 + 1
        set(maxZ) {
            super.maxZ = maxZ
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
        if (!down().isAir) {
            level.setBlock(block.position, this, direct = true, update = true)

            return true
        }
        return false
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (down().isAir) {
                level.setBlock(this.position, get(BlockID.AIR), true)

                return Level.BLOCK_UPDATE_NORMAL
            }
        }

        return 0
    }

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (isNotActivate(player)) return false
        if (item.getSafeBlockState().identifier == BlockCandle.properties.identifier && this.biteCount == 0) {
            return false
        }
        val damage = biteCount
        if (player != null && (player.foodData!!.isHungry || player.isCreative || Server.instance.getDifficulty() == 0)
        ) {
            if (damage < CommonBlockProperties.BITE_COUNTER.max) biteCount = damage + 1
            if (damage >= CommonBlockProperties.BITE_COUNTER.max) {
                level.setBlock(this.position, get(BlockID.AIR), true)
            } else {
                player.foodData?.addFood(2, 0.4f)
                level.setBlock(this.position, this, true)
            }
            level.addLevelSoundEvent(
                this.position,
                org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.Burp
            )
            level.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    player,
                    position.add(0.5, 0.5, 0.5), VibrationType.EAT
                )
            )
            return true
        }
        return false
    }

    override val comparatorInputOverride: Int
        get() = (7 - this.biteCount) * 2

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    var biteCount: Int
        get() = getPropertyValue(CommonBlockProperties.BITE_COUNTER)
        set(count) {
            setPropertyValue(CommonBlockProperties.BITE_COUNTER, count)
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CAKE, CommonBlockProperties.BITE_COUNTER)
    }
}
