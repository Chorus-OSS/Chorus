package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.block.property.enums.MinecraftCardinalDirection
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemRepeater
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex

abstract class BlockRedstoneRepeater(blockState: BlockState) : BlockRedstoneDiode(blockState) {
    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (isNotActivate(player)) return false
        val repeaterDelay = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.REPEATER_DELAY)
        if (repeaterDelay == 3) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.REPEATER_DELAY, 0)
        } else {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.REPEATER_DELAY, repeaterDelay + 1)
        }

        level.setBlock(this.position, this, true, true)
        return true
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
        if (!isSupportValid(block.down())) {
            return false
        }
        val blockFace = if (player != null) fromHorizontalIndex(
            player.getDirection()
                .getOpposite().horizontalIndex
        ) else BlockFace.SOUTH
        setPropertyValue(
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonPropertyMap.CARDINAL_BLOCKFACE.inverse()[blockFace]!!
        )
        if (!level.setBlock(block.position, this, true, true)) {
            return false
        }

        if (Server.instance.settings.levelSettings.enableRedstone) {
            if (shouldBePowered()) {
                level.scheduleUpdate(this, 1)
            }
        }
        return true
    }

    override val facing: BlockFace?
        get() = CommonPropertyMap.CARDINAL_BLOCKFACE.get(
            getPropertyValue<MinecraftCardinalDirection, org.chorus_oss.chorus.block.property.type.EnumPropertyType<MinecraftCardinalDirection>>(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
            )
        )

    override fun isAlternateInput(block: Block): Boolean {
        return isDiode(block)
    }

    override fun toItem(): Item {
        return ItemRepeater()
    }

    override val delay: Int
        get() = (1 + getPropertyValue(CommonBlockProperties.REPEATER_DELAY)) * 2

    override val isLocked: Boolean
        get() = this.powerOnSides > 0
}
