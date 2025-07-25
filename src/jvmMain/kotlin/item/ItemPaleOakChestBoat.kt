package org.chorus_oss.chorus.item

class ItemPaleOakChestBoat : ItemChestBoat(ItemID.Companion.PALE_OAK_CHEST_BOAT) {
    override val boatId: Int
        get() = 9

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}