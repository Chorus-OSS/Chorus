package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemPotion
import org.chorus_oss.chorus.registry.RecipeRegistry

class ContainerRecipe(recipeId: String?, input: Item, ingredient: Item, output: Item) :
    MixRecipe(
        recipeId
            ?: RecipeRegistry.computeRecipeIdWithItem(
                listOf(output),
                listOf(input, ingredient),
                RecipeType.CONTAINER
            ),
        input, ingredient, output
    ) {
    constructor(input: Item, ingredient: Item, output: Item) : this(null, input, ingredient, output)

    override fun fastCheck(vararg items: Item): Boolean {
        if (items.size == 2) {
            if (items[1] is ItemPotion) {
                return items[0].equals(ingredient)
            }
        }
        return false
    }

    override val type: RecipeType
        get() = RecipeType.CONTAINER
}
