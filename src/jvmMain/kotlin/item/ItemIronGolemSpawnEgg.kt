package org.chorus_oss.chorus.item

class ItemIronGolemSpawnEgg : ItemSpawnEgg(ItemID.Companion.IRON_GOLEM_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 20

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}