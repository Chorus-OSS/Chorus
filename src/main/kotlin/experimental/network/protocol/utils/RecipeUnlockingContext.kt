package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.protocol.types.item.desciptor.ItemDescriptorCount
import org.chorus_oss.protocol.types.recipe.RecipeUnlockingRequirement

operator fun RecipeUnlockingRequirement.Companion.invoke(from: org.chorus_oss.chorus.network.protocol.types.RecipeUnlockingRequirement): RecipeUnlockingRequirement {
    return RecipeUnlockingRequirement(
        context = RecipeUnlockingRequirement.Companion.UnlockingContext.entries[from.context.ordinal],
        ingredients = from.ingredients.map(ItemDescriptorCount::invoke)
    )
}