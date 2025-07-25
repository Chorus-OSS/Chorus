package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.level.Sound

class BlockBambooDoor @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWoodenDoor(blockstate) {
    override val name: String
        get() = "Bamboo Door Block"

    override fun playOpenSound() {
        level.addSound(this.position, Sound.OPEN_BAMBOO_WOOD_DOOR)
    }

    override fun playCloseSound() {
        level.addSound(this.position, Sound.CLOSE_BAMBOO_WOOD_DOOR)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.BAMBOO_DOOR,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.OPEN_BIT,
            CommonBlockProperties.UPPER_BLOCK_BIT,
            CommonBlockProperties.DOOR_HINGE_BIT
        )

    }
}