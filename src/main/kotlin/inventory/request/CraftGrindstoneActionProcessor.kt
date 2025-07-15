package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.inventory.GrindstoneEvent
import org.chorus_oss.chorus.inventory.GrindstoneInventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.types.itemstack.request.action.ItemStackRequestActionType
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.itemstack.request.action.CraftGrindstoneRequestAction
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Stream
import kotlin.math.ceil
import kotlin.math.max

class CraftGrindstoneActionProcessor : ItemStackRequestActionProcessor<CraftGrindstoneRequestAction> {
    override val type: ItemStackRequestActionType
        get() = ItemStackRequestActionType.CRAFT_REPAIR_AND_DISENCHANT

    override fun handle(
        action: CraftGrindstoneRequestAction,
        player: Player,
        context: ItemStackRequestContext
    ): ActionResponse? {
        val topWindow = player.topWindow
        if (topWindow.isEmpty) {
            log.error("the player's inventory is empty!")
            return context.error()
        }
        val inventory = topWindow.get() as GrindstoneInventory
        val firstItem = inventory.firstItem
        val secondItem = inventory.secondItem
        if ((firstItem.isNothing) && (secondItem.isNothing)) {
            return context.error()
        }
        val pair = updateGrindstoneResult(player, inventory) ?: return context.error()
        val exp = pair.second
        val event = GrindstoneEvent(
            inventory,
            firstItem, pair.first, secondItem, exp, player
        )
        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) {
            player.removeAllWindows(false)
            player.sendAllInventories()
            return context.error()
        }
        player.addExperience(event.experienceDropped)
        player.creativeOutputInventory.setItem(event.resultItem)
        return null
    }

    fun updateGrindstoneResult(player: Player?, inventory: GrindstoneInventory): Pair<Item, Int>? {
        var firstItem = inventory.firstItem
        var secondItem = inventory.secondItem
        var resultPair: Pair<Item, Int> = Pair(Item.AIR, 0)
        if (!firstItem.isNothing && !secondItem.isNothing && (firstItem.id != secondItem.id)) {
            return null
        }

        if (firstItem.isNothing) {
            val air = firstItem
            firstItem = secondItem
            secondItem = air
        }

        if (firstItem.isNothing) {
            return null
        }

        if (firstItem.id == ItemID.ENCHANTED_BOOK) {
            if (secondItem.isNothing) {
                resultPair =
                    Pair(Item.get(ItemID.BOOK, 0, firstItem.getCount()), recalculateResultExperience(inventory))
            } else {
                return null
            }
            return resultPair
        }
        val resultExperience = recalculateResultExperience(inventory)
        val result = firstItem.clone()
        var tag = result.namedTag
        if (tag == null) tag = CompoundTag()
        tag.remove("ench")
        tag.remove("custom_ench")
        result.setCompoundTag(tag)

        if (!secondItem.isNothing && firstItem.maxDurability > 0) {
            val first = firstItem.maxDurability - firstItem.damage
            val second = secondItem.maxDurability - secondItem.damage
            val reduction = first + second + firstItem.maxDurability * 5 / 100
            val resultingDamage = max((firstItem.maxDurability - reduction + 1).toDouble(), 0.0).toInt()
            result.damage = resultingDamage
        }
        resultPair = Pair(result, resultExperience)
        return resultPair
    }

    fun recalculateResultExperience(inventory: GrindstoneInventory): Int {
        val firstItem = inventory.firstItem
        val secondItem = inventory.secondItem
        if (!firstItem.hasEnchantments() && !secondItem.hasEnchantments()) {
            return 0
        }

        var resultExperience = Stream.of(firstItem, secondItem)
            .flatMap { item: Item ->
                // Support stacks of enchanted items and skips invalid stacks (e.g. negative stacks, enchanted air)
                if (item.isNothing) {
                    return@flatMap Stream.empty<Enchantment>()
                } else if (item.getCount() == 1) {
                    return@flatMap item.enchantments.toList().stream()
                } else {
                    val enchantments: Array<Array<Enchantment>> = Array(item.getCount()) {
                        item.enchantments
                    }
                    return@flatMap enchantments.toList().stream().flatMap { it.toList().stream() }
                }
            }
            .mapToInt { enchantment: Enchantment -> enchantment.getMinEnchantAbility(enchantment.level) }
            .sum()

        resultExperience = ThreadLocalRandom.current().nextInt(
            ceil(resultExperience.toDouble() / 2).toInt(),
            resultExperience + 1
        )
        return resultExperience
    }

    companion object : Loggable
}
