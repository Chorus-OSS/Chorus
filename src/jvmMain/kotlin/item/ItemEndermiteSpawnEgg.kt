package org.chorus_oss.chorus.item

class ItemEndermiteSpawnEgg : ItemSpawnEgg(ItemID.Companion.ENDERMITE_SPAWN_EGG) {
    override val entityNetworkId: Int
        get() = 55

    override var damage: Int
        get() = super.damage
        set(meta) {
        }
}