package org.chorus_oss.chorus.block

class BlockChiseledDeepslate @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockCobbledDeepslate(blockState) {
    override val name: String
        get() = "Chiseled Deepslate"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CHISELED_DEEPSLATE)
    }
}