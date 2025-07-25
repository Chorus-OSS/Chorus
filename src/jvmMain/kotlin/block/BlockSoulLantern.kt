package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties


class BlockSoulLantern @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockLantern(blockstate) {
    override val name: String
        get() = "Soul Lantern"

    override val lightLevel: Int
        get() = 10

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SOUL_LANTERN, CommonBlockProperties.HANGING)
    }
}
