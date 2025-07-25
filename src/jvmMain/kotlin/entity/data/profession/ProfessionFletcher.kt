package org.chorus_oss.chorus.entity.data.profession

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.TradeRecipeBuildUtils
import org.chorus_oss.chorus.utils.Utils
import java.util.*

class ProfessionFletcher : Profession(4, BlockID.FLETCHING_TABLE, "entity.villager.fletcher", Sound.DIG_WOOD) {
    override fun buildTrades(seed: Int): ListTag<CompoundTag> {
        val recipes: ListTag<CompoundTag> = ListTag()
        val random: Random = Random(seed.toLong())

        val bow: Item = Item.get(ItemID.BOW)
        val bowEnchantments: IntArray = intArrayOf(
            Enchantment.ID_BOW_FLAME,
            Enchantment.ID_BOW_INFINITY,
            Enchantment.ID_BOW_KNOCKBACK,
            Enchantment.ID_BOW_POWER,
            Enchantment.ID_DURABILITY
        )
        val bowEnchantmemt: Enchantment =
            Enchantment.getEnchantment(bowEnchantments.get(random.nextInt(bowEnchantments.size)))
        bowEnchantmemt.setLevel(random.nextInt(bowEnchantmemt.maxLevel) + 1)
        bow.addEnchantment(bowEnchantmemt)

        val crossbow: Item = Item.get(ItemID.CROSSBOW)
        val crossbowEnchantments: IntArray = intArrayOf(
            Enchantment.ID_CROSSBOW_MULTISHOT,
            Enchantment.ID_CROSSBOW_PIERCING,
            Enchantment.ID_CROSSBOW_QUICK_CHARGE,
            Enchantment.ID_DURABILITY
        )
        val crossbowEnchantment: Enchantment =
            Enchantment.getEnchantment(crossbowEnchantments.get(random.nextInt(crossbowEnchantments.size)))
        crossbowEnchantment.setLevel(random.nextInt(crossbowEnchantment.maxLevel) + 1)
        crossbow.addEnchantment(crossbowEnchantment)

        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.STICK, 0, 32), Item.get(ItemID.EMERALD))
                .setMaxUses(16)
                .setRewardExp(1.toByte())
                .setTier(1)
                .setTraderExp(2)
                .setPriceMultiplierA(0.05f)
                .build()
        )
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 1), Item.get(ItemID.ARROW, 0, 16))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 1), Item.get(BlockID.GRAVEL, 0, 10), Item.get(
                        ItemID.FLINT, 0, 10
                    )
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        }
        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.FLINT, 0, 26), Item.get(ItemID.EMERALD))
                .setMaxUses(12)
                .setRewardExp(1.toByte())
                .setTier(2)
                .setTraderExp(10)
                .setPriceMultiplierA(0.05f)
                .build()
        )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 2), Item.get(ItemID.BOW))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.STRING, 0, 14), Item.get(ItemID.EMERALD))
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(20)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 3), Item.get(ItemID.CROSSBOW))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.FEATHER, 0, 24), Item.get(ItemID.EMERALD))
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 7 + random.nextInt(15)), bow)
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(BlockID.TRIPWIRE_HOOK, 0, 8), Item.get(ItemID.EMERALD))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(5)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 8 + random.nextInt(15)), crossbow)
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(5)
                    .setTraderExp(15)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 2),
                    Item.get(ItemID.ARROW, 0, 5),
                    Item.get(ItemID.ARROW, Utils.rand(6, 43), 5)
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(5)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        }
        return recipes
    }
}
