package org.chorus_oss.chorus.block

class BlockPrismarineBricks(blockState: BlockState = properties.defaultState) : BlockPrismarine(blockState) {
    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PRISMARINE_BRICKS)
    }
}
