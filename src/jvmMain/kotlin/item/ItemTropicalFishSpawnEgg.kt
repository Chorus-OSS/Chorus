package org.chorus_oss.chorus.item

class ItemTropicalFishSpawnEgg : ItemSpawnEgg(ItemID.Companion.TROPICAL_FISH_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 111

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}