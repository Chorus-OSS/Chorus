package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.recipe.descriptor.ItemDescriptor

class SmithingTransformRecipe(
    recipeId: String,
    result: Item,
    base: ItemDescriptor,
    addition: ItemDescriptor,
    template: ItemDescriptor
) :
    BaseRecipe(recipeId) {
    init {
        ingredients.add(base)
        ingredients.add(addition)
        ingredients.add(template)
        results.add(result)
    }

    override fun match(input: Input): Boolean {
        return false
    }

    val result: Item
        get() = results.first()

    override val type: RecipeType
        get() = RecipeType.SMITHING_TRANSFORM

    val base: ItemDescriptor
        get() = ingredients.first()

    val addition: ItemDescriptor
        get() = ingredients[1]

    val template: ItemDescriptor
        get() = ingredients[2]
}
