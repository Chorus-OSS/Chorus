package org.chorus_oss.chorus.item

class ItemCherryBoat : ItemBoat(ItemID.CHERRY_BOAT) {
    override val boatId: Int
        get() = 8

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}