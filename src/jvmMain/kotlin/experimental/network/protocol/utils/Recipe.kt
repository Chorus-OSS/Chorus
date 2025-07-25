package org.chorus_oss.chorus.experimental.network.protocol.utils


import org.chorus_oss.chorus.recipe.RecipeType
import org.chorus_oss.protocol.types.item.ItemInstance
import org.chorus_oss.protocol.types.item.desciptor.ItemDescriptorCount
import org.chorus_oss.protocol.types.recipe.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
operator fun Recipe.Companion.invoke(from: org.chorus_oss.chorus.recipe.Recipe): Recipe {
    return when (from) {
        is org.chorus_oss.chorus.recipe.StonecutterRecipe -> ShapelessRecipe(
            recipeID = from.recipeId,
            input = from.ingredients.map(ItemDescriptorCount::invoke),
            output = from.results.map(ItemInstance::invoke),
            uuid = Uuid(from.uuid),
            block = "stonecutter",
            priority = from.priority,
            unlockingRequirement = RecipeUnlockingRequirement(from.requirement),
            recipeNetworkID = NetworkID++,
        )

        is org.chorus_oss.chorus.recipe.ShapelessRecipe -> ShapelessRecipe(
            recipeID = from.recipeId,
            input = from.ingredients.map(ItemDescriptorCount::invoke),
            output = from.results.map(ItemInstance::invoke),
            uuid = Uuid(from.uuid),
            block = when (from.type) {
                RecipeType.CARTOGRAPHY -> "cartography_table"
                RecipeType.SHAPELESS,
                RecipeType.USER_DATA_SHAPELESS_RECIPE -> "crafting_table"

                else -> throw IllegalArgumentException()
            },
            priority = from.priority,
            unlockingRequirement = RecipeUnlockingRequirement(from.requirement),
            recipeNetworkID = NetworkID++,
        )

        is org.chorus_oss.chorus.recipe.ShapedRecipe -> ShapedRecipe(
            recipeID = from.recipeId,
            width = from.width,
            height = from.height,
            input = (0..<from.height).flatMap { y ->
                (0..<from.width).map { x ->
                    from.getIngredient(x, y)
                }
            }.map(ItemDescriptorCount::invoke),
            output = from.results.map(ItemInstance::invoke),
            uuid = Uuid(from.uuid),
            block = "crafting_table",
            priority = from.priority,
            assumeSymmetry = from.isMirror,
            unlockingRequirement = RecipeUnlockingRequirement(from.requirement),
            recipeNetworkID = NetworkID++,
        )

        is org.chorus_oss.chorus.recipe.SmithingTransformRecipe -> SmithingTransformRecipe(
            recipeID = from.recipeId,
            template = ItemDescriptorCount(from.template),
            base = ItemDescriptorCount(from.base),
            addition = ItemDescriptorCount(from.addition),
            result = ItemInstance(from.result),
            block = "smithing_table",
            recipeNetworkID = NetworkID++,
        )

        is org.chorus_oss.chorus.recipe.SmithingTrimRecipe -> SmithingTrimRecipe(
            recipeID = from.recipeId,
            template = ItemDescriptorCount(from.ingredients[0]),
            base = ItemDescriptorCount(from.ingredients[1]),
            addition = ItemDescriptorCount(from.ingredients[2]),
            block = from.tag,
            recipeNetworkID = NetworkID++,
        )

        is org.chorus_oss.chorus.recipe.MultiRecipe -> MultiRecipe(
            uuid = Uuid(from.id),
            recipeNetworkID = NetworkID++,
        )

        is org.chorus_oss.chorus.recipe.SmeltingRecipe -> {
            val item = from.input.toItem()
            val output = ItemInstance(from.result)
            val block = when (from.type) {
                RecipeType.FURNACE -> "furnace"
                RecipeType.SMOKER -> "smoker"
                RecipeType.BLAST_FURNACE -> "blast_furnace"
                RecipeType.CAMPFIRE -> "campfire"
                RecipeType.SOUL_CAMPFIRE -> "soul_campfire"

                else -> throw IllegalArgumentException()
            }

            when (from.type.name.endsWith("_DATA")) {
                true -> FurnaceDataRecipe(
                    itemNetworkID = item.runtimeId,
                    itemAux = item.damage,
                    output = output,
                    block = block
                )

                false -> FurnaceRecipe(
                    itemNetworkID = item.runtimeId,
                    output = output,
                    block = block
                )
            }
        }

        else -> throw IllegalArgumentException()
    }
}

internal var NetworkID: UInt = 1u