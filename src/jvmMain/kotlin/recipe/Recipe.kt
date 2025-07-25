package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.recipe.descriptor.ItemDescriptor


interface Recipe {
    val recipeId: String

    val results: List<Item>

    val ingredients: List<ItemDescriptor>

    fun match(input: Input): Boolean

    fun fastCheck(vararg items: Item): Boolean {
        if (ingredients.size != items.size) return false
        return items.all {
            ingredients.any { i -> i.match(it) }
        }
    }

    val type: RecipeType
}
