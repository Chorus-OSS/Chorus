package org.chorus_oss.chorus.item

class ItemWitherSkeletonSpawnEgg : ItemSpawnEgg(ItemID.Companion.WITHER_SKELETON_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 48

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}