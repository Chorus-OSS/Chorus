package org.chorus_oss.chorus.item

class ItemArmadilloSpawnEgg : ItemSpawnEgg(ItemID.Companion.ARMADILLO_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 142

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}