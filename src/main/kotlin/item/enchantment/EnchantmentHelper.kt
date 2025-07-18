package org.chorus_oss.chorus.item.enchantment

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.experimental.network.protocol.utils.ENCHANT_RECIPE_ID
import org.chorus_oss.chorus.experimental.network.protocol.utils.ENCHANT_RECIPE_MAP
import org.chorus_oss.chorus.experimental.network.protocol.utils.EnchantmentOptionData
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.utils.ChorusRandom
import org.chorus_oss.protocol.types.EnchantmentInstance
import org.chorus_oss.protocol.types.EnchantmentOption
import org.chorus_oss.protocol.types.item.ItemEnchantments
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object EnchantmentHelper {
    private const val MAX_BOOKSHELF_COUNT = 15

    fun getEnchantOptions(tablePos: Locator, input: Item?, seed: Int): List<EnchantmentOptionData> {
        if (input == null || input.hasEnchantments()) {
            return emptyList()
        }

        val random = ChorusRandom(seed.toLong())

        val bookshelfCount = countBookshelves(tablePos)
        val baseRequiredLevel = random.nextRange(1, 8) + (bookshelfCount shr 1) + random.nextRange(0, bookshelfCount)

        return listOf(
            createEnchantOption(random, input, floor(max((baseRequiredLevel / 3.0), 1.0)).toInt(), 0),
            createEnchantOption(
                random,
                input,
                max(1.0, floor((baseRequiredLevel * 2.0) / 3.0 + 1).toInt().toDouble()).toInt(),
                1
            ),
            createEnchantOption(
                random,
                input,
                max(1.0, max(baseRequiredLevel.toDouble(), (bookshelfCount * 2).toDouble())).toInt(),
                2
            )
        )
    }

    private fun countBookshelves(tablePos: Locator): Int {
        var bookshelfCount = 0
        val world = tablePos.level

        for (x in -2..2) {
            outer@ for (z in -2..2) {
                // We only check blocks at a distance of 2 blocks from the enchanting table
                if (abs(x.toDouble()) != 2.0 && abs(z.toDouble()) != 2.0) {
                    continue
                }

                // Ensure the space between the bookshelf stack at this X/Z and the enchanting table is empty
                for (y in 0..1) {
                    // Calculate the coordinates of the space between the bookshelf and the enchanting table
                    if (world.getBlock(
                            tablePos.position.add(
                                max(min(x.toDouble(), 1.0), -1.0),
                                y.toDouble(),
                                max(min(z.toDouble(), 1.0), -1.0)
                            )
                        ).id !== BlockID.AIR
                    ) {
                        continue@outer
                    }
                }

                // Finally, check the number of bookshelves at the current position
                for (y in 0..1) {
                    if (world.getBlock(
                            tablePos.position.add(
                                x.toDouble(),
                                y.toDouble(),
                                z.toDouble()
                            )
                        ).id === BlockID.BOOKSHELF
                    ) {
                        bookshelfCount++
                        if (bookshelfCount == MAX_BOOKSHELF_COUNT) {
                            return bookshelfCount
                        }
                    }
                }
            }
        }

        return bookshelfCount
    }

    @OptIn(ExperimentalAtomicApi::class)
    private fun createEnchantOption(
        random: ChorusRandom,
        inputItem: Item,
        requiredXpLevel: Int,
        entry: Int
    ): EnchantmentOptionData {
        var enchantingPower = requiredXpLevel
        val enchantability = inputItem.enchantAbility
        enchantingPower += random.nextInt(enchantability shr 2) + random.nextInt(enchantability shr 2) + 1

        // Random bonus for enchanting power between 0.85 and 1.15
        val bonus = 1 + (random.nextFloat() + random.nextFloat() - 1) * 0.15
        enchantingPower = Math.round(enchantingPower * bonus).toInt()
        if (enchantingPower < 1) enchantingPower = 1

        val resultEnchantments: MutableList<Enchantment> = ArrayList()
        var availableEnchantments = getAvailableEnchantments(enchantingPower, inputItem)
        if (!availableEnchantments.isEmpty()) {
            val lastEnchantment: AtomicReference<Enchantment> =
                AtomicReference(getRandomWeightedEnchantment(random, availableEnchantments))
            if (lastEnchantment.get() != null) {
                resultEnchantments.add(lastEnchantment.get())
            }

            while (random.nextInt(1, 50) <= enchantingPower) {
                if (!resultEnchantments.isEmpty()) {
                    availableEnchantments = availableEnchantments.stream()
                        .filter { e: Enchantment ->
                            e.id != lastEnchantment.get().id && e.isCompatibleWith(
                                lastEnchantment.get()
                            )
                        }
                        .collect(Collectors.toList())
                }
                if (availableEnchantments.isEmpty()) {
                    break
                }
                val enchantment = getRandomWeightedEnchantment(random, availableEnchantments)
                if (enchantment != null) {
                    resultEnchantments.add(enchantment)
                    lastEnchantment.set(enchantment)
                }
                enchantingPower /= 2
            }
        }
        if (inputItem.id == ItemID.Companion.BOOK) {
            if (resultEnchantments.size > 1) {
                resultEnchantments.removeAt(random.nextInt(resultEnchantments.size - 1))
            }
        }
        return EnchantmentOptionData(
            index = entry,
            enchantments = resultEnchantments,
            data = EnchantmentOption(
                cost = requiredXpLevel.toUInt(),
                enchantments = ItemEnchantments(
                    slot = 0,
                    enchantmentSlice1 = resultEnchantments.map {
                        EnchantmentInstance(
                            type = it.id.toByte(),
                            level = it.level.toByte()
                        )
                    },
                    enchantmentSlice2 = emptyList(),
                    enchantmentSlice3 = emptyList(),
                ),
                name = getRandomOptionName(random),
                recipeNetworkID = ENCHANT_RECIPE_ID.fetchAndIncrement().toUInt()
            )
        ).also { ENCHANT_RECIPE_MAP[it.data.recipeNetworkID] = it }
    }

    private fun getAvailableEnchantments(enchantingPower: Int, item: Item): List<Enchantment> {
        val list: MutableList<Enchantment> = ArrayList()
        for (enchantment in getPrimaryEnchantmentsForItem(item)) {
            if (!enchantment.isObtainableFromEnchantingTable) {
                continue
            }

            for (lvl in enchantment.maxLevel downTo 1) {
                if (enchantingPower >= enchantment.getMinEnchantAbility(lvl) && enchantingPower <= enchantment.getMaxEnchantAbility(
                        lvl
                    )
                ) {
                    list.add(enchantment.clone().setLevel(lvl))
                    break
                }
            }
        }
        return list
    }

    private fun getRandomWeightedEnchantment(random: ChorusRandom, enchantments: List<Enchantment>): Enchantment? {
        if (enchantments.isEmpty()) {
            return null
        }

        var totalWeight = 0
        for (enchantment in enchantments) {
            totalWeight += enchantment.rarity.weight
        }

        var result: Enchantment? = null
        var randomWeight = random.nextInt(totalWeight)

        for (enchantment in enchantments) {
            randomWeight -= enchantment.rarity.weight
            if (randomWeight < 0) {
                result = enchantment
                break
            }
        }
        return result
    }

    private fun getRandomOptionName(random: ChorusRandom): String {
        val name = StringBuilder()
        for (i in random.nextBoundedInt(15 - 5 + 1) + 5 downTo 1) {
            name.append((random.nextBoundedInt('z'.code - 'a'.code + 1) + 'a'.code).toChar())
        }
        return name.toString()
    }

    private fun getPrimaryEnchantmentsForItem(item: Item): List<Enchantment> {
        val list: MutableList<Enchantment> = ArrayList()

        for (enchant in Enchantment.getEnchantments()) {
            if (enchant.identifier == null && enchant.canEnchant(item)) {
                list.add(enchant)
            }
        }

        return list
    }
}
