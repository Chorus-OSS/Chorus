package org.chorus_oss.chorus.item

class ItemCherryChestBoat : ItemChestBoat(ItemID.Companion.CHERRY_CHEST_BOAT) {
    override val boatId: Int
        get() = 8

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}