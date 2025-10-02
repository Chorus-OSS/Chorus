package org.chorus_oss.chorus.item

class ItemRabbitSpawnEgg : ItemSpawnEgg(ItemID.RABBIT_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 18

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}