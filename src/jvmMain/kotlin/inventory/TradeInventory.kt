package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.mob.villagers.EntityVillagerV2
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType

class TradeInventory(holder: EntityVillagerV2) : BaseInventory(holder, InventoryType.TRADE, 3) {
    var displayName: String? = null

    init {
        this.holder = holder
    }

    override fun init() {
        val networkedSlotMap = networkSlotMap()
        networkedSlotMap[0] = 4
        networkedSlotMap[1] = 5
        val slotTypeMap = slotTypeMap()
        slotTypeMap[0] = ContainerSlotType.TRADE2_INGREDIENT_1
        slotTypeMap[1] = ContainerSlotType.TRADE2_INGREDIENT_2
    }

    override fun onOpen(who: Player) {
        super.onOpen(who)
        val villager = this.holder as EntityVillagerV2
        villager.setTradingPlayer(who.getRuntimeID())
        villager.updateTrades(who)
    }

    override fun onClose(who: Player) {
        (this.holder as EntityVillagerV2).setTradingPlayer(0L)
        super.onClose(who)
    }
}
