package org.chorus_oss.chorus.block

class BlockRedSandstone @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSandstone(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.RED_SANDSTONE)
    }
}
