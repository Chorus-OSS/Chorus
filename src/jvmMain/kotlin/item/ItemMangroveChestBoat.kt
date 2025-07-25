package org.chorus_oss.chorus.item

class ItemMangroveChestBoat : ItemChestBoat(ItemID.Companion.MANGROVE_CHEST_BOAT) {
    override val boatId: Int
        get() = 6

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}