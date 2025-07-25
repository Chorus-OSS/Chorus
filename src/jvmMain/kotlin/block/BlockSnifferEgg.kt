package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.CrackedState
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace

// TODO: complete
class BlockSnifferEgg(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState) {
    override val name: String
        get() = "Sniffer Egg"

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
        this.setPropertyValue(
            CommonBlockProperties.CRACKED_STATE,
            CrackedState.NO_CRACKS
        )
        return level.setBlock(this.position, this)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SNIFFER_EGG, CommonBlockProperties.CRACKED_STATE)
    }
}