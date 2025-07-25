package org.chorus_oss.chorus.block

class BlockDeadBrainCoral @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockBrainCoral(blockstate) {
    override fun isDead(): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DEAD_BRAIN_CORAL)
    }
}