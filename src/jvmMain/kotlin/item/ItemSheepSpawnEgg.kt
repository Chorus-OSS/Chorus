package org.chorus_oss.chorus.item

class ItemSheepSpawnEgg : ItemSpawnEgg(ItemID.SHEEP_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 13

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}