package org.chorus_oss.chorus.item

class ItemWanderingTraderSpawnEgg : ItemSpawnEgg(ItemID.Companion.WANDERING_TRADER_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 118

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}