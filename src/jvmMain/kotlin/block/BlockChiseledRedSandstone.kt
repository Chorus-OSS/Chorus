package org.chorus_oss.chorus.block

class BlockChiseledRedSandstone @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSandstone(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CHISELED_RED_SANDSTONE)
    }
}
