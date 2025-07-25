package org.chorus_oss.chorus.block

class BlockCrimsonFence @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFence(blockstate) {
    override val name: String
        get() = "Crimson Fence"

    override val burnChance: Int
        get() = 0

    override val burnAbility: Int
        get() = 0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CRIMSON_FENCE)
    }
}