package org.chorus_oss.chorus.block

class BlockBlueOrchid(blockState: BlockState = properties.defaultState) : BlockFlower(blockState) {
    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BLUE_ORCHID)
    }
}