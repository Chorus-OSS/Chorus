package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.registry.RecipeRegistry


class BrewingRecipe(recipeId: String?, input: Item, ingredient: Item, output: Item) :
    MixRecipe(
        recipeId
            ?: RecipeRegistry.computeRecipeIdWithItem(
                listOf(output),
                listOf(input, ingredient),
                RecipeType.BREWING
            ), input, ingredient, output
    ) {
    constructor(input: Item, ingredient: Item, output: Item) : this(null, input, ingredient, output)

    override val type: RecipeType
        get() = RecipeType.BREWING
}