package org.chorus_oss.chorus.item

class ItemSkeletonSpawnEgg : ItemSpawnEgg(ItemID.Companion.SKELETON_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 34

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}