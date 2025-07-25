package org.chorus_oss.chorus.item

class ItemRavagerSpawnEgg : ItemSpawnEgg(ItemID.Companion.RAVAGER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 59

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}