package org.chorus_oss.chorus.block

class BlockWaxedCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCopperBlock(blockstate) {
    override val name: String
        get() = "Waxed Block of Copper"

    override val isWaxed
        get(): Boolean {
            return true
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WAXED_COPPER)
    }
}