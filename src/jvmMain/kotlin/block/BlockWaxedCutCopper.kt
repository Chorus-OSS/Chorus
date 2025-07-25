package org.chorus_oss.chorus.block

class BlockWaxedCutCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCutCopper(blockstate) {
    override val name: String
        get() = "Waxed Cut Copper"

    override val isWaxed: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WAXED_CUT_COPPER)
    }
}