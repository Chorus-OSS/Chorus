package org.chorus_oss.chorus.block

class BlockLightGrayConcretePowder @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockConcretePowder(blockstate) {
    override fun getConcrete() = BlockLightGrayConcrete()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LIGHT_GRAY_CONCRETE_POWDER)
    }
}