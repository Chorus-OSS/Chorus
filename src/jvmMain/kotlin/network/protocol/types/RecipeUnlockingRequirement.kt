package org.chorus_oss.chorus.network.protocol.types

import org.chorus_oss.chorus.recipe.descriptor.ItemDescriptor

class RecipeUnlockingRequirement(var context: UnlockingContext) {
    var ingredients: List<ItemDescriptor> = listOf()

    enum class UnlockingContext {
        NONE,
        ALWAYS_UNLOCKED,
        PLAYER_IN_WATER,
        PLAYER_HAS_MANY_ITEMS;

        companion object {
            private val VALUES = entries.toTypedArray()

            fun from(id: Int): UnlockingContext {
                return VALUES[id]
            }
        }
    }

    val isInvalid: Boolean
        get() = ingredients.isEmpty() && this.context == UnlockingContext.NONE

    companion object {
        val INVALID: RecipeUnlockingRequirement = RecipeUnlockingRequirement(UnlockingContext.NONE)
    }
}