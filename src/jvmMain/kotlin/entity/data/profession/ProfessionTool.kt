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

class ProfessionTool : Profession(10, BlockID.SMITHING_TABLE, "entity.villager.tool", Sound.SMITHING_TABLE_USE) {
    override fun buildTrades(seed: Int): ListTag<CompoundTag> {
        val recipes: ListTag<CompoundTag> = ListTag()
        val random: Random = Random(seed.toLong())

        val ench: IntArray = intArrayOf(
            Enchantment.ID_DURABILITY,
            Enchantment.ID_EFFICIENCY,
            Enchantment.ID_FORTUNE_DIGGING,
            Enchantment.ID_SILK_TOUCH
        )

        val iaxe: Item = Item.get(ItemID.IRON_AXE)
        val iaxee: Enchantment = Enchantment.getEnchantment(ench.get(random.nextInt(ench.size)))
        iaxee.setLevel(1 + random.nextInt(iaxee.maxLevel))
        iaxe.addEnchantment(iaxee)
        val ishovel: Item = Item.get(ItemID.IRON_SHOVEL)
        val ishovele: Enchantment = Enchantment.getEnchantment(ench.get(random.nextInt(ench.size)))
        ishovele.setLevel(1 + random.nextInt(ishovele.maxLevel))
        ishovel.addEnchantment(ishovele)
        val ipickaxe: Item = Item.get(ItemID.IRON_PICKAXE)
        val ipickaxee: Enchantment = Enchantment.getEnchantment(ench.get(random.nextInt(ench.size)))
        ipickaxee.setLevel(1 + random.nextInt(ipickaxee.maxLevel))
        ipickaxe.addEnchantment(ipickaxee)

        val daxe: Item = Item.get(ItemID.DIAMOND_AXE)
        val daxee: Enchantment = Enchantment.getEnchantment(ench.get(random.nextInt(ench.size)))
        daxee.setLevel(1 + random.nextInt(daxee.maxLevel))
        daxe.addEnchantment(daxee)
        val dshovel: Item = Item.get(ItemID.DIAMOND_SHOVEL)
        val dshovele: Enchantment = Enchantment.getEnchantment(ench.get(random.nextInt(ench.size)))
        dshovele.setLevel(1 + random.nextInt(dshovele.maxLevel))
        dshovel.addEnchantment(dshovele)
        val dpickaxe: Item = Item.get(ItemID.DIAMOND_PICKAXE)
        val dpickaxee: Enchantment = Enchantment.getEnchantment(ench.get(random.nextInt(ench.size)))
        dpickaxee.setLevel(1 + random.nextInt(dpickaxee.maxLevel))
        dpickaxe.addEnchantment(dpickaxee)

        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.COAL, 0, 15), Item.get(ItemID.EMERALD))
                .setMaxUses(16)
                .setRewardExp(1.toByte())
                .setTier(1)
                .setTraderExp(2)
                .setPriceMultiplierA(0.05f)
                .build()
        )
        when (random.nextInt(4)) {
            0 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 1), Item.get(ItemID.STONE_AXE))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            1 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 1), Item.get(ItemID.STONE_SHOVEL))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            2 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 1), Item.get(ItemID.STONE_PICKAXE))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            3 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 1), Item.get(ItemID.STONE_HOE))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        }
        recipes.add(
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
        when (random.nextInt(4)) {
            0 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 6 + random.nextInt(21 - 6)), iaxe)
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            1 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 7 + random.nextInt(22 - 7)), ishovel)
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            2 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 8 + random.nextInt(23 - 8)), ipickaxe)
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            3 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 4), Item.get(ItemID.DIAMOND_HOE))
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        }
        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.DIAMOND, 0, 1), Item.get(ItemID.EMERALD))
                .setMaxUses(12)
                .setRewardExp(1.toByte())
                .setTier(4)
                .setTraderExp(30)
                .setPriceMultiplierA(0.05f)
                .build()
        )
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 17 + random.nextInt(32 - 17)), daxe)
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 10 + random.nextInt(25 - 10)), dshovel)
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        }
        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 18 + random.nextInt(33 - 18)), dpickaxe)
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
