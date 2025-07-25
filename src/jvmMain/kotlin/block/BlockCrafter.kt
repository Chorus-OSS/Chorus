package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockCrafter @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    Block(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.CRAFTER,
            CommonBlockProperties.CRAFTING,
            CommonBlockProperties.ORIENTATION,
            CommonBlockProperties.TRIGGERED_BIT
        )
    }
}