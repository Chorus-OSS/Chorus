package org.chorus_oss.chorus.entity.data.profession

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.TradeRecipeBuildUtils
import java.util.*

class ProfessionButcher : Profession(11, BlockID.SMOKER, "entity.villager.butcher", Sound.BLOCK_SMOKER_SMOKE) {
    override fun buildTrades(seed: Int): ListTag<CompoundTag> {
        val recipes: ListTag<CompoundTag> = ListTag()
        val random: Random = Random(seed.toLong())
        when (random.nextInt(3)) {
            0 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.CHICKEN, 0, 14), Item.get(ItemID.EMERALD))
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(2)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )

            1 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.PORKCHOP, 0, 7), Item.get(ItemID.EMERALD))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(2)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )

            2 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.RABBIT, 0, 4), Item.get(ItemID.EMERALD))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(2)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        }
        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 1), Item.get(ItemID.RABBIT_STEW))
                .setMaxUses(12)
                .setRewardExp(1.toByte())
                .setTier(1)
                .setTraderExp(1)
                .setPriceMultiplierA(0.05f)
                .build()
        )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.COAL, 0, 15), Item.get(ItemID.EMERALD))
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 1), Item.get(ItemID.COOKED_PORKCHOP, 0, 5))
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 1), Item.get(ItemID.COOKED_CHICKEN, 0, 8))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        }
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.MUTTON, 0, 7), Item.get(ItemID.EMERALD))
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(20)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.BEEF, 0, 10), Item.get(ItemID.EMERALD))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(20)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        }

        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(BlockID.DRIED_KELP_BLOCK, 0, 10), Item.get(ItemID.EMERALD))
                .setMaxUses(12)
                .setRewardExp(1.toByte())
                .setTier(4)
                .setTraderExp(30)
                .setPriceMultiplierA(0.05f)
                .build()
        )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.SWEET_BERRIES, 0, 10), Item.get(ItemID.EMERALD))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(5)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        return recipes
    }
}
