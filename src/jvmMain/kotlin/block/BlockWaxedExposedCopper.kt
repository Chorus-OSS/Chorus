package org.chorus_oss.chorus.block

class BlockWaxedExposedCopper @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockExposedCopper(blockstate) {
    override val name: String
        get() = "Waxed Exposed Copper"

    override val isWaxed: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WAXED_EXPOSED_COPPER)
    }
}