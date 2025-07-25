package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.entity.item.EntityHopperMinecart


class MinecartHopperInventory(minecart: EntityHopperMinecart) :
    ContainerInventory(minecart, InventoryType.MINECART_HOPPER, 5)
