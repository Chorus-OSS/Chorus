package org.chorus_oss.chorus.block

class BlockRawCopperBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockRaw(blockstate) {
    override val name: String
        get() = "Block of Raw Copper"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.RAW_COPPER_BLOCK)
    }
}