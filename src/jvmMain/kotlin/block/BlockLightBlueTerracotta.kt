package org.chorus_oss.chorus.block

class BlockLightBlueTerracotta @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockHardenedClay(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LIGHT_BLUE_TERRACOTTA)
    }
}