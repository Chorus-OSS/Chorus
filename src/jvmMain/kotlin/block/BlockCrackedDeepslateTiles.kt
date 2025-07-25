package org.chorus_oss.chorus.block

class BlockCrackedDeepslateTiles @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDeepslateTiles(blockstate) {
    override val name: String
        get() = "Cracked Deepslate Tiles"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CRACKED_DEEPSLATE_TILES)
    }
}