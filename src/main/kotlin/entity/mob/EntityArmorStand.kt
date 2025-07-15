package org.chorus_oss.chorus.entity.mob

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.*
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.player.PlayerChangeArmorStandEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.inventory.BaseInventory
import org.chorus_oss.chorus.inventory.EntityArmorInventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemArmor
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemShield
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.DestroyBlockParticle
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.packets.SetActorDataPacket
import org.chorus_oss.protocol.types.ActorProperties
import org.chorus_oss.protocol.types.actor_data.ActorDataMap
import kotlin.math.abs

class EntityArmorStand(chunk: IChunk?, nbt: CompoundTag) : EntityMob(chunk, nbt), EntityInteractable, EntityNameable {
    override fun getEntityIdentifier(): String {
        return EntityID.ARMOR_STAND
    }

    override fun getHeight(): Float {
        return 1.975f
    }

    override fun getWidth(): Float {
        return 0.5f
    }

    override fun getGravity(): Float {
        return 0.04f
    }

    override fun initEntity() {
        this.setHealthSafe(6f)
        this.setMaxHealth(6)
        this.setImmobile(true)

        super.initEntity()

        val Pose: CompoundTag = namedTag!!.getCompound(TAG_POSE)
        this.setPose(Pose.getInt(TAG_POSE_INDEX))
    }

    override fun isPersistent(): Boolean {
        return true
    }

    override fun setPersistent(persistent: Boolean) {
        // Armor stands are always persistent
    }

    override fun getExperienceDrops(): Int {
        return 0
    }

    override fun getDrops(): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override fun canCollide(): Boolean {
        return false
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (player.isSpectator) {
            return false
        }

        // Name tag
        if (!item.isNothing && item.id == ItemID.NAME_TAG && playerApplyNameTag(player, item, false)) {
            return true
        }

        //Pose
        if (player.isSneaking() || player.isFlySneaking) {
            if (this.getPose() >= 12) {
                this.setPose(0)
            } else {
                this.setPose(this.getPose() + 1)
            }
            return false // Returning true would consume the item
        }

        //Inventory
        val isArmor: Boolean

        val hasItemInHand: Boolean = !item.isNothing
        var slot: Int

        if (hasItemInHand && item is ItemArmor) {
            isArmor = true
            slot = getArmorSlot(item)
        } else if (hasItemInHand && (item.id == BlockID.SKULL) || item.blockId == BlockID.CARVED_PUMPKIN) {
            isArmor = true
            slot = EntityEquipment.Companion.HEAD
        } else if (hasItemInHand) {
            isArmor = false
            if (item is ItemShield) {
                slot = EntityEquipment.Companion.OFF_HAND
            } else {
                slot = EntityEquipment.Companion.MAIN_HAND
            }
        } else {
            val clickHeight: Double = clickedPos.y - position.y
            if (clickHeight >= 0.1 && clickHeight < 0.55 && !boots.isNothing) {
                isArmor = true
                slot = EntityEquipment.Companion.FEET
            } else if (clickHeight >= 0.9 && clickHeight < 1.6) {
                if (!itemInOffhand.isNothing) {
                    isArmor = false
                    slot = EntityEquipment.Companion.OFF_HAND
                } else if (!itemInHand.isNothing) {
                    isArmor = false
                    slot = EntityEquipment.Companion.MAIN_HAND
                } else if (!chestplate.isNothing) {
                    isArmor = true
                    slot = EntityEquipment.Companion.CHEST
                } else {
                    return false
                }
            } else if (clickHeight >= 0.4 && clickHeight < 1.2 && !leggings.isNothing) {
                isArmor = true
                slot = EntityEquipment.Companion.LEGS
            } else if (clickHeight >= 1.6 && !helmet.isNothing) {
                isArmor = true
                slot = EntityEquipment.Companion.HEAD
            } else if (!itemInOffhand.isNothing) {
                isArmor = false
                slot = EntityEquipment.Companion.OFF_HAND
            } else if (!itemInHand.isNothing) {
                isArmor = false
                slot = EntityEquipment.Companion.MAIN_HAND
            } else {
                return false
            }
        }

        val ev: PlayerChangeArmorStandEvent = PlayerChangeArmorStandEvent(player, this, item, slot)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) return false

        var changed: Boolean = false
        if (isArmor) {
            changed = this.tryChangeEquipment(player, ev.item, slot)
            slot = EntityEquipment.Companion.MAIN_HAND
        }

        if (!changed) {
            changed = this.tryChangeEquipment(player, ev.item, slot)
        }

        if (changed) {
            level!!.addSound(this.position, Sound.MOB_ARMOR_STAND_PLACE)
        }

        return false // Returning true would consume the item but tryChangeEquipment already manages the inventory
    }

    private fun tryChangeEquipment(player: Player, handItem: Item, slot: Int): Boolean {
        val inventory: BaseInventory = equipment
        val item: Item = inventory.getItem(slot)

        if (item.isNothing && !handItem.isNothing) {
            // Adding item to the armor stand
            val itemClone: Item = handItem.clone()
            itemClone.setCount(1)
            inventory.setItem(slot, itemClone)
            if (!player.isCreative) {
                handItem.count--
                player.inventory.setItem(player.inventory.heldItemIndex, handItem)
            }
            return true
        } else if (!item.isNothing) {
            var itemtoAddToArmorStand: Item = Item.AIR
            if (!handItem.isNothing) {
                if (handItem.equals(item, true, true)) {
                    // Attempted to replace with the same item type
                    return false
                }

                if (item.count > 1) {
                    // The armor stand have more items than 1, item swapping is not supported in this situation
                    return false
                }

                val itemToSetToPlayerInv: Item
                if (handItem.count > 1) {
                    itemtoAddToArmorStand = handItem.clone()
                    itemtoAddToArmorStand.setCount(1)

                    itemToSetToPlayerInv = handItem.clone()
                    itemToSetToPlayerInv.count--
                } else {
                    itemtoAddToArmorStand = handItem.clone()
                    itemToSetToPlayerInv = Item.AIR
                }
                player.inventory.setItem(player.inventory.heldItemIndex, itemToSetToPlayerInv)
            }

            // Removing item from the armor stand
            val notAdded: Array<Item> = player.inventory.addItem(item)
            if (notAdded.size > 0) {
                if (notAdded.get(0).count == item.count) {
                    if (!handItem.isNothing) {
                        player.inventory.setItem(player.inventory.heldItemIndex, handItem)
                    }
                    return false
                }

                val itemClone: Item = item.clone()
                itemClone.count -= notAdded.get(0).count
                inventory.setItem(slot, itemClone)
            } else {
                inventory.setItem(slot, itemtoAddToArmorStand)
            }
            return true
        }
        return false
    }

    private fun getPose(): Int {
        return entityDataMap.getType(EntityDataTypes.ARMOR_STAND_POSE_INDEX)
    }

    private fun setPose(pose: Int) {
        entityDataMap.put(EntityDataTypes.ARMOR_STAND_POSE_INDEX, pose)
        val setEntityDataPacket = SetActorDataPacket(
            actorRuntimeID = this.getRuntimeID().toULong(),
            actorDataMap = ActorDataMap(this.getEntityDataMap()),
            actorProperties = ActorProperties(emptyList(), emptyList()),
            tick = 0uL
        )
        Server.instance.onlinePlayers.values.forEach { it.sendPacket(setEntityDataPacket) }
    }

    override fun saveNBT() {
        super.saveNBT()

        val pose = CompoundTag()
        pose.putInt(TAG_POSE_INDEX, this.getPose())
        pose.putInt(TAG_LAST_SIGNAL, 0)

        namedTag.putCompound(TAG_POSE, pose)
    }

    override fun fall(fallDistance: Float) {
        super.fall(fallDistance)

        level!!.addSound(this.position, Sound.MOB_ARMOR_STAND_LAND)
    }

    override fun kill() {
        this.health = 0f
        this.scheduleUpdate()

        for (passenger: Entity? in ArrayList(this.passengers)) {
            dismountEntity(passenger!!)
        }

        val lastDamageCause: EntityDamageEvent? = this.lastDamageCause
        val byAttack: Boolean = lastDamageCause != null && lastDamageCause.cause == DamageCause.ENTITY_ATTACK

        val pos: Vector3 = locator.position

        pos.y += 0.2
        level!!.dropItem(pos, boots)

        pos.y = position.y + 0.6
        level!!.dropItem(pos, leggings)

        pos.y = position.y + 1.4
        level!!.dropItem(if (byAttack) pos else this.position, Item.get(ItemID.ARMOR_STAND))
        level!!.dropItem(pos, chestplate)
        level!!.dropItem(pos, itemInHand)
        level!!.dropItem(pos, itemInOffhand)

        pos.y = position.y + 1.8
        level!!.dropItem(pos, helmet)
        equipment.clearAll()

        level!!.addSound(this.position, Sound.MOB_ARMOR_STAND_BREAK)

        // TODO: initiator should be a entity who kill it but not itself
        level!!.vibrationManager.callVibrationEvent(
            VibrationEvent(
                run {
                    val byEntity = getLastDamageCause()
                    if (byEntity is EntityDamageByEntityEvent) byEntity.damager else this
                },
                this.vector3,
                VibrationType.ENTITY_DIE
            )
        )
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        when (source.cause) {
            DamageCause.FALL -> {
                source.cancelled = true
                level!!.addSound(this.position, Sound.MOB_ARMOR_STAND_LAND)
            }

            DamageCause.CONTACT, DamageCause.HUNGER, DamageCause.MAGIC, DamageCause.DROWNING, DamageCause.SUFFOCATION, DamageCause.PROJECTILE -> source.cancelled =
                true

            DamageCause.FIRE, DamageCause.FIRE_TICK, DamageCause.LAVA -> if (hasEffect(EffectType.FIRE_RESISTANCE)) {
                return false
            }

            else -> Unit
        }

        if (source.cause != DamageCause.ENTITY_ATTACK) {
            if (namedTag!!.getByte("InvulnerableTimer") > 0) {
                source.cancelled = true
            }
            if (super.attack(source)) {
                namedTag!!.putByte("InvulnerableTimer", 9)
                return true
            }
            return false
        }

        Server.instance.pluginManager.callEvent(source)
        if (source.cancelled) {
            return false
        }
        setLastDamageCause(source)

        if (getDataProperty<Int>(EntityDataTypes.HURT_TICKS) > 0) {
            setHealthSafe(0f)
            return true
        }

        if (source is EntityDamageByEntityEvent) {
            if (source.damager is Player) {
                val player = source.damager
                if (player.isCreative) {
                    level!!.addParticle(DestroyBlockParticle(this.position, Block.get(BlockID.OAK_PLANKS)))
                    this.close()
                    return true
                }
            }
        }

        setDataProperty(EntityDataTypes.HURT_TICKS, 9, true)
        level!!.addSound(this.position, Sound.MOB_ARMOR_STAND_HIT)

        return true
    }

    override fun getOriginalName(): String {
        return "Armor Stand"
    }

    override fun entityBaseTick(tickDiff: Int): Boolean {
        var hasUpdate: Boolean = super.entityBaseTick(tickDiff)

        var hurtTime: Int = getDataProperty<Int>(EntityDataTypes.HURT_TICKS)
        if (hurtTime > 0 && age % 2 == 0) {
            setDataProperty(EntityDataTypes.HURT_TICKS, hurtTime - 1, true)
            hasUpdate = true
        }
        hurtTime = namedTag!!.getByte("InvulnerableTimer").toInt()
        if (hurtTime > 0 && age % 2 == 0) {
            namedTag!!.putByte("InvulnerableTimer", hurtTime - 1)
        }

        return hasUpdate
    }

    override fun onUpdate(currentTick: Int): Boolean {
        val tickDiff: Int = currentTick - lastUpdate
        val hasUpdated: Boolean = super.onUpdate(currentTick)

        if (closed || tickDiff <= 0 && !justCreated) {
            return hasUpdated
        }

        lastUpdate = currentTick

        var hasUpdate: Boolean = entityBaseTick(tickDiff)

        if (isAlive()) {
            if (health < getMaxHealth()) {
                setHealthSafe(health + 0.001f)
            }
            motion.y -= getGravity().toDouble()

            val highestPosition: Double = this.highestPosition
            move(motion.x, motion.y, motion.z)

            val friction: Float = 1 - getDrag()

            motion.x *= friction.toDouble()
            motion.y *= (1 - getDrag()).toDouble()
            motion.z *= friction.toDouble()

            updateMovement()
            hasUpdate = true
            if (onGround && (highestPosition - position.y) >= 3) {
                level!!.addSound(this.position, Sound.MOB_ARMOR_STAND_LAND)
            }
        }

        return hasUpdate || !onGround || abs(motion.x) > 0.00001 || abs(
            motion.y
        ) > 0.00001 || abs(motion.z) > 0.00001
    }

    override fun getDrag(): Float {
        if (hasWaterAt(getHeight() / 2f)) {
            return 0.25f
        }
        return 0f
    }

    override fun getInteractButtonText(player: Player): String {
        return "action.interact.armorstand.equip"
    }

    override fun canDoInteraction(): Boolean {
        return true
    }

    companion object {
        private const val TAG_POSE: String = "Pose"
        private const val TAG_LAST_SIGNAL: String = "LastSignal"
        private const val TAG_POSE_INDEX: String = "PoseIndex"

        private fun getArmorSlot(armorItem: ItemArmor): Int {
            return if (armorItem.isHelmet) {
                EntityArmorInventory.SLOT_HEAD
            } else if (armorItem.isChestplate) {
                EntityArmorInventory.SLOT_CHEST
            } else if (armorItem.isLeggings) {
                EntityArmorInventory.SLOT_LEGS
            } else {
                EntityArmorInventory.SLOT_FEET
            }
        }
    }
}
