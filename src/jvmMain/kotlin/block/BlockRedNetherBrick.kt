package org.chorus_oss.chorus.block

class BlockRedNetherBrick @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockNetherBrick(blockstate) {
    override val name: String
        get() = "Red Nether Bricks"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.RED_NETHER_BRICK)
    }
}