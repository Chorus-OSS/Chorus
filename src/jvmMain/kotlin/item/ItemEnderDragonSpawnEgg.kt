package org.chorus_oss.chorus.item

class ItemEnderDragonSpawnEgg : ItemSpawnEgg(ItemID.ENDER_DRAGON_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 53

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}