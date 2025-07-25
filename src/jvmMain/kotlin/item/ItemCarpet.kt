package org.chorus_oss.chorus.item

class ItemCarpet @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.CARPET, meta, count) {
    init {
        adjustName()
    }

    private fun adjustName() {
        when (damage) {
            0 -> {
                name = "White Carpet"
                return
            }

            1 -> {
                name = "Orange Carpet"
                return
            }

            2 -> {
                name = "Magenta Carpet"
                return
            }

            3 -> {
                name = "Light Blue Carpet"
                return
            }

            4 -> {
                name = "Yellow Carpet"
                return
            }

            5 -> {
                name = "Lime Carpet"
                return
            }

            6 -> {
                name = "Pink Carpet"
                return
            }

            7 -> {
                name = "Gray Carpet"
                return
            }

            8 -> {
                name = "Light Gray Carpet"
                return
            }

            9 -> {
                name = "Cyan Carpet"
                return
            }

            10 -> {
                name = "Purple Carpet"
                return
            }

            11 -> {
                name = "Blue Carpet"
                return
            }

            12 -> {
                name = "Brown Carpet"
                return
            }

            13 -> {
                name = "Green Carpet"
                return
            }

            14 -> {
                name = "Red Carpet"
                return
            }

            15 -> {
                name = "Black Carpet"
                return
            }

            else -> name = "Carpet"
        }
    }
}