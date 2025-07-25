package org.chorus_oss.chorus.entity.data.profession

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.TradeRecipeBuildUtils
import java.util.*

class ProfessionCartographer :
    Profession(6, BlockID.CARTOGRAPHY_TABLE, "entity.villager.cartographer", Sound.BLOCK_CARTOGRAPHY_TABLE_USE) {
    override fun buildTrades(seed: Int): ListTag<CompoundTag> {
        val recipes: ListTag<CompoundTag> = ListTag()
        val random: Random = Random(seed.toLong())

        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.PAPER, 0, 24), Item.get(ItemID.EMERALD))
                .setMaxUses(16)
                .setRewardExp(1.toByte())
                .setTier(1)
                .setTraderExp(2)
                .setPriceMultiplierA(0.05f)
                .build()
        )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 7), Item.get(ItemID.EMPTY_MAP))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(BlockID.GLASS_PANE, 0, 11), Item.get(ItemID.EMERALD))
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 13),
                    Item.get(ItemID.COMPASS),
                    Item.get(ItemID.FILLED_MAP, 3)
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.2f)
                    .setPriceMultiplierB(0.2f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.COMPASS, 0, 1), Item.get(ItemID.EMERALD))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(20)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 14),
                    Item.get(ItemID.COMPASS),
                    Item.get(ItemID.FILLED_MAP, 4)
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.2f)
                    .setPriceMultiplierB(0.2f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 12),
                    Item.get(ItemID.COMPASS),
                    Item.get(ItemID.FILLED_MAP, 14)
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.2f)
                    .setPriceMultiplierB(0.2f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 7), Item.get(BlockID.FRAME))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 3), Item.get(ItemID.BANNER, random.nextInt(16), 1))
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 8), Item.get(ItemID.BANNER_PATTERN, 7))
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(5)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        return recipes
    }
}
