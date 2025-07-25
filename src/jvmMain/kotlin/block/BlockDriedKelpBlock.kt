package org.chorus_oss.chorus.block


class BlockDriedKelpBlock(blockState: BlockState = properties.defaultState) : BlockSolid(blockState) {
    override val name: String
        get() = "Dried Kelp Block"

    override val hardness: Double
        get() = 0.5

    override val resistance: Double
        get() = 2.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DRIED_KELP_BLOCK)
    }
}
