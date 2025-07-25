package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.recipe.descriptor.DefaultDescriptor

abstract class MixRecipe(recipeId: String, input: Item, ingredient: Item, output: Item) :
    BaseRecipe(recipeId) {

    init {
        ingredients.add(DefaultDescriptor(input))
        ingredients.add(DefaultDescriptor(ingredient))
        results.add(output)
    }

    val ingredient: Item
        get() = ingredients[1].toItem()

    val input: Item
        get() = ingredients[0].toItem()

    val result: Item
        get() = results[0]

    override fun match(input: Input): Boolean {
        return true
    }
}
