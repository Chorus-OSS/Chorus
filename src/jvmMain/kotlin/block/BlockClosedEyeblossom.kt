package org.chorus_oss.chorus.block

open class BlockClosedEyeblossom(blockstate: BlockState = properties.defaultState) : BlockFlower(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CLOSED_EYEBLOSSOM)
    }
}