package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.RecipeUnlockingRequirement
import org.chorus_oss.chorus.recipe.descriptor.DefaultDescriptor
import org.chorus_oss.chorus.recipe.descriptor.ItemDescriptor
import org.chorus_oss.chorus.registry.RecipeRegistry
import java.util.*

open class ShapelessRecipe @JvmOverloads constructor(
    recipeId: String?,
    override var uuid: UUID,
    priority: Int,
    result: Item,
    ingredients: List<ItemDescriptor>,
    recipeUnlockingRequirement: RecipeUnlockingRequirement? = null
) :
    CraftingRecipe(
        recipeId
            ?: RecipeRegistry.computeRecipeId(
                listOf(result),
                ingredients,
                RecipeType.SHAPELESS
            ), priority, recipeUnlockingRequirement
    ) {
    constructor(result: Item, ingredients: Collection<Item>) : this(null, 10, result, ingredients)

    constructor(recipeId: String?, priority: Int, result: Item, ingredients: Collection<Item>) : this(
        recipeId,
        priority,
        result,
        ingredients.stream().map<ItemDescriptor> { item: Item ->
            DefaultDescriptor(item)
        }.toList()
    )

    constructor(recipeId: String?, priority: Int, result: Item, ingredients: List<ItemDescriptor>) : this(
        recipeId,
        UUID.randomUUID(),
        priority,
        result,
        ingredients
    )

    init {
        results.add(result.clone())
        require(ingredients.size <= 9) { "Shapeless recipes cannot have more than 9 ingredients" }
        this.ingredients.addAll(ingredients)
    }

    val result: Item
        get() = results[0]

    override val type: RecipeType
        get() = RecipeType.SHAPELESS

    override fun match(input: Input): Boolean {
        val data = input.data
        val flatInputItem: MutableList<Item> = ArrayList()
        for (i in 0..<input.row) {
            for (j in 0..<input.col) {
                if (!data[j][i].isNothing) {
                    flatInputItem.add(data[j][i])
                }
            }
        }
        next@ for (i in flatInputItem) {
            for (ingredient in ingredients) {
                if (ingredient.match(i)) continue@next
            }
            return false
        }
        return true
    }
}
