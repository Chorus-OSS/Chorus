package org.chorus_oss.chorus.item

class ItemPigSpawnEgg : ItemSpawnEgg(ItemID.Companion.PIG_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 12

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}