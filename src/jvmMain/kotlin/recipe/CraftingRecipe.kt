package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.network.protocol.types.RecipeUnlockingRequirement
import java.util.*


abstract class CraftingRecipe protected constructor(
    id: String,
    /**
     * Get the priority of this recipe,
     * the lower the value, the higher the priority.
     * and the same output recipe will be to match the higher priority
     *
     * @return the priority
     */
    val priority: Int, recipeUnlockingRequirement: RecipeUnlockingRequirement?
) :
    BaseRecipe(id) {
    open lateinit var uuid: UUID
    val requirement: RecipeUnlockingRequirement =
        recipeUnlockingRequirement ?: RecipeUnlockingRequirement(RecipeUnlockingRequirement.UnlockingContext.NONE)
}
