package org.chorus_oss.chorus.item

class ItemMagmaCubeSpawnEgg : ItemSpawnEgg(ItemID.Companion.MAGMA_CUBE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 42

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}