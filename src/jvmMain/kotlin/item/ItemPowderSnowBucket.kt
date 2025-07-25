package org.chorus_oss.chorus.item

class ItemPowderSnowBucket : ItemBucket(ItemID.Companion.POWDER_SNOW_BUCKET) {
    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}