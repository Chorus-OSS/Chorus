package org.chorus_oss.chorus.item

class ItemJungleBoat : ItemBoat(ItemID.Companion.JUNGLE_BOAT) {
    override val boatId: Int
        get() = 3

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}