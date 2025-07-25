package org.chorus_oss.chorus.item

class ItemZoglinSpawnEgg : ItemSpawnEgg(ItemID.Companion.ZOGLIN_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 126

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}