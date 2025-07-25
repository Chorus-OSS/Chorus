package org.chorus_oss.chorus.block

class BlockPolishedBlackstoneBricks @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPolishedBlackstone(blockstate) {
    override val name: String
        get() = "Polished Blackstone Bricks"

    override val hardness: Double
        get() = 1.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.POLISHED_BLACKSTONE_BRICKS)

    }
}