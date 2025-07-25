package org.chorus_oss.chorus.item

class ItemMangroveBoat : ItemBoat(ItemID.Companion.MANGROVE_BOAT) {
    override val boatId: Int
        get() = 6

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}