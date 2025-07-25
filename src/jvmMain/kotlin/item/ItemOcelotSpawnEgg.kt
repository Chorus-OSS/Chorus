package org.chorus_oss.chorus.item

class ItemOcelotSpawnEgg : ItemSpawnEgg(ItemID.Companion.OCELOT_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 22

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}