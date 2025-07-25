package org.chorus_oss.chorus.utils

import java.awt.Color

class BlockColor {
    @JvmField
    val red: Int

    @JvmField
    val green: Int

    @JvmField
    val blue: Int
    val alpha: Int

    @JvmOverloads
    constructor(red: Int, green: Int, blue: Int, alpha: Int = 0xff) {
        this.red = red and 0xff
        this.green = green and 0xff
        this.blue = blue and 0xff
        this.alpha = alpha and 0xff
    }

    @JvmOverloads
    constructor(rgb: Int, hasAlpha: Boolean = false) {
        this.red = (rgb shr 16) and 0xff
        this.green = (rgb shr 8) and 0xff
        this.blue = rgb and 0xff
        this.alpha = if (hasAlpha) (rgb shr 24) and 0xff else 0xff
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BlockColor) {
            return false
        }
        return this.red == other.red && this.green == other.green && this.blue == other.blue && this.alpha == other.alpha
    }

    override fun toString(): String {
        return "BlockColor[r=" + this.red + ",g=" + this.green + ",b=" + this.blue + ",a=" + this.alpha + "]"
    }

    val rgb: Int
        get() = (this.red shl 16 or (this.green shl 8) or this.blue) and 0xffffff

    val argb: Int
        get() = this.alpha shl 24 or (this.red shl 16) or (this.green shl 8) or this.blue

    fun toAwtColor(): Color {
        return Color(this.red, this.green, this.blue, this.alpha)
    }

    companion object {
        val TRANSPARENT_BLOCK_COLOR: BlockColor = BlockColor(0x00, 0x00, 0x00, 0x00)

        @JvmField
        val VOID_BLOCK_COLOR: BlockColor = TRANSPARENT_BLOCK_COLOR
        val AIR_BLOCK_COLOR: BlockColor = TRANSPARENT_BLOCK_COLOR

        val GRASS_BLOCK_COLOR: BlockColor = BlockColor(0x7f, 0xb2, 0x38)
        val SAND_BLOCK_COLOR: BlockColor = BlockColor(0xf7, 0xe9, 0xa3)
        val CLOTH_BLOCK_COLOR: BlockColor = BlockColor(0xc7, 0xc7, 0xc7)
        val TNT_BLOCK_COLOR: BlockColor = BlockColor(0xff, 0x00, 0x00)
        val ICE_BLOCK_COLOR: BlockColor = BlockColor(0xa0, 0xa0, 0xff)
        val IRON_BLOCK_COLOR: BlockColor = BlockColor(0xa7, 0xa7, 0xa7)
        val FOLIAGE_BLOCK_COLOR: BlockColor = BlockColor(0x00, 0x7c, 0x00)
        val SNOW_BLOCK_COLOR: BlockColor = BlockColor(0xff, 0xff, 0xff)
        val CLAY_BLOCK_COLOR: BlockColor = BlockColor(0xa4, 0xa8, 0xb8)
        val DIRT_BLOCK_COLOR: BlockColor = BlockColor(0x97, 0x6d, 0x4d)
        val STONE_BLOCK_COLOR: BlockColor = BlockColor(0x70, 0x70, 0x70)
        val WATER_BLOCK_COLOR: BlockColor = BlockColor(64, 64, 160)


        val FLOWING_WATER_BLOCK_COLOR: BlockColor = BlockColor(30, 90, 245, 0)
        val LAVA_BLOCK_COLOR: BlockColor = TNT_BLOCK_COLOR
        val WOOD_BLOCK_COLOR: BlockColor = BlockColor(0x8f, 0x77, 0x48)
        val QUARTZ_BLOCK_COLOR: BlockColor = BlockColor(0xff, 0xfc, 0xf5)
        val ADOBE_BLOCK_COLOR: BlockColor = BlockColor(0xd8, 0x7f, 0x33)

        val WHITE_BLOCK_COLOR: BlockColor = SNOW_BLOCK_COLOR
        val ORANGE_BLOCK_COLOR: BlockColor = ADOBE_BLOCK_COLOR
        val MAGENTA_BLOCK_COLOR: BlockColor = BlockColor(0xb2, 0x4c, 0xd8)
        val LIGHT_BLUE_BLOCK_COLOR: BlockColor = BlockColor(0x66, 0x99, 0xd8)
        val YELLOW_BLOCK_COLOR: BlockColor = BlockColor(0xe5, 0xe5, 0x33)
        val LIME_BLOCK_COLOR: BlockColor = BlockColor(0x7f, 0xcc, 0x19)
        val PINK_BLOCK_COLOR: BlockColor = BlockColor(0xf2, 0x7f, 0xa5)
        val GRAY_BLOCK_COLOR: BlockColor = BlockColor(0x4c, 0x4c, 0x4c)
        val LIGHT_GRAY_BLOCK_COLOR: BlockColor = BlockColor(0x99, 0x99, 0x99)
        val CYAN_BLOCK_COLOR: BlockColor = BlockColor(0x4c, 0x7f, 0x99)
        val PURPLE_BLOCK_COLOR: BlockColor = BlockColor(0x7f, 0x3f, 0xb2)
        val BLUE_BLOCK_COLOR: BlockColor = BlockColor(0x33, 0x4c, 0xb2)
        val BROWN_BLOCK_COLOR: BlockColor = BlockColor(0x66, 0x4c, 0x33)
        val GREEN_BLOCK_COLOR: BlockColor = BlockColor(0x66, 0x7f, 0x33)
        val RED_BLOCK_COLOR: BlockColor = BlockColor(0x99, 0x33, 0x33)
        val BLACK_BLOCK_COLOR: BlockColor = BlockColor(0x19, 0x19, 0x19)

        val GOLD_BLOCK_COLOR: BlockColor = BlockColor(0xfa, 0xee, 0x4d)
        val DIAMOND_BLOCK_COLOR: BlockColor = BlockColor(0x5c, 0xdb, 0xd5)
        val LAPIS_BLOCK_COLOR: BlockColor = BlockColor(0x4a, 0x80, 0xff)
        val EMERALD_BLOCK_COLOR: BlockColor = BlockColor(0x00, 0xd9, 0x3a)
        val OBSIDIAN_BLOCK_COLOR: BlockColor = BlockColor(0x15, 0x14, 0x1f)
        val SPRUCE_BLOCK_COLOR: BlockColor = BlockColor(0x81, 0x56, 0x31)
        val NETHERRACK_BLOCK_COLOR: BlockColor = BlockColor(0x70, 0x02, 0x00)
        val REDSTONE_BLOCK_COLOR: BlockColor = TNT_BLOCK_COLOR

        val WHITE_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0xd1, 0xb1, 0xa1)
        val ORANGE_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x9f, 0x52, 0x24)
        val MAGENTA_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x95, 0x57, 0x6c)
        val LIGHT_BLUE_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x70, 0x6c, 0x8a)
        val YELLOW_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0xba, 0x85, 0x24)
        val LIME_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x67, 0x75, 0x35)
        val PINK_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0xa0, 0x4d, 0x4e)
        val GRAY_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x39, 0x29, 0x23)
        val LIGHT_GRAY_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x87, 0x6b, 0x62)
        val CYAN_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x57, 0x5c, 0x5c)
        val PURPLE_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x7a, 0x49, 0x58)
        val BLUE_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x4c, 0x3e, 0x5c)
        val BROWN_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x4c, 0x32, 0x23)
        val GREEN_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x4c, 0x52, 0x2a)
        val RED_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x8e, 0x3c, 0x2e)
        val BLACK_TERRACOTA_BLOCK_COLOR: BlockColor = BlockColor(0x25, 0x16, 0x10)


        val CRIMSON_NYLIUM_BLOCK_COLOR: BlockColor = BlockColor(0xBD, 0x30, 0x31)


        val CRIMSON_STEM_BLOCK_COLOR: BlockColor = BlockColor(0x94, 0x3F, 0x61)


        val CRIMSON_HYPHAE_BLOCK_COLOR: BlockColor = BlockColor(0x5C, 0x19, 0x1D)


        val WARPED_NYLIUM_BLOCK_COLOR: BlockColor = BlockColor(0x16, 0x7E, 0x86)


        val WARPED_STEM_BLOCK_COLOR: BlockColor = BlockColor(0x3A, 0x8E, 0x8C)


        val WARPED_HYPHAE_BLOCK_COLOR: BlockColor = BlockColor(0x56, 0x2C, 0x3E)


        val WARPED_WART_BLOCK_COLOR: BlockColor = BlockColor(0x14, 0xB4, 0x85)


        val SCULK_BLOCK_COLOR: BlockColor = BlockColor(0x0d, 0x12, 0x17)


        val DEEPSLATE_GRAY: BlockColor = BlockColor(0x64, 0x64, 0x64)


        val RAW_IRON_BLOCK_COLOR: BlockColor = BlockColor(0xd8, 0xaf, 0x93)


        val LICHEN_GREEN: BlockColor = BlockColor(0x7F, 0xA7, 0x96)


        val BROWNISH_RED: BlockColor = BlockColor(0x8E, 0x2F, 0x2F)


        val SMALL_AMETHYST_BUD_COLOR: BlockColor = BlockColor(153, 90, 205)


        val CORAL_FAN_COLOR: BlockColor = BlockColor(146, 188, 88, 0)


        val REPEATING_COMMAND_BLOCK_COLOR: BlockColor = BlockColor(153, 90, 205)

        fun getDyeColor(dyeColorMeta: Int): BlockColor? {
            return DyeColor.getByDyeData(dyeColorMeta).color
        }
    }
}
