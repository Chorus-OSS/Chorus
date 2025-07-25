package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockUnpoweredRepeater @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockRedstoneRepeater(blockstate) {
    init {
        this.isPowered = false
    }

    override val name: String
        get() = "Unpowered Repeater"

    public override fun getPowered(): Block {
        return BlockPoweredRepeater().setPropertyValues(blockState.blockPropertyValues)
    }

    override val unpowered: Block
        get() = this

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.UNPOWERED_REPEATER,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.REPEATER_DELAY
        )
    }
}