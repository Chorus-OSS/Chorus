package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockPoweredRepeater @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockRedstoneRepeater(blockstate) {
    init {
        isPowered = true
    }

    override val name: String
        get() = "Powered Repeater"

    public override fun getPowered(): Block {
        return this
    }

    override val unpowered
        get(): Block {
            val blockUnpoweredRepeater = BlockUnpoweredRepeater()
            return blockUnpoweredRepeater.setPropertyValues(blockState.blockPropertyValues)
        }

    override val lightLevel: Int
        get() = 7

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.POWERED_REPEATER,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.REPEATER_DELAY
        )
    }
}