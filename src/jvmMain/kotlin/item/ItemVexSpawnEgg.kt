package org.chorus_oss.chorus.item

class ItemVexSpawnEgg : ItemSpawnEgg(ItemID.Companion.VEX_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 105

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}