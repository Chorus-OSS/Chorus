package org.chorus_oss.chorus.item

class ItemSnifferSpawnEgg : ItemSpawnEgg(ItemID.SNIFFER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 139

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}