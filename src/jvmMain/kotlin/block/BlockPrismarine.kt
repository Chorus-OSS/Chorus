package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.enums.PrismarineBlockType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemTool

open class BlockPrismarine(blockState: BlockState = properties.defaultState) : BlockSolid(blockState) {
    override val hardness: Double
        get() = 1.5

    override val resistance: Double
        get() = 30.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val name: String
        get() = when (prismarineBlockType) {
            PrismarineBlockType.DEFAULT -> "Prismarine"
            PrismarineBlockType.DARK -> "Dark Prismarine"
            PrismarineBlockType.BRICKS -> "Prismarine Bricks"
        }

    var prismarineBlockType: PrismarineBlockType
        get() = when (this) {
            is BlockPrismarineBricks -> PrismarineBlockType.BRICKS
            is BlockDarkPrismarine -> PrismarineBlockType.DARK
            else -> PrismarineBlockType.DEFAULT
        }
        set(prismarineBlockType) {
            this.blockState = when (prismarineBlockType) {
                PrismarineBlockType.BRICKS -> BlockPrismarineBricks.properties.defaultState
                PrismarineBlockType.DARK -> BlockDarkPrismarine.properties.defaultState
                PrismarineBlockType.DEFAULT -> Companion.properties.defaultState
            }
        }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun toItem(): Item {
        return when (this.prismarineBlockType) {
            PrismarineBlockType.BRICKS -> ItemBlock(BlockPrismarineBricks.properties.defaultState)
            PrismarineBlockType.DARK -> ItemBlock(BlockDarkPrismarine.properties.defaultState)
            PrismarineBlockType.DEFAULT -> ItemBlock(properties.defaultState)
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PRISMARINE)
    }
}
