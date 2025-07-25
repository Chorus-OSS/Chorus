package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.blockentity.BlockEntityBrewingStand
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item


class StartBrewEvent(val brewingStand: BlockEntityBrewingStand) : InventoryEvent(brewingStand.inventory), Cancellable {
    val ingredient: Item = brewingStand.inventory.ingredient

    val potions: Array<Item?> = arrayOfNulls(3)

    init {
        for (i in 0..2) {
            potions[i] = brewingStand.inventory.getItem(i)
        }
    }

    /**
     * @param index Potion index in range 0 - 2
     * @return potion
     */
    fun getPotion(index: Int): Item? {
        return potions[index]
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
