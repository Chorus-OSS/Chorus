package org.chorus_oss.chorus.block

class BlockRedSand(blockState: BlockState = properties.defaultState) : BlockSand(blockState) {
    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.RED_SAND)
    }
}
