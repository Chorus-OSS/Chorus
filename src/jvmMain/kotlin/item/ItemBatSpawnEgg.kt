package org.chorus_oss.chorus.item

class ItemBatSpawnEgg : ItemSpawnEgg(ItemID.Companion.BAT_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 19

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}