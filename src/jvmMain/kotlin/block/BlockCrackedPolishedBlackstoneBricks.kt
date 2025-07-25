package org.chorus_oss.chorus.block

class BlockCrackedPolishedBlackstoneBricks @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockBlackstone(blockstate) {
    override val name: String
        get() = "Cracked Polished Blackstone Bricks"

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CRACKED_POLISHED_BLACKSTONE_BRICKS)
    }
}