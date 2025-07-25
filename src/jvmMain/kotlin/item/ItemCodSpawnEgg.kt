package org.chorus_oss.chorus.item

class ItemCodSpawnEgg : ItemSpawnEgg(ItemID.Companion.COD_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 112

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}