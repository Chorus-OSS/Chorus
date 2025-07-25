package org.chorus_oss.chorus.block

class BlockCoarseDirt @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDirt(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.COARSE_DIRT)
    }
}