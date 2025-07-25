package org.chorus_oss.chorus.item

class ItemBirchChestBoat : ItemChestBoat(ItemID.Companion.BIRCH_CHEST_BOAT) {
    override val boatId: Int
        get() = 2

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}