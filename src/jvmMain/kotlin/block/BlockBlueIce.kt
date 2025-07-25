package org.chorus_oss.chorus.block


class BlockBlueIce(blockState: BlockState = properties.defaultState) : BlockPackedIce(blockState) {
    override val name: String
        get() = "Blue Ice"

    override val frictionFactor: Double
        get() {
            return 0.989
        }

    override val hardness: Double
        get() = 2.8

    override val resistance: Double
        get() = 14.0

    override val isTransparent: Boolean
        get() = false

    override val lightLevel: Int
        get() = 4

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BLUE_ICE)
    }
}
