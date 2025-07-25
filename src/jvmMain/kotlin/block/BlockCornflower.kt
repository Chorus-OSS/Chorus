package org.chorus_oss.chorus.block

class BlockCornflower(blockstate: BlockState = properties.defaultState) : BlockFlower(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CORNFLOWER)
    }
}