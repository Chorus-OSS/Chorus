package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.recipe.BrewingRecipe
import org.chorus_oss.chorus.recipe.ContainerRecipe
import org.chorus_oss.chorus.recipe.Recipe
import org.chorus_oss.protocol.packets.CraftingDataPacket
import org.chorus_oss.protocol.types.recipe.PotionContainerChangeRecipe
import org.chorus_oss.protocol.types.recipe.PotionRecipe

operator fun CraftingDataPacket.Companion.invoke(
    recipes: List<Recipe>,
    potionRecipes: List<BrewingRecipe>,
    potionContainerChangeRecipes: List<ContainerRecipe>,
    cleanRecipes: Boolean,
): CraftingDataPacket {
    return CraftingDataPacket(
        recipes = recipes.map(org.chorus_oss.protocol.types.recipe.Recipe::invoke),
        potionRecipes = potionRecipes.map {
            PotionRecipe(
                inputPotionID = it.input.runtimeId,
                inputPotionMetadata = it.input.damage,
                reagentItemID = it.ingredient.runtimeId,
                reagentItemMetadata = it.ingredient.damage,
                outputPotionID = it.result.runtimeId,
                outputPotionMetadata = it.result.damage,
            )
        },
        potionContainerChangeRecipes = potionContainerChangeRecipes.map {
            PotionContainerChangeRecipe(
                inputItemID = it.input.runtimeId,
                reagentItemID = it.ingredient.runtimeId,
                outputItemID = it.result.runtimeId,
            )
        },
        materialReducers = emptyList(),
        clearRecipes = cleanRecipes,
    )
}