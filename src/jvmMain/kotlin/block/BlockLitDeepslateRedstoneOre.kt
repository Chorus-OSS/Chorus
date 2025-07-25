package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.item.Item

class BlockLitDeepslateRedstoneOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockDeepslateRedstoneOre(blockstate), IBlockOreRedstoneGlowing {
    override val name: String
        get() = "Glowing Deepslate Redstone Ore"

    override val lightLevel: Int
        get() = 9

    override fun toItem(): Item {
        return super<IBlockOreRedstoneGlowing>.toItem()
    }

    override fun onUpdate(type: Int): Int {
        return super<IBlockOreRedstoneGlowing>.onUpdate(this, type)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LIT_DEEPSLATE_REDSTONE_ORE)
    }
}