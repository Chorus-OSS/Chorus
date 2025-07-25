package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

open class BlockWoodenButton @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockButton(blockstate) {
    override val name: String
        get() = "Oak Button"

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.WOODEN_BUTTON,
            CommonBlockProperties.BUTTON_PRESSED_BIT,
            CommonBlockProperties.FACING_DIRECTION
        )
    }
}