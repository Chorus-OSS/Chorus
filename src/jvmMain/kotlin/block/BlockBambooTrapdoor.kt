package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.level.Sound

class BlockBambooTrapdoor @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTrapdoor(blockstate) {
    override val name: String
        get() = "Bamboo Trapdoor"

    override fun playOpenSound() {
        level.addSound(this.position, Sound.OPEN_BAMBOO_WOOD_TRAPDOOR)
    }

    override fun playCloseSound() {
        level.addSound(this.position, Sound.CLOSE_BAMBOO_WOOD_TRAPDOOR)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.BAMBOO_TRAPDOOR,
            CommonBlockProperties.DIRECTION,
            CommonBlockProperties.OPEN_BIT,
            CommonBlockProperties.UPSIDE_DOWN_BIT
        )
    }
}