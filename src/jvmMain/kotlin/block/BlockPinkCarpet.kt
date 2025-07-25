package org.chorus_oss.chorus.block

class BlockPinkCarpet @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCarpet(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PINK_CARPET)
    }
}