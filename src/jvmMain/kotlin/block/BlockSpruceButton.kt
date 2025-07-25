package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockSpruceButton @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodenButton(blockstate) {
    override val name: String
        get() = "Spruce Button"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.SPRUCE_BUTTON,
            CommonBlockProperties.BUTTON_PRESSED_BIT,
            CommonBlockProperties.FACING_DIRECTION
        )
    }
}