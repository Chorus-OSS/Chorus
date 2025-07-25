package org.chorus_oss.chorus.block

class BlockWhiteConcretePowder @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockConcretePowder(blockstate) {
    override fun getConcrete() = BlockWhiteConcrete()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WHITE_CONCRETE_POWDER)
    }
}