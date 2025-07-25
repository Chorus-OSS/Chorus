package org.chorus_oss.chorus.item

class ItemStriderSpawnEgg : ItemSpawnEgg(ItemID.Companion.STRIDER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 125

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}