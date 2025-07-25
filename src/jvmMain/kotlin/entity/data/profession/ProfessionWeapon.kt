package org.chorus_oss.chorus.entity.data.profession

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.TradeRecipeBuildUtils
import java.util.*

class ProfessionWeapon : Profession(9, BlockID.GRINDSTONE, "entity.villager.weapon", Sound.BLOCK_GRINDSTONE_USE) {
    override fun buildTrades(seed: Int): ListTag<CompoundTag> {
        val recipes: ListTag<CompoundTag> = ListTag()
        val random: Random = Random(seed.toLong())

        val enchantments: IntArray = intArrayOf(
            Enchantment.ID_DURABILITY,
            Enchantment.ID_DAMAGE_ALL,
            Enchantment.ID_VANISHING_CURSE,
            Enchantment.ID_DAMAGE_SMITE,
            Enchantment.ID_DAMAGE_ARTHROPODS,
            Enchantment.ID_LOOTING,
            Enchantment.ID_FIRE_ASPECT
        )

        val ironsword: Item = Item.get(ItemID.IRON_SWORD)
        val ironswordEnchantment: Enchantment =
            Enchantment.getEnchantment(enchantments.get(random.nextInt(enchantments.size)))
        ironswordEnchantment.setLevel(1 + random.nextInt(ironswordEnchantment.maxLevel))
        ironsword.addEnchantment(ironswordEnchantment)

        val diamondAxe: Item = Item.get(ItemID.DIAMOND_AXE)
        val diamondAxeEnchantment: Enchantment =
            Enchantment.getEnchantment(enchantments.get(random.nextInt(enchantments.size)))
        diamondAxeEnchantment.setLevel(1 + random.nextInt(diamondAxeEnchantment.maxLevel))
        diamondAxe.addEnchantment(diamondAxeEnchantment)

        val diamondsword: Item = Item.get(ItemID.DIAMOND_SWORD)
        val diamondswordEnchantment: Enchantment =
            Enchantment.getEnchantment(enchantments.get(random.nextInt(enchantments.size)))
        diamondswordEnchantment.setLevel(1 + random.nextInt(diamondswordEnchantment.maxLevel))
        diamondsword.addEnchantment(diamondswordEnchantment)

        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.COAL, 0, 15), Item.get(ItemID.EMERALD))
                .setMaxUses(16)
                .setRewardExp(1.toByte())
                .setTier(1)
                .setTraderExp(2)
                .setPriceMultiplierA(0.05f)
                .build()
        )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 3), Item.get(ItemID.IRON_AXE))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 7 + random.nextInt(22 - 7)), ironsword)
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.IRON_INGOT, 0, 4), Item.get(ItemID.EMERALD))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 36), Item.get(BlockID.BELL))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.FLINT, 0, 24), Item.get(ItemID.EMERALD))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(20)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.DIAMOND, 0, 1), Item.get(ItemID.EMERALD))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 17 + random.nextInt(32 - 17)), diamondAxe)
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 13 + random.nextInt(27 - 13)), diamondsword)
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(5)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        return recipes
    }
}
