package org.chorus_oss.chorus.item

class ItemSpruceChestBoat : ItemChestBoat(ItemID.Companion.SPRUCE_CHEST_BOAT) {
    override val boatId: Int
        get() = 1

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}