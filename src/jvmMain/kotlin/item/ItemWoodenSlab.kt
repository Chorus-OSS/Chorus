package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.MinecraftVerticalHalf

class ItemWoodenSlab @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.WOODEN_SLAB, meta, count) {
    override fun internalAdjust() {
        when (damage) {
            0, 6, 7 -> {
                name = "Oak Slab"
                blockState = BlockOakSlab.properties.defaultState
            }

            1 -> {
                name = "Spruce Slab"
                blockState = BlockSpruceSlab.properties.defaultState
            }

            2 -> {
                name = "Birch Slab"
                blockState = BlockBirchSlab.properties.defaultState
            }

            3 -> {
                name = "Jungle Slab"
                blockState = BlockJungleSlab.properties.defaultState
            }

            4 -> {
                name = "Acacia Slab"
                blockState = BlockAcaciaSlab.properties.defaultState
            }

            5 -> {
                name = "Dark Oak Slab"
                blockState = BlockDarkOakSlab.properties.defaultState
            }

            8, 14, 15 -> {
                name = "Oak Slab (Top)"
                blockState = BlockOakSlab.properties.getBlockState(
                    CommonBlockProperties.MINECRAFT_VERTICAL_HALF,
                    MinecraftVerticalHalf.TOP
                )
            }

            9 -> {
                name = "Spruce Slab (Top)"
                blockState = BlockSpruceSlab.properties.getBlockState(
                    CommonBlockProperties.MINECRAFT_VERTICAL_HALF,
                    MinecraftVerticalHalf.TOP
                )
            }

            10 -> {
                name = "Birch Slab (Top)"
                blockState = BlockBirchSlab.properties.getBlockState(
                    CommonBlockProperties.MINECRAFT_VERTICAL_HALF,
                    MinecraftVerticalHalf.TOP
                )
            }

            11 -> {
                name = "Jungle Slab (Top)"
                blockState = BlockJungleSlab.properties.getBlockState(
                    CommonBlockProperties.MINECRAFT_VERTICAL_HALF,
                    MinecraftVerticalHalf.TOP
                )
            }

            12 -> {
                name = "Acacia Slab (Top)"
                blockState = BlockAcaciaSlab.properties.getBlockState(
                    CommonBlockProperties.MINECRAFT_VERTICAL_HALF,
                    MinecraftVerticalHalf.TOP
                )
            }

            13 -> {
                name = "Dark Oak Slab (Top)"
                blockState = BlockDarkOakSlab.properties.getBlockState(
                    CommonBlockProperties.MINECRAFT_VERTICAL_HALF,
                    MinecraftVerticalHalf.TOP
                )
            }

            else -> throw IllegalArgumentException("Invalid damage: $damage")
        }
        this.meta = 0
    }
}