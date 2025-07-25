package org.chorus_oss.chorus.item

class ItemChickenSpawnEgg : ItemSpawnEgg(ItemID.Companion.CHICKEN_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 10

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}