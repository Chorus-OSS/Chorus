package org.chorus_oss.chorus.item

class ItemNpcSpawnEgg : ItemSpawnEgg(ItemID.Companion.NPC_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 51

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}