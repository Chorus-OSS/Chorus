package org.chorus_oss.chorus.item

class ItemPaleOakBoat : ItemBoat(ItemID.PALE_OAK_BOAT) {
    override val boatId: Int
        get() = 9

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}