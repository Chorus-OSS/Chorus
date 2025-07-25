package org.chorus_oss.chorus.block


class BlockUnknown @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    Block(blockstate) {

    override val name: String
        get() = "Unknown"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.UNKNOWN)
    }
}
