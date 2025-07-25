package org.chorus_oss.chorus.block

class BlockOpenEyeblossom(blockState: BlockState = properties.defaultState) : BlockClosedEyeblossom(blockState) {
    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.OPEN_EYEBLOSSOM)

    }
}