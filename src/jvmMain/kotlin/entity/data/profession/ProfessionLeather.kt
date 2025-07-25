package org.chorus_oss.chorus.entity.data.profession

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.*
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.DyeColor
import org.chorus_oss.chorus.utils.TradeRecipeBuildUtils
import java.util.*

class ProfessionLeather : Profession(12, BlockID.CAULDRON, "entity.villager.leather", Sound.BUCKET_FILL_WATER) {
    override fun buildTrades(seed: Int): ListTag<CompoundTag> {
        val recipes: ListTag<CompoundTag> = ListTag()
        val random: Random = Random(seed.toLong())

        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.LEATHER, 0, 6), Item.get(ItemID.EMERALD))
                .setMaxUses(16)
                .setRewardExp(1.toByte())
                .setTier(1)
                .setTraderExp(2)
                .setPriceMultiplierA(0.05f)
                .build()
        )
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 7),
                    (Item.get(ItemID.LEATHER_CHESTPLATE) as ItemLeatherChestplate).setColor(
                        DyeColor.entries.get(
                            random.nextInt(DyeColor.entries.size)
                        )
                    )
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 3),
                    (Item.get(ItemID.LEATHER_LEGGINGS) as ItemLeatherLeggings).setColor(
                        DyeColor.entries.get(
                            random.nextInt(DyeColor.entries.size)
                        )
                    )
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.2f)
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
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 5),
                    (Item.get(ItemID.LEATHER_HELMET) as ItemLeatherHelmet).setColor(
                        DyeColor.entries.get(
                            random.nextInt(DyeColor.entries.size)
                        )
                    )
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 4),
                    (Item.get(ItemID.LEATHER_BOOTS) as ItemLeatherBoots).setColor(
                        DyeColor.entries.get(
                            random.nextInt(DyeColor.entries.size)
                        )
                    )
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        }
        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.RABBIT_HIDE, 0, 9), Item.get(ItemID.EMERALD))
                .setMaxUses(12)
                .setRewardExp(1.toByte())
                .setTier(3)
                .setTraderExp(20)
                .setPriceMultiplierA(0.05f)
                .build()
        )
            .add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 7),
                    (Item.get(ItemID.LEATHER_CHESTPLATE) as ItemLeatherChestplate).setColor(
                        DyeColor.entries.get(
                            random.nextInt(DyeColor.entries.size)
                        )
                    )
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.TURTLE_HELMET, 0, 4), Item.get(ItemID.EMERALD))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 6), Item.get(ItemID.LEATHER_HORSE_ARMOR))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 6), Item.get(ItemID.SADDLE))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(5)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 5),
                    (Item.get(ItemID.LEATHER_HELMET) as ItemLeatherHelmet).setColor(
                        DyeColor.entries.get(
                            random.nextInt(DyeColor.entries.size)
                        )
                    )
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(5)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        }
        return recipes
    }
}
