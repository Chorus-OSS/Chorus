package org.chorus_oss.chorus.item.enchantment

import org.chorus_oss.chorus.block.BlockCarvedPumpkin
import org.chorus_oss.chorus.item.*


enum class EnchantmentType {
    ALL,
    ARMOR,
    ARMOR_HEAD,
    ARMOR_TORSO,
    ARMOR_LEGS,
    ARMOR_FEET,
    SWORD,
    DIGGER,
    FISHING_ROD,
    BREAKABLE,
    BOW,
    WEARABLE,
    TRIDENT,
    CROSSBOW,
    MACE;


    fun canEnchantItem(item: Item): Boolean {
        if (this == ALL || item is ItemBook) {
            return true
        }
        if (this == BREAKABLE && item.maxDurability >= 0) {
            return true
        }

        if (item is ItemArmor) {
            if (this == WEARABLE || this == ARMOR && item.isArmor) {
                return true
            }

            return when (this) {
                ARMOR_HEAD -> item.isHelmet
                ARMOR_TORSO -> item.isChestplate
                ARMOR_LEGS -> item.isLeggings
                ARMOR_FEET -> item.isBoots
                else -> false
            }
        }

        return when (this) {
            SWORD -> item.isSword && item !is ItemTrident
            DIGGER -> item.isPickaxe || item.isShovel || item.isAxe || item.isHoe
            BOW -> item is ItemBow
            FISHING_ROD -> item is ItemFishingRod
            WEARABLE -> item is ItemHead || item.getSafeBlockState().identifier == BlockCarvedPumpkin.properties.identifier
            TRIDENT -> item is ItemTrident
            CROSSBOW -> item is ItemCrossbow
            MACE -> item is ItemMace
            else -> false
        }
    }
}
