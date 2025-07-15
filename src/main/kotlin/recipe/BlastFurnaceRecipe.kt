package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.recipe.descriptor.DefaultDescriptor
import org.chorus_oss.chorus.registry.RecipeRegistry

class BlastFurnaceRecipe(recipeId: String?, result: Item, ingredient: Item) :
    SmeltingRecipe(
        recipeId
            ?: RecipeRegistry.computeRecipeId(
                listOf(result),
                listOf(DefaultDescriptor(ingredient)),
                RecipeType.BLAST_FURNACE
            )
    ) {
    constructor(result: Item, ingredient: Item) : this(null, result, ingredient)


    init {
        ingredients.add(DefaultDescriptor(ingredient.clone()))
        results.add(result.clone())
    }

    override fun match(input: Input): Boolean {
        return false
    }

    override val type: RecipeType
        get() = RecipeType.BLAST_FURNACE
}
