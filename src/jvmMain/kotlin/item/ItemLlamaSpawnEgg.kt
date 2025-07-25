package org.chorus_oss.chorus.item

class ItemLlamaSpawnEgg : ItemSpawnEgg(ItemID.Companion.LLAMA_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 29

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}