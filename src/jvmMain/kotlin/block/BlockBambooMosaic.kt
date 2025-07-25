package org.chorus_oss.chorus.block


class BlockBambooMosaic @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    override val name: String
        get() = "Bamboo Mosaic"

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 15.0

    override val burnAbility: Int
        get() = 20

    override val burnChance: Int
        get() = 5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BAMBOO_MOSAIC)
    }
}