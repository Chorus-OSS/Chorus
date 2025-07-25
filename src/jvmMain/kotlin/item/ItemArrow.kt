package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.entity.effect.PotionType


class ItemArrow @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.ARROW, meta, count, GENERIC_NAME) {
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
        val type = damage
        if (type <= 0) {
            name = GENERIC_NAME
            return
        }

        val potion = PotionType.get(type - 1)
        this.name = when (potion.stringId) {
            "minecraft:water" -> "Arrow of Splashing"
            "minecraft:mundane", "minecraft:long_mundane", "minecraft:thick", "minecraft:awkward" -> "Tipped Arrow"
            else -> ItemPotion.Companion.buildName(potion, GENERIC_NAME, false)
        }
    }

    val tippedArrowPotion: PotionType?
        get() {
            val damage = damage
            if (damage > 0) {
                return PotionType.get(damage - 1)
            }
            return null
        }

    companion object {
        private const val GENERIC_NAME = "Arrow"
    }
}
