package org.chorus_oss.chorus.item

class ItemLlamaSpawnEgg : ItemSpawnEgg(ItemID.LLAMA_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 29

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}