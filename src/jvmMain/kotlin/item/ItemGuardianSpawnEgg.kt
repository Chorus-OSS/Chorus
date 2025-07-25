package org.chorus_oss.chorus.item

class ItemGuardianSpawnEgg : ItemSpawnEgg(ItemID.Companion.GUARDIAN_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 49

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}