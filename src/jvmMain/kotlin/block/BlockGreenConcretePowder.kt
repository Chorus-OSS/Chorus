package org.chorus_oss.chorus.block

class BlockGreenConcretePowder @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockConcretePowder(blockstate) {

    override fun getConcrete() = BlockGreenConcrete()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.GREEN_CONCRETE_POWDER)
    }
}