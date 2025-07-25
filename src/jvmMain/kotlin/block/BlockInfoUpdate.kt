package org.chorus_oss.chorus.block

class BlockInfoUpdate(blockState: BlockState = properties.defaultState) : Block(blockState) {
    override val name: String
        get() = "Info Update Block"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.INFO_UPDATE)
    }
}
