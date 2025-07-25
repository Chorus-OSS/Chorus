package org.chorus_oss.chorus.network.protocol.types.itemstack.request.action

enum class ItemStackRequestActionType(val id: Int) {
    TAKE(0),
    PLACE(1),
    SWAP(2),
    DROP(3),
    DESTROY(4),
    CONSUME(5),
    CREATE(6),

    @Deprecated("since v712")
    PLACE_IN_ITEM_CONTAINER(7),

    @Deprecated("since v712")
    TAKE_FROM_ITEM_CONTAINER(8),
    LAB_TABLE_COMBINE(9),
    BEACON_PAYMENT(10),
    MINE_BLOCK(11),
    CRAFT_RECIPE(12),
    CRAFT_RECIPE_AUTO(13),
    CRAFT_CREATIVE(14),
    CRAFT_RECIPE_OPTIONAL(15),
    CRAFT_REPAIR_AND_DISENCHANT(16),
    CRAFT_LOOM(17),
    CRAFT_NON_IMPLEMENTED_DEPRECATED(18),
    CRAFT_RESULTS_DEPRECATED(19);

    companion object {
        private val VALUES = entries.associateBy { it.id }

        fun fromId(id: Int): ItemStackRequestActionType {
            return VALUES[id] ?: throw RuntimeException("Unknown ItemStackRequestActionType ID: $id")
        }
    }
}
