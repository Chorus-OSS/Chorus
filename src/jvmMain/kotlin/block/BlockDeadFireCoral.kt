package org.chorus_oss.chorus.block

class BlockDeadFireCoral @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFireCoral(blockstate) {

    override fun isDead() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DEAD_FIRE_CORAL)
    }
}