package org.chorus_oss.chorus.item

class ItemVillagerSpawnEgg : ItemSpawnEgg(ItemID.Companion.VILLAGER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 115

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}