package org.chorus_oss.chorus.block

class BlockWarpedNylium @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockNylium(blockstate) {
    override val name: String
        get() = "Warped Nylium"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WARPED_NYLIUM)
    }
}