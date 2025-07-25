package org.chorus_oss.chorus.block

class BlockPackedMud(blockState: BlockState = properties.defaultState) : BlockSolid(blockState) {
    override val name: String
        get() = "Packed Mud"

    override val hardness: Double
        get() = 1.0

    override val resistance: Double
        get() = 3.0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PACKED_MUD)
    }
}
