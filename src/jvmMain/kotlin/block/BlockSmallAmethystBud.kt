package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties


class BlockSmallAmethystBud @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockAmethystBud(blockState) {
    override val namePrefix: String
        get() = "Small"

    override val lightLevel: Int
        get() = 1

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SMALL_AMETHYST_BUD, CommonBlockProperties.MINECRAFT_BLOCK_FACE)
    }
}
