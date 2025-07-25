package org.chorus_oss.chorus.block

class BlockCrimsonNylium @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockNylium(blockstate) {
    override val name: String
        get() = "Crimson Nylium"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CRIMSON_NYLIUM)
    }
}