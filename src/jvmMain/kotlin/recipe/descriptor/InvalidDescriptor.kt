package org.chorus_oss.chorus.recipe.descriptor

import org.chorus_oss.chorus.item.Item

class InvalidDescriptor private constructor() : ItemDescriptor, Cloneable {
    override var count: Int = 0

    override val type: ItemDescriptorType
        get() = ItemDescriptorType.INVALID

    override fun toItem(): Item {
        throw UnsupportedOperationException()
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): ItemDescriptor {
        return super<Cloneable>.clone() as ItemDescriptor
    }

    override fun toString(): String {
        return "InvalidDescriptor(count=" + this.count + ")"
    }

    companion object {
        val INSTANCE: InvalidDescriptor = InvalidDescriptor()
    }
}
