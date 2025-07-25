package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.RecipeUnlockingRequirement
import org.chorus_oss.chorus.recipe.descriptor.DefaultDescriptor
import org.chorus_oss.chorus.registry.RecipeRegistry
import java.util.*

class StonecutterRecipe(
    recipeId: String?,
    override var uuid: UUID,
    priority: Int,
    result: Item,
    ingredient: Item,
    recipeUnlockingRequirement: RecipeUnlockingRequirement?
) :
    CraftingRecipe(
        recipeId
            ?: RecipeRegistry.computeRecipeIdWithItem(
                listOf(result),
                listOf(ingredient),
                RecipeType.STONECUTTER
            ), priority, recipeUnlockingRequirement
    ) {

    constructor(recipeId: String?, priority: Int, result: Item, ingredient: Item) : this(
        recipeId,
        UUID.randomUUID(),
        priority,
        result,
        ingredient
    )

    constructor(recipeId: String?, uuid: UUID, priority: Int, result: Item, ingredient: Item) : this(
        recipeId,
        uuid,
        priority,
        result,
        ingredient,
        null
    )

    init {
        results.add(result.clone())
        require(ingredient.getCount() >= 1) { "Recipe '" + recipeId + "' Ingredient amount was not 1 (value: " + ingredient.getCount() + ")" }
        ingredients.add(DefaultDescriptor(ingredient.clone()))
    }

    val result: Item
        get() = results.first().clone()

    val ingredient: Item
        get() = ingredients.first().toItem().clone()

    override fun match(input: Input): Boolean {
        return true
    }

    override val type: RecipeType
        get() = RecipeType.STONECUTTER
}
