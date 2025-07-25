package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.inventory.CraftingTableInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.protocol.types.itemstack.ContainerSlotType

object NetworkMapping {
    fun getInventory(player: Player, containerSlotType: ContainerSlotType): Inventory {
        return when (containerSlotType) {
            ContainerSlotType.HorseEquip -> {
                val riding = player.getRiding()
                if (riding is InventoryHolder) {
                    riding.inventory
                } else {
                    throw IllegalArgumentException("Can't handle horse inventory: ${containerSlotType.name.uppercase()} when an ItemStackRequest is received!")
                }
            }

            ContainerSlotType.CreatedOutput -> player.creativeOutputInventory
            ContainerSlotType.Cursor -> player.cursorInventory
            ContainerSlotType.Inventory, ContainerSlotType.Hotbar, ContainerSlotType.HotbarAndInventory -> player.inventory
            ContainerSlotType.Armor -> player.inventory.armorInventory!!
            ContainerSlotType.Offhand -> player.offhandInventory
            ContainerSlotType.BeaconPayment,
            ContainerSlotType.Trade2Ingredient1,
            ContainerSlotType.Trade2Ingredient2,
            ContainerSlotType.Trade2Result,
            ContainerSlotType.LoomDye,
            ContainerSlotType.LoomMaterial,
            ContainerSlotType.LoomInput,
            ContainerSlotType.LoomResult,
            ContainerSlotType.Barrel,
            ContainerSlotType.BrewingResult,
            ContainerSlotType.BrewingFuel,
            ContainerSlotType.BrewingInput,
            ContainerSlotType.FurnaceFuel,
            ContainerSlotType.FurnaceIngredient,
            ContainerSlotType.FurnaceResult,
            ContainerSlotType.SmokerIngredient,
            ContainerSlotType.BlastFurnaceIngredient,
            ContainerSlotType.EnchantingInput,
            ContainerSlotType.EnchantingMaterial,
            ContainerSlotType.SmithingTableTemplate,
            ContainerSlotType.SmithingTableInput,
            ContainerSlotType.SmithingTableMaterial,
            ContainerSlotType.SmithingTableResult,
            ContainerSlotType.AnvilInput,
            ContainerSlotType.AnvilMaterial,
            ContainerSlotType.AnvilResult,
            ContainerSlotType.StonecutterInput,
            ContainerSlotType.StonecutterResult,
            ContainerSlotType.GrindstoneAdditional,
            ContainerSlotType.GrindstoneInput,
            ContainerSlotType.GrindstoneResult,
            ContainerSlotType.CartographyInput,
            ContainerSlotType.CartographyAdditional,
            ContainerSlotType.CartographyResult,
            ContainerSlotType.LevelEntity,
            ContainerSlotType.ShulkerBox -> {
                if (player.enderChestOpen) {
                    player.enderChestInventory
                } else if (player.topWindow.isPresent) {
                    player.topWindow.get()
                } else {
                    throw IllegalArgumentException("Can't handle inventory: ${containerSlotType.name.uppercase()} when an ItemStackRequest is received!")
                }
            }

            ContainerSlotType.CraftingInput -> {
                if (player.topWindow.isPresent && player.topWindow.get() is CraftingTableInventory) {
                    player.topWindow.get()
                } else {
                    player.craftingGrid
                }
            }

            else -> {
                throw IllegalArgumentException("Can't handle containerSlotType: ${containerSlotType.name.uppercase()} when an ItemStackRequest is received!")
            }
        }
    }
}
