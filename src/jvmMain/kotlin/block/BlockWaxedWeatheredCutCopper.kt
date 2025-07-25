package org.chorus_oss.chorus.block

class BlockWaxedWeatheredCutCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockWeatheredCutCopper(blockstate) {
    override val name: String
        get() = "Waxed Weathered Cut Copper"

    override val isWaxed: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WAXED_WEATHERED_CUT_COPPER)
    }
}