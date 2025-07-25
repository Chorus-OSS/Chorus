package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.level.generator.`object`.ObjectBigMushroom

class BlockRedMushroom @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockMushroom(blockstate) {
    override val name: String
        get() = "Red Mushroom"

    override val lightLevel: Int
        get() = 1

    override fun getType() = ObjectBigMushroom.MushroomType.RED

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.RED_MUSHROOM)
    }
}