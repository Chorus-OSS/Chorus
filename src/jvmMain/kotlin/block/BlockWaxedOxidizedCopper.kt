package org.chorus_oss.chorus.block

class BlockWaxedOxidizedCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockOxidizedCopper(blockstate) {
    override val name: String
        get() = "Waxed Oxidized Copper"

    override val isWaxed: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WAXED_OXIDIZED_COPPER)
    }
}