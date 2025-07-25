package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties


class BlockMediumAmethystBud @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockAmethystBud(blockState) {
    override val namePrefix: String
        get() = "Medium"

    override val lightLevel: Int
        get() = 2

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.MEDIUM_AMETHYST_BUD, CommonBlockProperties.MINECRAFT_BLOCK_FACE)

    }
}
