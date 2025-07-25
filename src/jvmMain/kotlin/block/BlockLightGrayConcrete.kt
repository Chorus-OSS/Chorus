package org.chorus_oss.chorus.block

class BlockLightGrayConcrete @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockConcrete(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LIGHT_GRAY_CONCRETE)
    }
}