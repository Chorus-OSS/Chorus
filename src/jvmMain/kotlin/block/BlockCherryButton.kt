package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockCherryButton @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodenButton(blockstate) {
    override val name: String
        get() = "Cherry Button"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.CHERRY_BUTTON,
            CommonBlockProperties.BUTTON_PRESSED_BIT,
            CommonBlockProperties.FACING_DIRECTION
        )

    }
}