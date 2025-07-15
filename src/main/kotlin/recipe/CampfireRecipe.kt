package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.recipe.descriptor.DefaultDescriptor
import org.chorus_oss.chorus.registry.RecipeRegistry

open class CampfireRecipe(recipeId: String?, result: Item, ingredient: Item) :
    SmeltingRecipe(
        recipeId
            ?: RecipeRegistry.computeRecipeId(
                listOf(result),
                listOf(DefaultDescriptor(ingredient)),
                RecipeType.CAMPFIRE
            )
    ) {
    constructor(result: Item, ingredient: Item) : this(null, result, ingredient)

    init {
        results.add(result)
        ingredients.add(DefaultDescriptor(ingredient))
    }

    override fun match(input: Input): Boolean {
        return false
    }

    override val type: RecipeType
        get() = RecipeType.CAMPFIRE
}
