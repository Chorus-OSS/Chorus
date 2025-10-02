package org.chorus_oss.chorus.item

class ItemRavagerSpawnEgg : ItemSpawnEgg(ItemID.RAVAGER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 59

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}