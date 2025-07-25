package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import java.util.function.Function

class EntityItemSlot {
    var slot: Byte = 0
    var block: CompoundTag? = null
    var canDestroy: List<String>? = null
    var canPlaceOn: List<String>? = null
    var count: Byte = 0
    var damage: Short = 0
    var name: String = ""
    var tag: CompoundTag? = null
    var wasPickedUp: Boolean = false

    constructor()
    constructor(nbt: CompoundTag) {
        this.slot = nbt.getByte(TAG_SLOT)
        this.block = if (nbt.contains(TAG_BLOCK)) nbt.getCompound(TAG_BLOCK) else null
        if (nbt.contains(TAG_CAN_DESTROY)) {
            val canDestroy: ListTag<StringTag> = nbt.getList(
                TAG_CAN_DESTROY,
                StringTag::class.java
            )
            this.canDestroy = canDestroy.all.stream().map(Function { obj: StringTag -> obj.parseValue() }).toList()
        }
        if (nbt.contains(TAG_CAN_PLACE_ON)) {
            val canPlaceOn: ListTag<StringTag> = nbt.getList(
                TAG_CAN_PLACE_ON,
                StringTag::class.java
            )
            this.canPlaceOn = canPlaceOn.all.stream().map(Function { obj: StringTag -> obj.parseValue() }).toList()
        }
        this.count = nbt.getByte(TAG_COUNT)
        this.damage = nbt.getShort(TAG_DAMAGE)
        this.name = nbt.getString(TAG_NAME)
        this.tag = if (nbt.contains(TAG_TAG)) nbt.getCompound(TAG_TAG) else null
        this.wasPickedUp = nbt.getBoolean(TAG_WAS_PICKED_UP)
    }

    fun get(): CompoundTag {
        val nbt: CompoundTag = CompoundTag()
        nbt.putByte(TAG_SLOT, slot.toInt())
        if (this.block != null) {
            nbt.putCompound(TAG_BLOCK, this.block!!)
        }
        if (this.canDestroy != null) {
            val canDestroy: List<StringTag> = canDestroy!!.stream().map(
                Function { data: String? -> StringTag(data!!) }).toList()
            nbt.putList(TAG_CAN_DESTROY, ListTag(canDestroy))
        }
        if (this.canPlaceOn != null) {
            val canPlaceOn: List<StringTag> = canPlaceOn!!.stream().map(
                Function { data: String? -> StringTag(data!!) }).toList()
            nbt.putList(TAG_CAN_PLACE_ON, ListTag(canPlaceOn))
        }
        nbt.putByte(TAG_COUNT, count.toInt())
        nbt.putShort(TAG_DAMAGE, damage.toInt())
        nbt.putString(TAG_NAME, this.name)
        if (this.tag != null) {
            nbt.putCompound(TAG_TAG, this.tag!!)
        }
        nbt.putBoolean(TAG_WAS_PICKED_UP, this.wasPickedUp)
        return nbt
    }

    companion object {
        const val TAG_SLOT: String = "Slot"
        const val TAG_BLOCK: String = "Block"
        const val TAG_CAN_DESTROY: String = "CanDestroy"
        const val TAG_CAN_PLACE_ON: String = "CanPlaceOn"
        const val TAG_COUNT: String = "Count"
        const val TAG_DAMAGE: String = "Damage"
        const val TAG_NAME: String = "Name"
        const val TAG_TAG: String = "tag"
        const val TAG_WAS_PICKED_UP: String = "WasPickedUp"
    }
}
