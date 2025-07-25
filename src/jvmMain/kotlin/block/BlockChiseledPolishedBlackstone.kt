package org.chorus_oss.chorus.block

class BlockChiseledPolishedBlackstone @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockBlackstone(blockstate) {
    override val name: String
        get() = "Chiseled Polished Blackstone"

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CHISELED_POLISHED_BLACKSTONE)
    }
}