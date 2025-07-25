package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties


class BlockLargeAmethystBud @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockAmethystBud(blockState) {
    override val namePrefix: String
        get() = "Large"

    override val lightLevel: Int
        get() = 4

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.LARGE_AMETHYST_BUD, CommonBlockProperties.MINECRAFT_BLOCK_FACE)
    }
}
