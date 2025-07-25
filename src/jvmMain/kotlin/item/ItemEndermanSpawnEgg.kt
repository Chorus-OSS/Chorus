package org.chorus_oss.chorus.item

class ItemEndermanSpawnEgg : ItemSpawnEgg(ItemID.Companion.ENDERMAN_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 38

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}