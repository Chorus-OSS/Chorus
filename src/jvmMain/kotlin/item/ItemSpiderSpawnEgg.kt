package org.chorus_oss.chorus.item

class ItemSpiderSpawnEgg : ItemSpawnEgg(ItemID.Companion.SPIDER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 35

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}