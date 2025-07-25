package org.chorus_oss.chorus.item

class ItemAcaciaBoat : ItemBoat(ItemID.Companion.ACACIA_BOAT) {
    override val boatId: Int
        get() = 4

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}