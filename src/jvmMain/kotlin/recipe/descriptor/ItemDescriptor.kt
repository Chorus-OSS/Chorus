package org.chorus_oss.chorus.recipe.descriptor

import org.chorus_oss.chorus.item.Item

interface ItemDescriptor : Cloneable {
    val type: ItemDescriptorType

    fun toItem(): Item

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): ItemDescriptor

    val count: Int

    fun match(item: Item): Boolean {
        return false
    }
}
