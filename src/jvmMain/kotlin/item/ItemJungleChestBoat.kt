package org.chorus_oss.chorus.item

class ItemJungleChestBoat : ItemChestBoat(ItemID.Companion.JUNGLE_CHEST_BOAT) {
    override val boatId: Int
        get() = 3

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}