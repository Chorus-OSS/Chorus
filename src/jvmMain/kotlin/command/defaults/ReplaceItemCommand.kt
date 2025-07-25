package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockEntityHolder
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.inventory.EntityInventoryHolder
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Locator

class ReplaceItemCommand(name: String) : VanillaCommand(name, "commands.replaceitem.description") {
    init {
        this.permission = "chorus.command.replaceitem"
        commandParameters.clear()
        commandParameters["block"] = arrayOf(
            CommandParameter.Companion.newEnum("block", false, arrayOf("block")),
            CommandParameter.Companion.newType("position", CommandParamType.BLOCK_POSITION),
            CommandParameter.Companion.newEnum(
                "slot.container",
                false,
                arrayOf("slot.container")
            ),
            CommandParameter.Companion.newType("slotId", CommandParamType.INT),
            CommandParameter.Companion.newEnum("itemName", CommandEnum.Companion.ENUM_ITEM),
            CommandParameter.Companion.newType("amount", true, CommandParamType.INT),
            CommandParameter.Companion.newType("data", true, CommandParamType.INT),
            CommandParameter.Companion.newType("components", true, CommandParamType.JSON),
        )
        commandParameters["block-oldItemHandling"] = arrayOf(
            CommandParameter.Companion.newEnum("block", false, arrayOf("block")),
            CommandParameter.Companion.newType("position", CommandParamType.BLOCK_POSITION),
            CommandParameter.Companion.newEnum(
                "slot.container",
                false,
                arrayOf("slot.container")
            ),
            CommandParameter.Companion.newType("slotId", CommandParamType.INT),
            CommandParameter.Companion.newEnum(
                "oldItemHandling",
                false,
                arrayOf("destroy", "keep")
            ),
            CommandParameter.Companion.newEnum("itemName", CommandEnum.Companion.ENUM_ITEM),
            CommandParameter.Companion.newType("amount", true, CommandParamType.INT),
            CommandParameter.Companion.newType("data", true, CommandParamType.INT),
            CommandParameter.Companion.newType("components", true, CommandParamType.JSON),
        )
        val slotTypes: List<String> = listOf(
            "slot.weapon.mainhand",
            "slot.weapon.offhand",
            "slot.armor.head",
            "slot.armor.chest",
            "slot.armor.legs",
            "slot.armor.feet",
            "slot.enderchest",
            "slot.hotbar",
            "slot.inventory",
            "slot.saddle",
            "slot.armor",
            "slot.equippable"
        )
        commandParameters["entity"] = arrayOf(
            CommandParameter.Companion.newEnum("entity", false, arrayOf("entity")),
            CommandParameter.Companion.newType("target", CommandParamType.TARGET),
            CommandParameter.Companion.newEnum("slotType", false, slotTypes.toTypedArray()),
            CommandParameter.Companion.newType("slotId", CommandParamType.INT),
            CommandParameter.Companion.newEnum("itemName", CommandEnum.Companion.ENUM_ITEM),
            CommandParameter.Companion.newType("amount", true, CommandParamType.INT),
            CommandParameter.Companion.newType("data", true, CommandParamType.INT),
            CommandParameter.Companion.newType("components", true, CommandParamType.JSON),
        )
        commandParameters["entity-oldItemHandling"] = arrayOf(
            CommandParameter.Companion.newEnum("entity", false, arrayOf("entity")),
            CommandParameter.Companion.newType("target", CommandParamType.TARGET),
            CommandParameter.Companion.newEnum("slotType", false, slotTypes.toTypedArray()),
            CommandParameter.Companion.newType("slotId", CommandParamType.INT),
            CommandParameter.Companion.newEnum(
                "oldItemHandling",
                false,
                arrayOf("destroy", "keep")
            ),
            CommandParameter.Companion.newEnum("itemName", CommandEnum.Companion.ENUM_ITEM),
            CommandParameter.Companion.newType("amount", true, CommandParamType.INT),
            CommandParameter.Companion.newType("data", true, CommandParamType.INT),
            CommandParameter.Companion.newType("components", true, CommandParamType.JSON),
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val list = result.value
        when (result.key) {
            "entity", "entity-oldItemHandling" -> {
                return entity(sender, result.key, list, log)
            }

            "block", "block-oldItemHandling" -> {
                val pos = list.getResult<Locator>(1)
                val block = pos!!.levelBlock
                var holder: InventoryHolder? = null
                if (block is BlockEntityHolder<*>) {
                    if (block.blockEntity is InventoryHolder) {
                        holder = block.blockEntity as InventoryHolder
                    }
                }
                if (holder == null) {
                    log.addError("commands.replaceitem.noContainer", block.position.asBlockVector3().toString())
                        .output()
                    return 0
                }
                val slotId = list.getResult<Int>(3)!!
                if (slotId < 0 || slotId >= holder.inventory.size) {
                    log.addError(
                        "commands.replaceitem.badSlotNumber",
                        "slot.container",
                        "0",
                        (holder.inventory.size - 1).toString()
                    ).output()
                    return 0
                }
                val oldItemHandling = if (result.key == "block") "destroy" else list.getResult(4)!!
                val old = holder.inventory.getItem(slotId)
                if (oldItemHandling == "keep" && !old.isNothing) {
                    log.addError("commands.replaceitem.keepFailed", "slot.container", slotId.toString()).output()
                    return 0
                }
                val notOldItemHandling = result.key == "block"
                val item = list.getResult<Item>(if (notOldItemHandling) 4 else 5)!!
                item.setCount(1)
                if (list.hasResult(if (notOldItemHandling) 5 else 6)) {
                    val amount = list.getResult<Int>(if (notOldItemHandling) 5 else 6)!!
                    item.setCount(amount)
                }
                if (list.hasResult(if (notOldItemHandling) 6 else 7)) {
                    val data = list.getResult<Int>(if (notOldItemHandling) 6 else 7)!!
                    item.damage = data
                }
                if (list.hasResult(if (notOldItemHandling) 7 else 8)) {
                    val components = list.getResult<String>(if (notOldItemHandling) 7 else 8)!!
                    item.readItemJsonComponents(Item.ItemJsonComponents.fromJson(components))
                }
                if (holder.inventory.setItem(slotId, item)) {
                    log.addSuccess(
                        "commands.replaceitem.success",
                        "slot.container",
                        old.id.toString(),
                        item.getCount().toString(),
                        item.displayName
                    ).output()
                    return 1
                } else {
                    log.addError(
                        "commands.replaceitem.failed",
                        "slot.container",
                        old.id,
                        item.getCount().toString(),
                        item.displayName
                    ).output()
                    return 0
                }
            }

            else -> {
                return 0
            }
        }
    }

    private fun entity(sender: CommandSender, key: String, list: ParamList, log: CommandLogger): Int {
        val entities = list.getResult<List<Entity>>(1)!!
        if (entities.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }
        val notOldItemHandling = key == "entity"
        val slotType = list.getResult<String>(2)
        val slotId = list.getResult<Int>(3)!!
        val oldItemHandling = if (notOldItemHandling) "destroy" else list.getResult(4)!!
        val item = list.getResult<Item>(if (notOldItemHandling) 4 else 5)
        item!!.setCount(1)
        if (list.hasResult(if (notOldItemHandling) 5 else 6)) {
            val amount = list.getResult<Int>(if (notOldItemHandling) 5 else 6)!!
            item.setCount(amount)
        }
        if (list.hasResult(if (notOldItemHandling) 6 else 7)) {
            val data = list.getResult<Int>(if (notOldItemHandling) 6 else 7)!!
            item.damage = data
        }
        if (list.hasResult(if (notOldItemHandling) 7 else 8)) {
            val components = list.getResult<String>(if (notOldItemHandling) 7 else 8)!!
            item.readItemJsonComponents(Item.ItemJsonComponents.fromJson(components))
        }
        var successCount = 0
        for (entity in entities) {
            when (slotType) {
                "slot.weapon.mainhand" -> {
                    if (entity is Player) {
                        val old = entity.inventory.itemInHand
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.inventory.setItemInHand(item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                        }
                    } else if (entity is EntityInventoryHolder) {
                        val old: Item = entity.itemInHand
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.setItemInHand(item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                        }
                    }
                }

                "slot.weapon.offhand" -> {
                    if (entity is Player) {
                        val old = entity.offhandInventory.getItem(0)
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.offhandInventory.setItem(0, item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                        }
                    } else if (entity is EntityInventoryHolder) {
                        val old: Item = entity.itemInOffhand
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.setItemInOffhand(item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id,
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id,
                                item.getCount().toString(),
                                item.displayName
                            )
                        }
                    }
                }

                "slot.armor.head" -> {
                    if (entity is Player) {
                        val old = entity.inventory.helmet
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.inventory.setHelmet(item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            continue
                        }
                    }
                    if (entity is EntityInventoryHolder) {
                        val old: Item = entity.helmet
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.setHelmet(item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                        }
                    }
                }

                "slot.armor.chest" -> {
                    if (entity is Player) {
                        val old = entity.inventory.chestplate
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.inventory.setChestplate(item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            continue
                        }
                    }
                    if (entity is EntityInventoryHolder) {
                        val old: Item = entity.chestplate
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.setChestplate(item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                        }
                    }
                }

                "slot.armor.legs" -> {
                    if (entity is Player) {
                        val old = entity.inventory.leggings
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.inventory.setLeggings(item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            continue
                        }
                    }
                    if (entity is EntityInventoryHolder) {
                        val old: Item = entity.leggings
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.setLeggings(item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                        }
                    }
                }

                "slot.armor.feet" -> {
                    if (entity is Player) {
                        val old = entity.inventory.boots
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.inventory.setBoots(item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            continue
                        }
                    }
                    if (entity is EntityInventoryHolder) {
                        val old: Item = entity.boots
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.setBoots(item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                        }
                    }
                }

                "slot.enderchest" -> {
                    if (slotId < 0 || slotId >= 27) {
                        log.addError("commands.replaceitem.badSlotNumber", slotType, "0", "26")
                        continue
                    }
                    if (entity is Player) {
                        val old = entity.enderChestInventory.getItem(slotId)
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.enderChestInventory.setItem(slotId, item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                        }
                    } else {
                        log.addError(
                            "commands.replaceitem.failed",
                            slotType,
                            "0",
                            item.getCount().toString(),
                            item.displayName
                        )
                    }
                }

                "slot.hotbar" -> {
                    if (slotId < 0 || slotId >= 9) {
                        log.addError("commands.replaceitem.badSlotNumber", slotType, "0", "8")
                        continue
                    }
                    if (entity is Player) {
                        val old = entity.inventory.getItem(slotId)
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (entity.inventory.setItem(slotId, item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                        }
                    } else {
                        log.addError(
                            "commands.replaceitem.failed",
                            slotType,
                            "0",
                            item.getCount().toString(),
                            item.displayName
                        )
                    }
                }

                "slot.inventory" -> {
                    if (entity is Player) {
                        val old = entity.inventory.getItem(slotId)
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (slotId < 0 || slotId >= entity.inventory.size) {
                            log.addError(
                                "commands.replaceitem.badSlotNumber",
                                slotType,
                                "0",
                                entity.inventory.size.toString()
                            )
                            continue
                        }
                        if (entity.inventory.setItem(slotId, item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                "0",
                                item.getCount().toString(),
                                item.displayName
                            )
                        }
                    } else if (entity is EntityInventoryHolder) {
                        val old: Item = entity.inventory.getItem(slotId)
                        if (oldItemHandling == "keep" && !old.isNothing) {
                            log.addError("commands.replaceitem.keepFailed", slotType, slotId.toString())
                            continue
                        }
                        if (slotId < 0 || slotId >= entity.inventory.size) {
                            log.addError(
                                "commands.replaceitem.badSlotNumber",
                                slotType,
                                "0",
                                entity.inventory.size.toString()
                            )
                            continue
                        }
                        if (entity.inventory.setItem(slotId, item)) {
                            log.addSuccess(
                                "commands.replaceitem.success.entity",
                                entity.getEntityName(),
                                slotType,
                                old.id.toString(),
                                item.getCount().toString(),
                                item.displayName
                            )
                            successCount++
                        } else {
                            log.addError(
                                "commands.replaceitem.failed",
                                slotType,
                                "0",
                                item.getCount().toString(),
                                item.displayName
                            )
                        }
                    }
                }
            }
        }
        log.successCount(successCount).output()
        return successCount
    }
}
