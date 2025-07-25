package org.chorus_oss.chorus.item

class ItemTurtleSpawnEgg : ItemSpawnEgg(ItemID.Companion.TURTLE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 74

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}