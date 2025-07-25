package org.chorus_oss.chorus.block

class BlockLimeConcretePowder @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockConcretePowder(blockstate) {
    override fun getConcrete() = BlockLimeConcrete()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LIME_CONCRETE_POWDER)
    }
}