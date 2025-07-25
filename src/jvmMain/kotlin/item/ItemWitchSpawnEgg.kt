package org.chorus_oss.chorus.item

class ItemWitchSpawnEgg : ItemSpawnEgg(ItemID.Companion.WITCH_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 45

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}