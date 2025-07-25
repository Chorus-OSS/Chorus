package org.chorus_oss.chorus.block

open class BlockPolishedBlackstone @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockBlackstone(blockstate) {
    override val name: String
        get() = "Polished Blackstone"

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val hardness: Double
        get() = 1.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.POLISHED_BLACKSTONE)

    }
}