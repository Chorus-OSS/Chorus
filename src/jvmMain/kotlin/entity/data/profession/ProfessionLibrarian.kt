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

class ProfessionLibrarian : Profession(5, BlockID.LECTERN, "entity.villager.librarian", Sound.ITEM_BOOK_PUT) {
    override fun buildTrades(seed: Int): ListTag<CompoundTag> {
        val recipes: ListTag<CompoundTag> = ListTag()
        val random: Random = Random(seed.toLong())

        val book1: Item = Item.get(ItemID.ENCHANTED_BOOK)
        val e_book1: Enchantment = Enchantment.getEnchantments().get(random.nextInt(Enchantment.getEnchantments().size))
        e_book1.setLevel(random.nextInt(e_book1.maxLevel) + 1)
        book1.addEnchantment(e_book1)
        val book2: Item = Item.get(ItemID.ENCHANTED_BOOK)
        val e_book2: Enchantment = Enchantment.getEnchantments().get(random.nextInt(Enchantment.getEnchantments().size))
        e_book2.setLevel(random.nextInt(e_book2.maxLevel) + 1)
        book2.addEnchantment(e_book2)
        val book3: Item = Item.get(ItemID.ENCHANTED_BOOK)
        val e_book3: Enchantment = Enchantment.getEnchantments().get(random.nextInt(Enchantment.getEnchantments().size))
        e_book3.setLevel(random.nextInt(e_book3.maxLevel) + 1)
        book3.addEnchantment(e_book3)
        val book4: Item = Item.get(ItemID.ENCHANTED_BOOK)
        val e_book4: Enchantment = Enchantment.getEnchantments().get(random.nextInt(Enchantment.getEnchantments().size))
        e_book4.setLevel(random.nextInt(e_book4.maxLevel) + 1)
        book4.addEnchantment(e_book4)

        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.PAPER, 0, 24), Item.get(ItemID.EMERALD))
                .setMaxUses(16)
                .setRewardExp(1.toByte())
                .setTier(1)
                .setTraderExp(2)
                .setPriceMultiplierA(0.05f)
                .build()
        )
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 9), Item.get(BlockID.BOOKSHELF))
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
                    Item.get(ItemID.EMERALD, 0, 5 + random.nextInt(60)),
                    Item.get(ItemID.BOOK),
                    book1
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
            TradeRecipeBuildUtils.of(Item.get(ItemID.BOOK), Item.get(ItemID.EMERALD))
                .setMaxUses(12)
                .setRewardExp(1.toByte())
                .setTier(2)
                .setTraderExp(10)
                .setPriceMultiplierA(0.05f)
                .build()
        )
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD), Item.get(BlockID.LANTERN))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(2)
                    .setTraderExp(5)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 5 + random.nextInt(60)),
                    Item.get(ItemID.BOOK),
                    book2
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
            TradeRecipeBuildUtils.of(Item.get(ItemID.INK_SAC), Item.get(ItemID.EMERALD))
                .setMaxUses(12)
                .setRewardExp(1.toByte())
                .setTier(3)
                .setTraderExp(20)
                .setPriceMultiplierA(0.05f)
                .build()
        )
        if (random.nextBoolean()) {
            recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD), Item.get(BlockID.GLASS, 0, 4))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )
        } else {
            recipes.add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 5 + random.nextInt(60)),
                    Item.get(ItemID.BOOK),
                    book3
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(3)
                    .setTraderExp(10)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        }
        recipes.add(
            TradeRecipeBuildUtils.of(
                Item.get(ItemID.WRITABLE_BOOK),
                Item.get(ItemID.WRITABLE_BOOK),
                Item.get(ItemID.EMERALD)
            )
                .setMaxUses(12)
                .setRewardExp(1.toByte())
                .setTier(4)
                .setTraderExp(30)
                .setPriceMultiplierA(0.05f)
                .build()
        )
        when (random.nextInt(3)) {
            0 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 5), Item.get(ItemID.CLOCK))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )

            1 -> recipes.add(
                TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 4), Item.get(ItemID.COMPASS))
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .setPriceMultiplierA(0.05f)
                    .build()
            )

            2 -> recipes.add(
                TradeRecipeBuildUtils.of(
                    Item.get(ItemID.EMERALD, 0, 5 + random.nextInt(60)),
                    Item.get(ItemID.BOOK),
                    book4
                )
                    .setMaxUses(12)
                    .setRewardExp(1.toByte())
                    .setTier(4)
                    .setTraderExp(15)
                    .setPriceMultiplierA(0.2f)
                    .build()
            )
        }
        recipes.add(
            TradeRecipeBuildUtils.of(Item.get(ItemID.EMERALD, 0, 12), Item.get(ItemID.NAME_TAG))
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
