package org.chorus_oss.chorus.recipe.descriptor

import org.chorus_oss.chorus.item.Item

class DefaultDescriptor(@JvmField val item: Item) : ItemDescriptor, Cloneable {
    override val type: ItemDescriptorType
        get() = ItemDescriptorType.DEFAULT

    override fun toItem(): Item {
        return item.clone()
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): ItemDescriptor {
        return super<Cloneable>.clone() as ItemDescriptor
    }

    override val count: Int
        get() = item.getCount()

    override fun match(item: Item): Boolean {
        return this.item.equals(item, true, false)
    }

    override fun hashCode(): Int {
        return item.hashCode()
    }

    override fun toString(): String {
        return "DefaultDescriptor(item=" + this.item + ")"
    }
}