package org.chorus_oss.chorus.item

class ItemHappyGhastSpawnEgg : ItemSpawnEgg(ItemID.HAPPY_GHAST_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 147

    override var damage: Int
        get() = super.damage
        set(_) = Unit
}