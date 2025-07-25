package org.chorus_oss.chorus.item

class ItemSpruceBoat : ItemBoat(ItemID.Companion.SPRUCE_BOAT) {
    override val boatId: Int
        get() = 1

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}