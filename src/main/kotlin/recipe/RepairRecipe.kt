package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.inventory.InventoryType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.recipe.descriptor.DefaultDescriptor
import org.chorus_oss.chorus.registry.RecipeRegistry

class RepairRecipe(val inventoryType: InventoryType, result: Item, ingredients: Collection<Item>) :
    BaseRecipe(RecipeRegistry.computeRecipeIdWithItem(listOf(result), ingredients, RecipeType.REPAIR)) {
    init {
        results.add(result.clone())
        for (item in ingredients) {
            require(item.getCount() >= 1) { "Recipe Ingredient amount was not 1 (value: " + item.getCount() + ")" }
            this.ingredients.add(DefaultDescriptor(item.clone()))
        }
    }

    override fun match(input: Input): Boolean {
        return true
    }

    override val type: RecipeType
        get() = RecipeType.REPAIR
}
