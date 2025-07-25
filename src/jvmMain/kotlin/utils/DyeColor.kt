package org.chorus_oss.chorus.utils

import java.util.*

enum class DyeColor(
    dyeColorMeta: Int, woolColorMeta: Int,
    /**
     * The `minecraft:dye` meta that actually represents the item dye for that color.
     * Uses black_dye instead of ink_sac, white_dye instead of bone_meal, and so on.
     */
    val itemDyeMeta: Int, val colorName: String,
    val dyeName: String, blockColor: BlockColor,
    @JvmField val leatherColor: BlockColor = blockColor,
    val signColor: BlockColor = blockColor
) {
    BLACK(
        0,
        15,
        16,
        "Black",
        "Ink Sac",
        BlockColor.Companion.BLACK_BLOCK_COLOR,
        BlockColor(0x1D1D21),
        BlockColor(0x00, 0x00, 0x00)
    ),
    RED(
        1,
        14,
        1,
        "Red",
        "Red Dye",
        BlockColor.RED_BLOCK_COLOR,
        BlockColor(0xB02E26),
        BlockColor(0xb0, 0x2e, 0x26)
    ),
    GREEN(
        2,
        13,
        2,
        "Green",
        "Green Dye",
        BlockColor.GREEN_BLOCK_COLOR,
        BlockColor(0x5E7C16),
        BlockColor(0x5e, 0x7c, 0x16)
    ),
    BROWN(
        3,
        12,
        17,
        "Brown",
        "Cocoa Beans",
        BlockColor.BROWN_BLOCK_COLOR,
        BlockColor(0x835432),
        BlockColor(0x83, 0x54, 0x32)
    ),
    BLUE(
        4,
        11,
        18,
        "Blue",
        "Lapis Lazuli",
        BlockColor.BLUE_BLOCK_COLOR,
        BlockColor(0x3C44AA),
        BlockColor(0x3c, 0x44, 0xaa)
    ),
    PURPLE(
        5,
        10,
        5,
        "Purple",
        BlockColor.PURPLE_BLOCK_COLOR,
        BlockColor(0x8932B8),
        BlockColor(0x89, 0x32, 0xb8)
    ),
    CYAN(
        6,
        9,
        6,
        "Cyan",
        BlockColor.CYAN_BLOCK_COLOR,
        BlockColor(0x169C9C),
        BlockColor(0x16, 0x9c, 0x9c)
    ),
    LIGHT_GRAY(
        7,
        8,
        7,
        "Light Gray",
        BlockColor.LIGHT_GRAY_BLOCK_COLOR,
        BlockColor(0x9D9D97),
        BlockColor(0x9d, 0x9d, 0x97)
    ),
    GRAY(
        8,
        7,
        8,
        "Gray",
        BlockColor.GRAY_BLOCK_COLOR,
        BlockColor(0x474F52),
        BlockColor(0x47, 0x4f, 0x52)
    ),
    PINK(
        9,
        6,
        9,
        "Pink",
        BlockColor.PINK_BLOCK_COLOR,
        BlockColor(0xF38BAA),
        BlockColor(0xf3, 0x8b, 0xaa)
    ),
    LIME(
        10,
        5,
        10,
        "Lime",
        BlockColor.LIME_BLOCK_COLOR,
        BlockColor(0x80C71F),
        BlockColor(0x80, 0xc7, 0x1f)
    ),
    YELLOW(
        11,
        4,
        11,
        "Yellow",
        "Yellow Dye",
        BlockColor.YELLOW_BLOCK_COLOR,
        BlockColor(0xFED83D),
        BlockColor(0xfe, 0xd8, 0x3d)
    ),
    LIGHT_BLUE(
        12,
        3,
        12,
        "Light Blue",
        BlockColor.LIGHT_BLUE_BLOCK_COLOR,
        BlockColor(0x3AB3DA),
        BlockColor(0x3a, 0xb3, 0xda)
    ),
    MAGENTA(
        13,
        2,
        13,
        "Magenta",
        BlockColor.MAGENTA_BLOCK_COLOR,
        BlockColor(0xC74EBD),
        BlockColor(0xc7, 0x4e, 0xbd)
    ),
    ORANGE(
        14,
        1,
        14,
        "Orange",
        BlockColor.ORANGE_BLOCK_COLOR,
        BlockColor(0xFF9801),
        BlockColor(0xf9, 0x80, 0x1d)
    ),
    WHITE(
        15,
        0,
        19,
        "White",
        BlockColor.WHITE_BLOCK_COLOR,
        BlockColor(0xF0F0F0),
        BlockColor(0xf0, 0xf0, 0xf0)
    );

    /**
     * The `minecraft:dye` meta from `0-15` that represents the source of a dye. Includes
     * ink_sac, bone_meal, cocoa_beans, and lapis_lazuli.
     */
    val dyeData: Int = dyeColorMeta

    val woolData: Int = woolColorMeta
    val color: BlockColor = blockColor


    constructor(
        dyeColorMeta: Int,
        woolColorMeta: Int,
        colorName: String,
        blockColor: BlockColor,
        signColor: BlockColor
    ) : this(
        dyeColorMeta, woolColorMeta, woolColorMeta, colorName,
        "$colorName Dye", blockColor, signColor, blockColor
    )

    constructor(
        dyeColorMeta: Int,
        woolColorMeta: Int,
        itemDyeMeta: Int,
        colorName: String,
        blockColor: BlockColor
    ) : this(
        dyeColorMeta, woolColorMeta, itemDyeMeta, colorName,
        "$colorName Dye", blockColor, blockColor, blockColor
    )

    constructor(
        dyeColorMeta: Int,
        woolColorMeta: Int,
        itemDyeMeta: Int,
        colorName: String,
        blockColor: BlockColor,
        leatherColor: BlockColor,
        signColor: BlockColor
    ) : this(
        dyeColorMeta, woolColorMeta, itemDyeMeta, colorName,
        "$colorName Dye", blockColor, leatherColor, signColor
    )

    companion object {
        private val BY_WOOL_DATA = entries.toTypedArray()
        private val BY_DYE_ITEM_DATA =
            arrayOfNulls<DyeColor>(
                Arrays.stream(BY_WOOL_DATA).mapToInt { obj: DyeColor -> obj.itemDyeMeta }.max()
                    .orElse(0) + 1
            )

        init {
            for (dyeColor in BY_WOOL_DATA) {
                BY_DYE_ITEM_DATA[dyeColor.dyeData] = dyeColor
                BY_DYE_ITEM_DATA[dyeColor.itemDyeMeta] = dyeColor
            }

            for (color in entries) {
                BY_WOOL_DATA[color.woolData and 0x0f] = color
            }
        }


        fun getByDyeData(dyeColorMeta: Int): DyeColor {
            return BY_DYE_ITEM_DATA[dyeColorMeta.coerceIn(0, BY_DYE_ITEM_DATA.size - 1)]
                ?: throw RuntimeException("Unknown DyeColorMeta: $dyeColorMeta")
        }

        fun getByWoolData(woolColorMeta: Int): DyeColor {
            return BY_WOOL_DATA[woolColorMeta and 0x0f]
        }
    }
}
