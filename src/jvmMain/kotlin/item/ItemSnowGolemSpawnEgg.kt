package org.chorus_oss.chorus.item

class ItemSnowGolemSpawnEgg : ItemSpawnEgg(ItemID.SNOW_GOLEM_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 21

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}