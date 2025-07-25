package org.chorus_oss.chorus.utils

enum class TerracottaColor(
    dyeColorMeta: Int, terracottaColorMeta: Int, val colorName: String,
    val dyeName: String, blockColor: BlockColor?
) {
    BLACK(0, 15, "Black", "Ink Sac", BlockColor.BLACK_TERRACOTA_BLOCK_COLOR),
    RED(1, 14, "Red", "Rose Red", BlockColor.RED_TERRACOTA_BLOCK_COLOR),
    GREEN(2, 13, "Green", "Cactus Green", BlockColor.GREEN_TERRACOTA_BLOCK_COLOR),
    BROWN(3, 12, "Brown", "Cocoa Beans", BlockColor.BROWN_TERRACOTA_BLOCK_COLOR),
    BLUE(4, 11, "Blue", "Lapis Lazuli", BlockColor.BLUE_TERRACOTA_BLOCK_COLOR),
    PURPLE(5, 10, "Purple", BlockColor.PURPLE_TERRACOTA_BLOCK_COLOR),
    CYAN(6, 9, "Cyan", BlockColor.CYAN_TERRACOTA_BLOCK_COLOR),
    LIGHT_GRAY(7, 8, "Light Gray", BlockColor.LIGHT_GRAY_TERRACOTA_BLOCK_COLOR),
    GRAY(8, 7, "Gray", BlockColor.GRAY_TERRACOTA_BLOCK_COLOR),
    PINK(9, 6, "Pink", BlockColor.PINK_TERRACOTA_BLOCK_COLOR),
    LIME(10, 5, "Lime", BlockColor.LIME_TERRACOTA_BLOCK_COLOR),
    YELLOW(11, 4, "Yellow", "Dandelion Yellow", BlockColor.YELLOW_TERRACOTA_BLOCK_COLOR),
    LIGHT_BLUE(12, 3, "Light Blue", BlockColor.LIGHT_BLUE_TERRACOTA_BLOCK_COLOR),
    MAGENTA(13, 2, "Magenta", BlockColor.MAGENTA_TERRACOTA_BLOCK_COLOR),
    ORANGE(14, 1, "Orange", BlockColor.ORANGE_TERRACOTA_BLOCK_COLOR),
    WHITE(15, 0, "White", "Bone Meal", BlockColor.WHITE_TERRACOTA_BLOCK_COLOR);


    val dyeData: Int = dyeColorMeta
    val terracottaData: Int = terracottaColorMeta
    val color: BlockColor? = blockColor


    constructor(dyeColorMeta: Int, terracottaColorMeta: Int, colorName: String, blockColor: BlockColor?) : this(
        dyeColorMeta, terracottaColorMeta, colorName,
        "$colorName Dye", blockColor
    )

    companion object {
        private val BY_TERRACOTTA_DATA = entries.toTypedArray()
        private val BY_DYE_DATA = entries.toTypedArray()

        init {
            for (color in entries) {
                BY_TERRACOTTA_DATA[color.terracottaData and 0x0f] = color
                BY_DYE_DATA[color.dyeData and 0x0f] = color
            }
        }

        fun getByDyeData(dyeColorMeta: Int): TerracottaColor {
            return BY_DYE_DATA[dyeColorMeta and 0x0f]
        }

        fun getByTerracottaData(terracottaColorMeta: Int): TerracottaColor {
            return BY_TERRACOTTA_DATA[terracottaColorMeta and 0x0f]
        }
    }
}
