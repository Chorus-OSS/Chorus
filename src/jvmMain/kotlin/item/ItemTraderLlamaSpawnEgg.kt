package org.chorus_oss.chorus.item

class ItemTraderLlamaSpawnEgg : ItemSpawnEgg(ItemID.Companion.TRADER_LLAMA_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 157

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}