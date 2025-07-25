package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.recipe.descriptor.ItemDescriptor

class SmithingTrimRecipe(
    id: String,
    base: ItemDescriptor,
    addition: ItemDescriptor,
    template: ItemDescriptor,
    tag: String
) :
    BaseRecipe(id) {
    val tag: String

    init {
        results.clear()
        ingredients.add(template)
        ingredients.add(base)
        ingredients.add(addition)
        this.tag = tag
    }

    override fun match(input: Input): Boolean {
        return false
    }

    override val type: RecipeType
        get() = RecipeType.SMITHING_TRIM
}
