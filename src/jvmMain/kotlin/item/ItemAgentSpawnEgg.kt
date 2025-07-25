package org.chorus_oss.chorus.item

class ItemAgentSpawnEgg : ItemSpawnEgg(ItemID.Companion.AGENT_SPAWN_EGG) {
    override var damage: Int
        get() = super.damage
        set(meta) {
        }

    override val entityNetworkId: Int
        get() = 56
}