package org.chorus_oss.chorus.recipe.descriptor

import org.chorus_oss.chorus.item.Item

class DeferredDescriptor(val fullName: String, val auxValue: Int, override val count: Int) : ItemDescriptor, Cloneable {
    override val type: ItemDescriptorType
        get() = ItemDescriptorType.DEFERRED

    override fun toItem(): Item {
        throw UnsupportedOperationException()
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): ItemDescriptor {
        return super<Cloneable>.clone() as ItemDescriptor
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is DeferredDescriptor) return false
        val `this$fullName`: Any = this.fullName
        val `other$fullName`: Any = o.fullName
        if (`this$fullName` != `other$fullName`) return false
        if (this.auxValue != o.auxValue) return false
        return this.count == o.count
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$fullName`: Any = this.fullName
        result = result * PRIME + `$fullName`.hashCode()
        result = result * PRIME + this.auxValue
        result = result * PRIME + this.count
        return result
    }

    override fun toString(): String {
        return "DeferredDescriptor(fullName=" + this.fullName + ", auxValue=" + this.auxValue + ", count=" + this.count + ")"
    }
}
