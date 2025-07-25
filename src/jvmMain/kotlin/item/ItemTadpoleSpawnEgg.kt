package org.chorus_oss.chorus.item

class ItemTadpoleSpawnEgg : ItemSpawnEgg(ItemID.Companion.TADPOLE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 133

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}