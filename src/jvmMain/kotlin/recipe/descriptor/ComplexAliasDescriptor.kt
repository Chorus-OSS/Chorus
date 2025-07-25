package org.chorus_oss.chorus.recipe.descriptor

import org.chorus_oss.chorus.item.Item

class ComplexAliasDescriptor(var name: String) : ItemDescriptor, Cloneable {
    override val type: ItemDescriptorType
        get() = ItemDescriptorType.COMPLEX_ALIAS

    override fun toItem(): Item {
        throw UnsupportedOperationException()
    }

    override fun clone(): ItemDescriptor {
        return super<Cloneable>.clone() as ItemDescriptor
    }

    override val count: Int
        get() = 0
}
