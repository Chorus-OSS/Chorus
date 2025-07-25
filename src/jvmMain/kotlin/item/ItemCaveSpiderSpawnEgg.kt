package org.chorus_oss.chorus.item

class ItemCaveSpiderSpawnEgg : ItemSpawnEgg(ItemID.Companion.CAVE_SPIDER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 40

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}