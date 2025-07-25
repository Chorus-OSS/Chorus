package org.chorus_oss.chorus.block


class BlockReinforcedDeepslate @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    override val name: String
        get() = "ReinForced DeepSlate"

    override val resistance: Double
        get() = 1200.0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.REINFORCED_DEEPSLATE)
    }
}
