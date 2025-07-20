package org.chorus_oss.chorus.utils

enum class MinecartType(
    val id: Int
) {
    MINECART_EMPTY(0),
    MINECART_CHEST(1),
    MINECART_FURNACE(2),
    MINECART_TNT(3),
    MINECART_MOB_SPAWNER(4),
    MINECART_HOPPER(5),
    MINECART_COMMAND_BLOCK(6),
    MINECART_UNKNOWN(-1);

    companion object {
        private val TYPES: Map<Int, MinecartType> = entries.associateBy { it.id }

        /**
         * Returns of an instance of Minecart-variants
         *
         * @param types The number of minecart
         * @return Integer
         */
        fun valueOf(types: Int): MinecartType {
            val what = TYPES[types]
            return what ?: MINECART_UNKNOWN
        }
    }
}
