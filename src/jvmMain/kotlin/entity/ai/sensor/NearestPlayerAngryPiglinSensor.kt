package org.chorus_oss.chorus.entity.ai.sensor

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.inventory.*


//存储最近的玩家的Memory

class NearestPlayerAngryPiglinSensor : ISensor {
    override fun sense(entity: EntityMob) {
        for (player in entity.viewers.values) {
            if (player.position.distance(entity.position) < 32) {
                var trigger = false
                if (player.topWindow.isPresent) {
                    if (checkInventory(player.topWindow.get())) {
                        trigger = true
                    }
                }
                if (player.isBreakingBlock()) {
                    if (checkBlock(player.breakingBlock!!)) {
                        trigger = true
                    }
                }
                if (trigger) {
                    entity.memoryStorage[CoreMemoryTypes.ATTACK_TARGET] = player
                }
            }
        }
    }

    override val period: Int
        get() = 1

    private fun checkInventory(inventory: Inventory): Boolean {
        return inventory is ChestInventory ||
                inventory is DoubleChestInventory ||
                inventory is HumanEnderChestInventory ||
                inventory is ShulkerBoxInventory ||
                inventory is BarrelInventory ||
                inventory is MinecartChestInventory ||
                inventory is ChestBoatInventory
    }

    private fun checkBlock(block: Block): Boolean {
        return when (block.id) {
            BlockID.GOLD_BLOCK, BlockID.GOLD_ORE, BlockID.GILDED_BLACKSTONE, BlockID.NETHER_GOLD_ORE, BlockID.RAW_GOLD_BLOCK, BlockID.DEEPSLATE_GOLD_ORE -> true
            else -> false
        }
    }
}
