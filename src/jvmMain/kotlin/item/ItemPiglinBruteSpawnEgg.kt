package org.chorus_oss.chorus.item

class ItemPiglinBruteSpawnEgg : ItemSpawnEgg(ItemID.Companion.PIGLIN_BRUTE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 127

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}