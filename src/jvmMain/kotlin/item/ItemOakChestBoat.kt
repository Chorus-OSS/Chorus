package org.chorus_oss.chorus.item

class ItemOakChestBoat : ItemChestBoat(ItemID.Companion.OAK_CHEST_BOAT) {
    override val boatId: Int
        get() = 0

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}