package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.level.Sound

class BlockCherryDoor @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockWoodenDoor(blockState) {
    override val name: String
        get() = "Cherry Door Block"

    override fun playOpenSound() {
        level.addSound(this.position, Sound.OPEN_CHERRY_WOOD_DOOR)
    }

    override fun playCloseSound() {
        level.addSound(this.position, Sound.CLOSE_CHERRY_WOOD_DOOR)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.CHERRY_DOOR,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.OPEN_BIT,
            CommonBlockProperties.UPPER_BLOCK_BIT,
            CommonBlockProperties.DOOR_HINGE_BIT
        )
    }
}