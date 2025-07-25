package org.chorus_oss.chorus.recipe

import com.google.common.base.Objects
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.recipe.descriptor.ItemDescriptor

abstract class BaseRecipe protected constructor(id: String) : Recipe {
    override val recipeId: String = id
    final override val results: MutableList<Item> = ArrayList()
    final override val ingredients: MutableList<ItemDescriptor> = ArrayList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseRecipe) return false
        return Objects.equal(recipeId, other.recipeId)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(recipeId)
    }

    override fun toString(): String {
        return javaClass.simpleName + "{" +
                "id='" + this.recipeId + '\'' +
                '}'
    }
}
