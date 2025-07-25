package org.chorus_oss.chorus.item

class ItemBambooRaft : ItemBoat(ItemID.Companion.BAMBOO_RAFT) {
    override val boatId: Int
        get() = 7

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}