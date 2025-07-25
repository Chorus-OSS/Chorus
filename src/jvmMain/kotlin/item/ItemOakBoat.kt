package org.chorus_oss.chorus.item

class ItemOakBoat : ItemBoat(ItemID.Companion.OAK_BOAT) {
    override val boatId: Int
        get() = 0

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}