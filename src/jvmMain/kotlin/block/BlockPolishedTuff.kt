package org.chorus_oss.chorus.block

class BlockPolishedTuff @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockTuff(blockState) {
    override val name: String
        get() = "Polished Tuff"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.POLISHED_TUFF)
    }
}