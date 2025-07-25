package org.chorus_oss.chorus.item

class ItemPillagerSpawnEgg : ItemSpawnEgg(ItemID.Companion.PILLAGER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 114

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}