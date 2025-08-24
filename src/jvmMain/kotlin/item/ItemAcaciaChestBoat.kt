package org.chorus_oss.chorus.item

class ItemAcaciaChestBoat : ItemChestBoat(ItemID.ACACIA_CHEST_BOAT) {
    override val boatId: Int
        get() = 4

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}