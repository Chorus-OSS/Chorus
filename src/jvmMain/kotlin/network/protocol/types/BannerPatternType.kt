package org.chorus_oss.chorus.network.protocol.types

enum class BannerPatternType(val typeId: Int, val code: String, val patternName: String) {
    BORDER(0, "bo", "Border"),
    BRICKS(1, "bri", "Bricks"),
    CIRCLE(2, "mc", "Circle"),
    CREEPER(3, "cre", "Creeper"),
    CROSS(4, "cr", "Cross"),
    CURLY_BORDER(5, "cbo", "Curly Border"),
    DIAGONAL_LEFT(6, "lud", "Diagonal Left"),
    DIAGONAL_RIGHT(7, "rd", "Diagonal Right"),
    DIAGONAL_UP_LEFT(8, "ld", "Diagonal Up Left"),
    DIAGONAL_UP_RIGHT(9, "rud", "Diagonal Up Right"),
    FLOWER(10, "flo", "Flower"),
    GRADIENT(11, "gra", "Gradient"),
    GRADIENT_UP(12, "gru", "Gradient Up"),
    HALF_HORIZONTAL(13, "hh", "Half Horizontal"),
    HALF_HORIZONTAL_BOTTOM(14, "hhb", "Half Horizontal Bottom"),
    HALF_VERTICAL(15, "vh", "Half Vertical"),
    HALF_VERTICAL_RIGHT(16, "vhr", "Half Vertical Right"),
    MOJANG(17, "moj", "Mojang"),
    RHOMBUS(18, "mr", "Rhombus"),
    SKULL(19, "sku", "Skull"),
    SMALL_STRIPES(20, "ss", "Small Stripes"),
    SQUARE_BOTTOM_LEFT(21, "bl", "Square Bottom Left"),
    SQUARE_BOTTOM_RIGHT(22, "br", "Square Bottom Right"),
    SQUARE_TOP_LEFT(23, "tl", "Square Top Left"),
    SQUARE_TOP_RIGHT(24, "tr", "Square Top Right"),
    STRAIGHT_CROSS(25, "sc", "Straight Cross"),
    STRIPE_BOTTOM(26, "bs", "Stripe Bottom"),
    STRIPE_CENTER(27, "cs", "Stripe Center"),
    STRIPE_DOWN_LEFT(28, "dls", "Stripe Down Left"),
    STRIPE_DOWN_RIGHT(29, "drs", "Stripe Down Right"),
    STRIPE_LEFT(30, "ls", "Stripe Left"),
    STRIPE_MIDDLE(31, "ms", "Stripe Middle"),
    STRIPE_RIGHT(32, "rs", "Stripe Right"),
    STRIPE_TOP(33, "ts", "Stripe Top"),
    TRIANGLE_BOTTOM(34, "bt", "Triangle Bottom"),
    TRIANGLE_TOP(35, "tt", "Triangle Top"),
    TRIANGLES_BOTTOM(36, "bts", "Triangles Bottom"),
    TRIANGLES_TOP(37, "tts", "Triangles Top"),
    GLOBE(38, "glb", "Globe"),
    PIGLIN(39, "pig", "Piglin");

    companion object {
        private var code2PatternType: MutableMap<String, BannerPatternType> = HashMap()
        private var typeId2PatternType: MutableMap<Int, BannerPatternType> = HashMap()

        init {
            val values = entries.toTypedArray()
            for (value in values) {
                code2PatternType[value.code] = value
                typeId2PatternType[value.typeId] = value
            }
        }

        fun fromCode(code: String): BannerPatternType {
            return code2PatternType[code] ?: throw RuntimeException("Unknown BannerPatternType Code: $code")
        }

        fun fromTypeId(typeId: Int): BannerPatternType {
            return typeId2PatternType[typeId] ?: throw RuntimeException("Unknown BannerPatternType TypeId: $typeId")
        }
    }
}
