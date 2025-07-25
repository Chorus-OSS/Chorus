package org.chorus_oss.chorus.entity.data.profession

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.TradeRecipeBuildUtils
import java.util.*

class ProfessionCleric : Profession(7, BlockID.BREWING_STAND, "entity.villager.cleric", Sound.RANDOM_POTION_BREWED) {
    override fun buildTrades(seed: Int): ListTag<CompoundTag> {
        val recipes: ListTag<CompoundTag> = ListTag()
        val random: Random = Random(seed.toLong())
        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.ROTTEN_FLESH, 0, 32), Item.get(ItemID.EMERALD, 0, 2))
                .setMaxUses(16)
                .setRewardExp(1.toByte())
                .setTier(1)
                .setTraderExp(2)
                .setPriceMultiplierA(0.05f)
                .build()
        )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 1), Item.get(ItemID.REDSTONE, 0, 2))
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(2)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.GOLD_INGOT, 0, 3), Item.get(ItemID.EMERALD, 0, 1))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 1), Item.get(ItemID.LAPIS_LAZULI))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.RABBIT_FOOT, 0, 2), Item.get(ItemID.EMERALD, 0, 2))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(20)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 4), Item.get(BlockID.GLOWSTONE, 0, 1))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .build()
            )
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.TURTLE_SCUTE, 0, 4), Item.get(ItemID.EMERALD, 0, 1))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.GLASS_BOTTLE, 0, 9), Item.get(ItemID.EMERALD, 0, 1))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        }
        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 5), Item.get(ItemID.ENDER_PEARL, 0, 1))
                .setMaxUses(12)
                .setRewardExp(1.toByte())
                .setTier(4)
                .setTraderExp(15)
                .setPriceMultiplierA(0.05f)
                .build()
        )
            .add(
                TradeRecipeBuildUtils.of(Item.get(BlockID.NETHER_WART, 0, 22), Item.get(ItemID.EMERALD, 0, 1))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(5)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 3), Item.get(ItemID.EXPERIENCE_BOTTLE, 0, 1))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(5)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        return recipes
    }
}
