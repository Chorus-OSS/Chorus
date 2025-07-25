package org.chorus_oss.chorus.item

class ItemHorseSpawnEgg : ItemSpawnEgg(ItemID.Companion.HORSE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 23

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}