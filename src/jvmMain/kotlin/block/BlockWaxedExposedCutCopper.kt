package org.chorus_oss.chorus.block

class BlockWaxedExposedCutCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockExposedCutCopper(blockstate) {
    override val name: String
        get() = "Waxed Exposed Cut Copper"

    override val isWaxed: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WAXED_EXPOSED_CUT_COPPER)
    }
}