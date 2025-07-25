package org.chorus_oss.chorus.item

class ItemAxolotlSpawnEgg : ItemSpawnEgg(ItemID.Companion.AXOLOTL_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 130

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}