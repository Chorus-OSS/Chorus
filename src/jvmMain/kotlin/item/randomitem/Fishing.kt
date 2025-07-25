package org.chorus_oss.chorus.item.randomitem

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.item.randomitem.fishing.FishingEnchantmentItemSelector

object Fishing {
    val ROOT_FISHING: Selector = RandomItem.putSelector(Selector(RandomItem.ROOT))
    val FISHES: Selector = RandomItem.putSelector(Selector(ROOT_FISHING), 0.85f)
    val TREASURES: Selector = RandomItem.putSelector(Selector(ROOT_FISHING), 0.05f)
    val JUNKS: Selector = RandomItem.putSelector(Selector(ROOT_FISHING), 0.1f)
    val FISH: Selector = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.COD, FISHES), 0.6f)
    val SALMON: Selector = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.SALMON, FISHES), 0.25f)
    val TROPICAL_FISH: Selector =
        RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.TROPICAL_FISH, FISHES), 0.02f)
    val PUFFERFISH: Selector = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.PUFFERFISH, FISHES), 0.13f)
    val TREASURE_BOW: Selector = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.BOW, TREASURES), 0.1667f)
    val TREASURE_ENCHANTED_BOOK: Selector =
        RandomItem.putSelector(FishingEnchantmentItemSelector(ItemID.Companion.ENCHANTED_BOOK, TREASURES), 0.1667f)
    val JUNK_BOWL: Selector = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.BOWL, JUNKS), 0.12f)
    val JUNK_FISHING_ROD: Selector =
        RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.FISHING_ROD, JUNKS), 0.024f)
    val JUNK_LEATHER: Selector = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.LEATHER, JUNKS), 0.12f)
    val JUNK_LEATHER_BOOTS: Selector =
        RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.LEATHER_BOOTS, JUNKS), 0.12f)
    val JUNK_ROTTEN_FLESH: Selector =
        RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.ROTTEN_FLESH, JUNKS), 0.12f)
    val JUNK_STICK: Selector = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.STICK, JUNKS), 0.06f)
    val JUNK_STRING_ITEM: Selector = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.STRING, JUNKS), 0.06f)
    val JUNK_WATTER_BOTTLE: Selector =
        RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.POTION, 0, JUNKS), 0.12f)
    val JUNK_BONE: Selector = RandomItem.putSelector(ConstantItemSelector(ItemID.Companion.BONE, JUNKS), 0.12f)
    val JUNK_TRIPWIRE_HOOK: Selector =
        RandomItem.putSelector(ConstantItemSelector(Item.get(BlockID.TRIPWIRE_HOOK), JUNKS), 0.12f)

    fun getFishingResult(rod: Item?): Item {
        var fortuneLevel = 0
        var lureLevel = 0
        if (rod != null) {
            fortuneLevel = rod.getEnchantmentLevel(Enchantment.Companion.ID_FORTUNE_FISHING)
            lureLevel = rod.getEnchantmentLevel(Enchantment.Companion.ID_LURE)
        }
        return getFishingResult(fortuneLevel, lureLevel)
    }

    fun getFishingResult(fortuneLevel: Int, lureLevel: Int): Item {
        val treasureChance = (0.05f + 0.01f * fortuneLevel - 0.01f * lureLevel).coerceIn(0f, 1f)
        val junkChance = (0.05f - 0.025f * fortuneLevel - 0.01f * lureLevel).coerceIn(0f, 1f)
        val fishChance = (1 - treasureChance - junkChance).coerceIn(0f, 1f)
        RandomItem.putSelector(FISHES, fishChance)
        RandomItem.putSelector(TREASURES, treasureChance)
        RandomItem.putSelector(JUNKS, junkChance)
        val result = RandomItem.selectFrom(ROOT_FISHING)
        return result as Item
    }
}
