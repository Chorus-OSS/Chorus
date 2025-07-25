package org.chorus_oss.chorus.item

class ItemBambooChestRaft : ItemChestBoat(ItemID.Companion.BAMBOO_CHEST_RAFT) {
    override val boatId: Int
        get() = 7

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}