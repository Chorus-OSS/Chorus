package org.chorus_oss.chorus.block

class BlockWaxedOxidizedCutCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockOxidizedCutCopper(blockstate) {
    override val name: String
        get() = "Waxed Oxidized Cut Copper"

    override val isWaxed: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WAXED_OXIDIZED_CUT_COPPER)
    }
}