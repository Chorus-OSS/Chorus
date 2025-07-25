package org.chorus_oss.chorus.item

class ItemPandaSpawnEgg : ItemSpawnEgg(ItemID.Companion.PANDA_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 113

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}