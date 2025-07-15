package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.AdventureSettings
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.blockentity.BlockEntitySpawnable
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityArmorStand
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageModifier
import org.chorus_oss.chorus.event.player.*
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.experimental.network.protocol.utils.FLAG_ALL_PRIORITY
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.InventoryTransactionPacket
import org.chorus_oss.protocol.types.inventory.transaction.InventoryAction
import org.chorus_oss.protocol.types.inventory.transaction.ReleaseItemTransactionData
import org.chorus_oss.protocol.types.inventory.transaction.UseItemOnEntityTransactionData
import org.chorus_oss.protocol.types.inventory.transaction.UseItemTransactionData
import java.util.*
import kotlin.math.abs

class InventoryTransactionProcessor : DataPacketProcessor<MigrationPacket<InventoryTransactionPacket>>() {
    private var lastUsedItem: Item? = null

    override fun handle(player: Player, pk: MigrationPacket<InventoryTransactionPacket>) {
        val packet = pk.packet

        val player = player.player
        if (player.isSpectator) {
            player.sendAllInventories()
            return
        }

        when (packet.transactionType) {
            InventoryTransactionPacket.Companion.TransactionType.UseItem -> handleUseItem(player, packet)
            InventoryTransactionPacket.Companion.TransactionType.UseItemOnEntity -> handleUseItemOnEntity(
                player,
                packet
            )

            InventoryTransactionPacket.Companion.TransactionType.ReleaseItem -> {
                val releaseItemData = packet.transactionData as ReleaseItemTransactionData
                val itemInHandID = Registries.ITEM_RUNTIMEID.getIdentifier(releaseItemData.itemInHand.item.netID)

                try {
                    val type = releaseItemData.actionType
                    when (type) {
                        ReleaseItemTransactionData.Companion.ActionType.Release -> {
                            val lastUseTick = player.getLastUseTick(itemInHandID)
                            if (lastUseTick != -1) {
                                val item = player.inventory.itemInHand

                                val ticksUsed = player.level!!.tick - lastUseTick
                                if (!item.onRelease(player, ticksUsed)) {
                                    player.inventory.sendContents(player)
                                }

                                player.removeLastUseTick(itemInHandID)
                            } else {
                                player.inventory.sendContents(player)
                            }
                        }

                        ReleaseItemTransactionData.Companion.ActionType.Consume -> {
                            log.debug(
                                "Unexpected release item action consume from {}",
                                player.getEntityName()
                            )
                        }
                    }
                } finally {
                    player.removeLastUseTick(itemInHandID)
                }
            }

            InventoryTransactionPacket.Companion.TransactionType.Normal -> {
                if (packet.actions.size == 2 && packet.actions[0].sourceType == InventoryAction.Companion.Type.WorldInteraction &&
                    packet.actions[0].sourceFlags == InventoryAction.Companion.Flag.DropItem &&
                    packet.actions[1].sourceType == InventoryAction.Companion.Type.Container
                    && packet.actions[1].sourceFlags == InventoryAction.Companion.Flag.None
                ) { //handle throw hotbar item for player
                    dropHotBarItemForPlayer(
                        packet.actions[1].inventorySlot.toInt(),
                        packet.actions[0].newItem.item.count.toInt(),
                        player
                    )
                }
            }

            else -> {}
        }
    }

    override val packetId: Int = InventoryTransactionPacket.id

    private fun handleUseItemOnEntity(player: Player, pk: InventoryTransactionPacket) {
        val player = player.player
        val useItemOnEntityData = pk.transactionData as UseItemOnEntityTransactionData
        val target = player.level!!.getEntity(useItemOnEntityData.entityRuntimeID.toLong()) ?: return
        val type = useItemOnEntityData.actionType
        if (!Item.invoke(useItemOnEntityData.itemInHand.item).equalsExact(player.inventory.itemInHand)) {
            player.inventory.sendHeldItem(player)
        }
        var item = player.inventory.itemInHand
        when (type) {
            UseItemOnEntityTransactionData.Companion.ActionType.Interact -> {
                val playerInteractEntityEvent =
                    PlayerInteractEntityEvent(player, target, item, Vector3(useItemOnEntityData.clickPos))
                if (player.isSpectator) playerInteractEntityEvent.cancelled = true
                Server.instance.pluginManager.callEvent(playerInteractEntityEvent)
                if (playerInteractEntityEvent.cancelled) {
                    return
                }
                if (target !is EntityArmorStand) {
                    player.level!!.vibrationManager.callVibrationEvent(
                        VibrationEvent(
                            target,
                            target.position.clone(),
                            VibrationType.ENTITY_INTERACT
                        )
                    )
                } else {
                    player.level!!.vibrationManager.callVibrationEvent(
                        VibrationEvent(
                            target,
                            target.position.clone(),
                            VibrationType.EQUIP
                        )
                    )
                }
                if (target.onInteract(
                        player,
                        item,
                        Vector3(useItemOnEntityData.clickPos)
                    ) && (player.isSurvival || player.isAdventure)
                ) {
                    if (item.isTool) {
                        if (item.useOn(target) && item.damage >= item.maxDurability) {
                            player.level!!.addSound(player.position, Sound.RANDOM_BREAK)
                            item = Item.AIR
                        }
                    } else {
                        if (item.count > 1) {
                            item.count--
                        } else {
                            item = Item.AIR
                        }
                    }

                    if (item.isNothing || player.inventory.itemInHand.id == item.id) {
                        player.inventory.setItemInHand(item)
                    } else {
                        logTriedToSetButHadInHand(player, item, player.inventory.itemInHand)
                    }
                } else {
                    //Otherwise nametag still gets consumed on client side
                    player.inventory.sendContents(player)
                }
            }

            UseItemOnEntityTransactionData.Companion.ActionType.Attack -> {
                if (target is Player && !player.adventureSettings[AdventureSettings.Type.ATTACK_PLAYERS]
                    || target !is Player && !player.adventureSettings[AdventureSettings.Type.ATTACK_MOBS]
                ) return
                if (target.getRuntimeID() == player.getRuntimeID()) {
                    val event = PlayerHackDetectedEvent(player, PlayerHackDetectedEvent.HackType.INVALID_PVP)
                    Server.instance.pluginManager.callEvent(event)

                    if (event.isKick) player.kick(PlayerKickEvent.Reason.INVALID_PVP, "Attempting to attack yourself")

                    log.warn(player.getEntityName() + " tried to attack oneself")
                    return
                }
                if (!player.canInteract(target.position, (if (player.isCreative) 8 else 5).toDouble())) {
                    return
                } else if (target is Player) {
                    if ((target.gamemode and 0x01) > 0) {
                        return
                    } else if (!Server.instance.settings.levelSettings.default.pvp) {
                        return
                    }
                }
                var itemDamage = item.getAttackDamage(player).toFloat()
                val enchantments = item.enchantments
                if (item.applyEnchantments()) {
                    for (enchantment in enchantments) {
                        itemDamage += enchantment.getDamageBonus(target, player).toFloat()
                    }
                }
                val damage: MutableMap<DamageModifier, Float> = EnumMap(
                    DamageModifier::class.java
                )
                damage[DamageModifier.BASE] = itemDamage
                var knockBack = 0.3f
                if (item.applyEnchantments()) {
                    val knockBackEnchantment = item.getEnchantment(Enchantment.ID_KNOCKBACK)
                    if (knockBackEnchantment != null) {
                        knockBack += knockBackEnchantment.level * 0.1f
                    }
                }
                val entityDamageByEntityEvent = EntityDamageByEntityEvent(
                    player,
                    target,
                    EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                    damage,
                    knockBack,
                    if (item.applyEnchantments()) enchantments else null
                )
                entityDamageByEntityEvent.isBreakShield = item.canBreakShield()
                if (player.isSpectator) entityDamageByEntityEvent.cancelled = true
                if ((target is Player) && !player.level!!.gameRules.getBoolean(GameRule.PVP)) {
                    entityDamageByEntityEvent.cancelled = true
                }

                //保存攻击的目标在lastAttackEntity
                if (!entityDamageByEntityEvent.cancelled) {
                    player.player.lastAttackEntity = (entityDamageByEntityEvent.entity)
                }
                if (target is EntityLiving) {
                    target.preAttack(player)
                }
                try {
                    if (!target.attack(entityDamageByEntityEvent)) {
                        if (item.isTool && player.isSurvival) {
                            player.inventory.sendContents(player)
                        }
                        return
                    }
                } finally {
                    if (target is EntityLiving) {
                        target.postAttack(player)
                    }
                }
                if (item.isTool && (player.isSurvival || player.isAdventure)) {
                    if (item.useOn(target) && item.damage >= item.maxDurability) {
                        player.level!!.addSound(player.position, Sound.RANDOM_BREAK)
                        player.inventory.setItemInHand(Item.AIR)
                    } else {
                        if (item.isNothing || player.inventory.itemInHand.id === item.id) {
                            player.inventory.setItemInHand(item)
                        } else {
                            logTriedToSetButHadInHand(player, item, player.inventory.itemInHand)
                        }
                    }
                }
            }
        }
    }

    private fun handleUseItem(player: Player, pk: InventoryTransactionPacket) {
        val player = player.player
        val useItemData = pk.transactionData as UseItemTransactionData
        val blockVector = Vector3(useItemData.blockPosition).asBlockVector3()
        val face = BlockFace.entries[abs(useItemData.blockFace % BlockFace.entries.size)]

        val type = useItemData.actionType
        when (type) {
            UseItemTransactionData.Companion.ActionType.ClickBlock -> {
                val itemInHand = Item(useItemData.itemInHand.item)

                // Remove if client bug is ever fixed
                val spamBug =
                    (player.player.lastRightClickPos != null && System.currentTimeMillis() - player.player.lastRightClickTime < 100.0 && blockVector.distanceSquared(
                        player.player.lastRightClickPos!!
                    ) < 0.00001)
                player.player.lastRightClickPos = blockVector.asVector3()
                player.player.lastRightClickTime = System.currentTimeMillis().toDouble()
                if (spamBug) {
                    return
                }
                if (!itemInHand.canBeActivated()) player.setDataFlag(EntityFlag.USING_ITEM, false)
                if (player.canInteract(blockVector.add(0.5, 0.5, 0.5), (if (player.isCreative) 13 else 7).toDouble())) {
                    if (player.isCreative) {
                        val i = player.inventory.itemInHand
                        if (player.level!!.useItemOn(
                                blockVector.asVector3(),
                                i,
                                face,
                                useItemData.clickPosition.x,
                                useItemData.clickPosition.y,
                                useItemData.clickPosition.z,
                                player
                            ) != null
                        ) {
                            return
                        }
                    } else if (player.inventory.itemInHand.equals(itemInHand, true, false)) {
                        var i: Item? = player.inventory.itemInHand
                        val oldItem: Item = i!!.clone()
                        // TODO: Implement adventure mode checks
                        if ((player.level!!.useItemOn(
                                blockVector.asVector3(),
                                i,
                                face,
                                useItemData.clickPosition.x,
                                useItemData.clickPosition.y,
                                useItemData.clickPosition.z,
                                player
                            ).also { i = it }) != null
                        ) {
                            if (i!! != oldItem || i.getCount() != oldItem.getCount()) {
                                if (oldItem.id == i.id || i.isNothing) {
                                    player.inventory.setItemInHand(i)
                                } else {
                                    logTriedToSetButHadInHand(player, i, oldItem)
                                }
                                player.inventory.sendHeldItem(player.viewers.values)
                            }
                            return
                        }
                    }
                }
                player.inventory.sendHeldItem(player)
                if (blockVector.distanceSquared(player.position) > 10000) {
                    return
                }
                val target = player.level!!.getBlock(blockVector.asVector3())
                val block = target.getSide(face)
                player.level!!.sendBlocks(
                    arrayOf(player),
                    arrayOf<Block?>(target, block),
                    org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_NO_GRAPHICS.toInt()
                )
                player.level!!.sendBlocks(
                    arrayOf(player), arrayOf(
                        target.getLevelBlockAtLayer(1), block.getLevelBlockAtLayer(1)
                    ), org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_NO_GRAPHICS.toInt(), 1
                )
            }

            UseItemTransactionData.Companion.ActionType.BreakBlock -> {
                //Creative mode use PlayerActionPacket.ACTION_CREATIVE_PLAYER_DESTROY_BLOCK
                if (!player.spawned || !player.isAlive() || player.isCreative) {
                    return
                }
                player.resetInventory()
                var i: Item? = player.inventory.itemInHand
                val oldItem: Item = i!!.clone()
                if (player.isSurvival || player.isAdventure) {
                    if (player.canInteract(blockVector.add(0.5, 0.5, 0.5), 7.0) && (player.level!!.useBreakOn(
                            blockVector.asVector3(),
                            face,
                            i,
                            player,
                            true
                        ).also { i = it }) != null
                    ) {
                        player.foodData?.exhaust(0.005)
                        if (i!! != oldItem || i.getCount() != oldItem.getCount()) {
                            if (oldItem.id == i.id || i.isNothing) {
                                player.inventory.setItemInHand(i)
                            } else {
                                logTriedToSetButHadInHand(player, i, oldItem)
                            }
                            player.inventory.sendHeldItem(player.viewers.values)
                        }
                        return
                    }
                }
                player.inventory.sendContents(player)
                player.inventory.sendHeldItem(player)
                if (blockVector.distanceSquared(player.position) < 10000) {
                    val target = player.level!!.getBlock(blockVector.asVector3())
                    player.level!!.sendBlocks(
                        arrayOf(player), arrayOf(
                            target.position
                        ),
                        org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL_PRIORITY.toInt(), 0
                    )

                    val blockEntity = player.level!!.getBlockEntity(blockVector.asVector3())
                    if (blockEntity is BlockEntitySpawnable) {
                        blockEntity.spawnTo(player)
                    }
                }
            }

            UseItemTransactionData.Companion.ActionType.ClickAir -> {
                val item: Item
                val useItemDataItem = Item(useItemData.itemInHand.item)
                val serverItemInHand = player.inventory.itemInHand
                val directionVector = player.getDirectionVector()
                // Removes Damage Tag that the client adds, but we do not store.
                if (useItemDataItem.hasCompoundTag() && (!serverItemInHand.hasCompoundTag() || !serverItemInHand.namedTag!!.containsInt(
                        "Damage"
                    ))
                ) {
                    if (useItemDataItem.namedTag!!.containsInt("Damage")) {
                        useItemDataItem.namedTag!!.remove("Damage")
                    }
                }

                item = if (player.isCreative) {
                    serverItemInHand
                } else if (player.inventory.itemInHand != useItemDataItem) {
                    log.warn("Item received did not match item in hand.")
                    player.inventory.sendHeldItem(player)
                    return
                } else {
                    serverItemInHand
                }
                val interactEvent =
                    PlayerInteractEvent(player, item, directionVector, face, PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
                Server.instance.pluginManager.callEvent(interactEvent)
                if (interactEvent.cancelled) {
                    if (interactEvent.item != null && interactEvent.item.isArmor) {
                        player.inventory.sendArmorContents(player)
                    }
                    player.inventory.sendHeldItem(player)
                    return
                }
                if (item.onClickAir(player, directionVector)) {
                    if (!player.isCreative) {
                        if (item.isNothing || player.inventory.itemInHand.id == item.id) {
                            player.inventory.setItemInHand(item)
                        } else {
                            logTriedToSetButHadInHand(player, item, player.inventory.itemInHand)
                        }
                    }
                    if (!player.isUsingItem(item.id)) {
                        lastUsedItem = item
                        player.setLastUseTick(item.id, player.level!!.tick) //set lastUsed tick
                        return
                    }

                    val ticksUsed = player.level!!.tick - player.getLastUseTick(lastUsedItem!!.id)
                    if (lastUsedItem!!.onUse(player, ticksUsed)) {
                        lastUsedItem!!.afterUse(player)
                        player.removeLastUseTick(item.id)
                        lastUsedItem = null
                    }
                }
            }
        }
    }

    private fun logTriedToSetButHadInHand(player: Player, tried: Item, had: Item) {
        log.debug(
            "Tried to set item {} but {} had item {} in their hand slot",
            tried.id,
            player.player.getEntityName(),
            had.id
        )
    }

    companion object : Loggable {
        private fun dropHotBarItemForPlayer(hotbarSlot: Int, dropCount: Int, player: Player) {
            val inventory = player.inventory
            val item = inventory.getItem(hotbarSlot)
            if (item.isNothing) return

            val ev: PlayerDropItemEvent
            Server.instance.pluginManager.callEvent(PlayerDropItemEvent(player, item).also { ev = it })
            if (ev.cancelled) {
                player.inventory.sendContents(player)
                return
            }

            val c = item.getCount() - dropCount
            if (c <= 0) {
                inventory.clear(hotbarSlot)
            } else {
                item.setCount(c)
                inventory.setItem(hotbarSlot, item)
            }
            item.setCount(dropCount)
            player.dropItem(item)
        }
    }
}
