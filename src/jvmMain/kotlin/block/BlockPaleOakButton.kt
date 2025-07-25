package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockPaleOakButton @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodenButton(blockstate) {
    override val name: String
        get() = "Pale Oak Button"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.PALE_OAK_BUTTON,
            CommonBlockProperties.BUTTON_PRESSED_BIT,
            CommonBlockProperties.FACING_DIRECTION
        )

    }
}