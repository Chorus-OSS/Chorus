package org.chorus_oss.chorus.block

class BlockRawIronBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    Block(blockstate) {
    override val name: String
        get() = "Block of Raw Iron"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.RAW_IRON_BLOCK)
    }
}