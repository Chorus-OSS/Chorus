package org.chorus_oss.chorus.item

class ItemDarkOakBoat : ItemBoat(ItemID.Companion.DARK_OAK_BOAT) {
    override val boatId: Int
        get() = 5

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}