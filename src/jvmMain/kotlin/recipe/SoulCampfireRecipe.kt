package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.recipe.descriptor.DefaultDescriptor
import org.chorus_oss.chorus.registry.RecipeRegistry

class SoulCampfireRecipe(recipeId: String?, result: Item, ingredient: Item) :
    CampfireRecipe(
        recipeId
            ?: RecipeRegistry.computeRecipeId(
                listOf(result),
                listOf(DefaultDescriptor(ingredient)),
                RecipeType.SOUL_CAMPFIRE
            ), result, ingredient
    ) {
    constructor(result: Item, ingredient: Item) : this(null, result, ingredient)

    override val type: RecipeType
        get() = RecipeType.SOUL_CAMPFIRE
}
