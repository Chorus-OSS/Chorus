package org.chorus_oss.chorus.entity.data.profession

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.DyeColor
import org.chorus_oss.chorus.utils.TradeRecipeBuildUtils
import java.util.*

class ProfessionShepherd : Profession(3, BlockID.LOOM, "entity.villager.shepherd", Sound.BLOCK_LOOM_USE) {
    override fun buildTrades(seed: Int): ListTag<CompoundTag> {
        val recipes: ListTag<CompoundTag> = ListTag()
        val random: Random = Random(seed.toLong())

        val rod: Item = Item.get(ItemID.FISHING_ROD)
        val rodEnchantment: Enchantment = Enchantment.getEnchantment(
            intArrayOf(
                Enchantment.ID_DURABILITY,
                Enchantment.ID_LURE,
                Enchantment.ID_FORTUNE_FISHING
            ).get(random.nextInt(2))
        )
        rodEnchantment.setLevel(random.nextInt(3) + 1)
        rod.addEnchantment(rodEnchantment)

        recipes.add(
            TradeRecipeBuildUtils.of(
                Item.get(
                    "minecraft:" + arrayOf(DyeColor.WHITE, DyeColor.BROWN, DyeColor.BLACK, DyeColor.GRAY).get(
                        random.nextInt(4)
                    ).colorName.lowercase(Locale.getDefault()) + "_wool", 0, 18
                ), Item.get(
                    ItemID.EMERALD
                )
            )
                .setMaxUses(16)
                .setRewardExp(1.toByte())
                .setTier(1)
                .setTraderExp(2)
                .setPriceMultiplierA(0.05f)
                .build()
        )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 2), Item.get(ItemID.SHEARS))
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(
                    Item.get(
                        "minecraft:" + DyeColor.entries.get(
                            intArrayOf(
                                DyeColor.WHITE.dyeData,
                                DyeColor.LIGHT_BLUE.dyeData,
                                DyeColor.LIME.dyeData,
                                DyeColor.BLACK.dyeData,
                                DyeColor.GRAY.dyeData
                            ).get(random.nextInt(5))
                        ).colorName.lowercase(Locale.getDefault()) + "_dye", 0, 12
                    ), Item.get(
                        ItemID.EMERALD
                    )
                )
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD),
                    Item.get(
                        "minecraft:" + DyeColor.entries.get(random.nextInt(DyeColor.entries.size)).name
                            .lowercase(Locale.getDefault()) + "_wool"
                    )
                )
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 1),
                    Item.get(
                        "minecraft:" + DyeColor.entries.get(random.nextInt(DyeColor.entries.size)).colorName.lowercase(
                            Locale.getDefault()
                        ) + "_carpet"
                    )
                )
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        }
        recipes.add(
            TradeRecipeBuildUtils.of(
                Item.get(
                    "minecraft:" + DyeColor.entries.get(
                        intArrayOf(
                            DyeColor.YELLOW.dyeData,
                            DyeColor.LIGHT_GRAY.dyeData,
                            DyeColor.ORANGE.dyeData,
                            DyeColor.RED.dyeData,
                            DyeColor.PINK.dyeData
                        ).get(random.nextInt(5))
                    ).colorName.lowercase(Locale.getDefault()) + "_dye", 0, 12
                ), Item.get(
                    ItemID.EMERALD
                )
            )
                .setMaxUses(16)
                .setRewardExp(1.toByte())
                .setTier(3)
                .setTraderExp(20)
                .setPriceMultiplierA(0.05f)
                .build()
        )
            .add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 3),
                    Item.get("minecraft:" + DyeColor.entries.get(random.nextInt(DyeColor.entries.size)).colorName + "_bed")
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(
                    Item.get(
                        "minecraft:" + arrayOf(
                            DyeColor.BROWN,
                            DyeColor.PURPLE,
                            DyeColor.BLUE,
                            DyeColor.GREEN,
                            DyeColor.MAGENTA,
                            DyeColor.CYAN
                        ).get(random.nextInt(6)).colorName.lowercase(Locale.getDefault()) + "_dye", 0, 12
                    ), Item.get(
                        ItemID.EMERALD
                    )
                )
                    .setMaxUses(16)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD),
                    Item.get(ItemID.BANNER, random.nextInt(DyeColor.entries.size))
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 2), Item.get(ItemID.PAINTING, 0, 3))
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
