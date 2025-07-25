package org.chorus_oss.chorus.item

class ItemCamelSpawnEgg : ItemSpawnEgg(ItemID.Companion.CAMEL_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 138

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}