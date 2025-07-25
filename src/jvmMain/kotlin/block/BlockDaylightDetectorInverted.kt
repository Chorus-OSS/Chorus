package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.math.BlockFace

class BlockDaylightDetectorInverted @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockDaylightDetector(blockState) {
    override val name: String
        get() = "Daylight Detector Inverted"

    override fun toItem(): Item {
        return ItemBlock(BlockDaylightDetector.properties.defaultState, name, 0)
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
        val block = BlockDaylightDetector()
        level.setBlock(this.position, block, direct = true, update = true)
        if (Server.instance.settings.levelSettings.enableRedstone) {
            block.updatePower()
        }
        return true
    }

    override val isInverted: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.DAYLIGHT_DETECTOR_INVERTED, CommonBlockProperties.REDSTONE_SIGNAL)
    }
}
