package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.recipe.descriptor.ItemDescriptor

import java.util.*


class MultiRecipe(val id: UUID) : Recipe {
    override val recipeId: String
        get() = id.toString()

    override val results: List<Item>
        get() = listOf()

    override val ingredients: List<ItemDescriptor>
        get() = listOf()

    override fun match(input: Input): Boolean {
        return true
    }

    override val type: RecipeType
        get() = RecipeType.MULTI
}
