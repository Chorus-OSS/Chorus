package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.entity.effect.PotionType
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class ItemSplashPotion @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ProjectileItem(ItemID.Companion.SPLASH_POTION, meta, count, "Splash Potion") {
    init {
        updateName()
    }

    override var damage: Int
        get() = super.damage
        set(meta) {
            super.damage = (meta)
            updateName()
        }

    private fun updateName() {
        val potion = PotionType.get(damage)
        name = if (PotionType.WATER.equals(potion)) {
            "Splash Water Bottle"
        } else {
            ItemPotion.Companion.buildName(potion, "Splash Potion", true)
        }
    }

    override val maxStackSize: Int
        get() = 1

    override fun canBeActivated(): Boolean {
        return true
    }

    override val projectileEntityType: String
        get() = ItemID.Companion.SPLASH_POTION

    override val throwForce: Float
        get() = 0.5f

    override fun correctNBT(nbt: CompoundTag) {
        nbt.putInt("PotionId", this.meta)
    }
}