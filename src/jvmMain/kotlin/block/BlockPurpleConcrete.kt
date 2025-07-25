package org.chorus_oss.chorus.block

class BlockPurpleConcrete @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockConcrete(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PURPLE_CONCRETE)
    }
}