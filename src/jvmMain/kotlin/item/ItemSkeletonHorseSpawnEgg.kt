package org.chorus_oss.chorus.item

class ItemSkeletonHorseSpawnEgg : ItemSpawnEgg(ItemID.Companion.SKELETON_HORSE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 26

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}