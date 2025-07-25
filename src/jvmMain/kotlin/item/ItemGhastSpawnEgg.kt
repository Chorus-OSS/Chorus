package org.chorus_oss.chorus.item

class ItemGhastSpawnEgg : ItemSpawnEgg(ItemID.Companion.GHAST_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 41

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}