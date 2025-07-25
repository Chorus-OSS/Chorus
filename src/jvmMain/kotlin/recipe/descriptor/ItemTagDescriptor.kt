package org.chorus_oss.chorus.recipe.descriptor

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.tags.ItemTags

class ItemTagDescriptor(@JvmField val itemTag: String, override val count: Int) : ItemDescriptor, Cloneable {
    override val type: ItemDescriptorType
        get() = ItemDescriptorType.ITEM_TAG

    override fun toItem(): Item {
        throw UnsupportedOperationException()
    }

    override fun match(item: Item): Boolean {
        return item.getCount() >= count && ItemTags.getTagSet(item.id).contains(itemTag)
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): ItemDescriptor {
        return super<Cloneable>.clone() as ItemDescriptor
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is ItemTagDescriptor) return false
        val `this$itemTag`: Any = this.itemTag
        val `other$itemTag`: Any = o.itemTag
        if (`this$itemTag` != `other$itemTag`) return false
        return this.count == o.count
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$itemTag`: Any = this.itemTag
        result = result * PRIME + `$itemTag`.hashCode()
        result = result * PRIME + this.count
        return result
    }

    override fun toString(): String {
        return "ItemTagDescriptor(itemTag=" + this.itemTag + ", count=" + this.count + ")"
    }
}
