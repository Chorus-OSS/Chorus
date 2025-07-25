package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.level.generator.`object`.ObjectBigMushroom

class BlockBrownMushroom @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockMushroom(blockstate) {
    override val name: String
        get() = "Brown Mushroom"

    override val lightLevel: Int
        get() = 1

    override fun getType(): ObjectBigMushroom.MushroomType {
        return ObjectBigMushroom.MushroomType.BROWN
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BROWN_MUSHROOM)
    }
}