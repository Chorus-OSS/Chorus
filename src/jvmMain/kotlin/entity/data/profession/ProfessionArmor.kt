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

class ProfessionArmor :
    Profession(8, BlockID.BLAST_FURNACE, "entity.villager.armor", Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE) {
    override fun buildTrades(seed: Int): ListTag<CompoundTag> {
        val recipes: ListTag<CompoundTag> = ListTag()
        val random: Random = Random(seed.toLong())

        val enchantments: IntArray = intArrayOf(
            Enchantment.ID_DURABILITY,
            Enchantment.ID_THORNS,
            Enchantment.ID_PROTECTION_ALL,
            Enchantment.ID_PROTECTION_EXPLOSION,
            Enchantment.ID_PROTECTION_PROJECTILE,
            Enchantment.ID_PROTECTION_FIRE,
            Enchantment.ID_VANISHING_CURSE
        )
        val diamondLeggings: Item = Item.get(ItemID.DIAMOND_LEGGINGS)
        val diamondLeggingsEnchantment: Enchantment =
            Enchantment.getEnchantment(enchantments.get(random.nextInt(enchantments.size)))
        diamondLeggingsEnchantment.setLevel(1 + random.nextInt(diamondLeggingsEnchantment.maxLevel))
        diamondLeggings.addEnchantment(diamondLeggingsEnchantment)

        val diamondChestplate: Item = Item.get(ItemID.DIAMOND_CHESTPLATE)
        val diamondChestplateEnchantment: Enchantment =
            Enchantment.getEnchantment(enchantments.get(random.nextInt(enchantments.size)))
        diamondChestplateEnchantment.setLevel(1 + random.nextInt(diamondChestplateEnchantment.maxLevel))
        diamondChestplate.addEnchantment(diamondChestplateEnchantment)

        val diamondHelmet: Item = Item.get(ItemID.DIAMOND_HELMET)
        val diamondHelmetEnchantment: Enchantment =
            Enchantment.getEnchantment(enchantments.get(random.nextInt(enchantments.size)))
        diamondHelmetEnchantment.setLevel(1 + random.nextInt(diamondHelmetEnchantment.maxLevel))
        diamondHelmet.addEnchantment(diamondHelmetEnchantment)

        val diamondBoots: Item = Item.get(ItemID.DIAMOND_BOOTS)
        val diamondBootsEnchantment: Enchantment =
            Enchantment.getEnchantment(enchantments.get(random.nextInt(enchantments.size)))
        diamondBootsEnchantment.setLevel(1 + random.nextInt(diamondBootsEnchantment.maxLevel))
        diamondBoots.addEnchantment(diamondBootsEnchantment)

        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.COAL, 0, 15), Item.get(ItemID.EMERALD, 0, 1))
                .setMaxUses(16)
                .setRewardExp(1.toByte())
                .setTier(1)
                .setTraderExp(2)
                .setPriceMultiplierA(0.05f)
                .build()
        )
        when (random.nextInt(4)) {
            0 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 4), Item.get(ItemID.IRON_BOOTS, 0, 1))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            1 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 7), Item.get(ItemID.IRON_LEGGINGS, 0, 1))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            2 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 5), Item.get(ItemID.IRON_HELMET, 0, 1))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            3 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 9), Item.get(ItemID.IRON_CHESTPLATE, 0, 1))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(1)
                    .setTraderExp(1)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        }
        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.IRON_INGOT, 0, 4), Item.get(ItemID.EMERALD, 0, 1))
                .setMaxUses(12)
                .setRewardExp(1.toByte())
                .setTier(2)
                .setTraderExp(10)
                .setPriceMultiplierA(0.05f)
                .build()
        )
        when (random.nextInt(3)) {
            0 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 36), Item.get(BlockID.BELL, 0, 1))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            1 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 3), Item.get(ItemID.CHAINMAIL_LEGGINGS, 0, 1))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            2 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 1), Item.get(ItemID.CHAINMAIL_BOOTS, 0, 1))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        }
        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.LAVA_BUCKET), Item.get(ItemID.EMERALD, 0, 1))
                .setMaxUses(12)
                .setRewardExp(1.toByte())
                .setTier(3)
                .setTraderExp(20)
                .setPriceMultiplierA(0.05f)
                .build()
        )
            .add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.DIAMOND, 0, 1), Item.get(ItemID.EMERALD, 0, 1))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(30)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        when (random.nextInt(3)) {
            0 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 4), Item.get(ItemID.CHAINMAIL_CHESTPLATE, 0, 1))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            1 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 1), Item.get(ItemID.CHAINMAIL_HELMET, 0, 1))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )

            2 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 5), Item.get(ItemID.SHIELD, 0, 1))
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        }
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 19 + random.nextInt(34 - 19)), diamondLeggings)
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 13 + random.nextInt(28 - 13)), diamondBoots)
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .build()
            )
        }
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 21 + random.nextInt(36 - 21)), diamondChestplate)
                    .setMaxUses(3)
                    .setRewardExp(1.toByte())
                    .setTier(5)
                    .setTraderExp(30)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 13 + random.nextInt(28 - 13)), diamondHelmet)
                    .setMaxUses(99)
                    .setRewardExp(1.toByte())
                    .setTier(5)
                    .setTraderExp(30)
                    .build()
            )
        }
        return recipes
    }
}
