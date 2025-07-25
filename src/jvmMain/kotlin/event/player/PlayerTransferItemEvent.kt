package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.item.Item
import java.util.*

class PlayerTransferItemEvent(
    player: Player,
    type: Type,
    sourceItem: Item,
    destinationItem: Item?,
    sourceSlot: Int,
    destinationSlot: Int,  // set this to -1 if not present
    sourceInventory: Inventory,
    destinationInventory: Inventory?
) : PlayerEvent(), Cancellable {
    val type: Type
    val sourceItem: Item

    private val destinationItem: Item?

    val sourceSlot: Int

    private val destinationSlot: Int // -1 if not present

    val sourceInventory: Inventory

    private val destinationInventory: Inventory?

    init {
        this.player = player
        this.type = type
        this.sourceItem = sourceItem
        this.destinationItem = destinationItem
        this.sourceSlot = sourceSlot
        this.destinationSlot = destinationSlot
        this.sourceInventory = sourceInventory
        this.destinationInventory = destinationInventory
    }


    fun getDestinationItem(): Optional<Item> {
        return Optional.ofNullable(this.destinationItem)
    }


    fun getDestinationSlot(): Optional<Int> {
        return if (this.destinationSlot == -1) Optional.empty() else Optional.of(this.destinationSlot)
    }


    fun getDestinationInventory(): Optional<Inventory> {
        return Optional.ofNullable(this.destinationInventory)
    }


    enum class Type {
        TRANSFER,
        SWAP,
        DROP
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
