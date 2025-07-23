package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.player.EntityFreezeEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemShield
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.packets.SetActorLinkPacket
import org.chorus_oss.protocol.types.*
import org.chorus_oss.protocol.types.actor_data.ActorDataMap
import org.chorus_oss.protocol.types.item.ItemStack
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


open class EntityHuman(chunk: IChunk?, nbt: CompoundTag) : EntityHumanType(chunk, nbt) {
    lateinit var uuid: UUID
    override lateinit var skin: Skin

    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getLength(): Float {
        return 0.6f
    }

    override fun getHeight(): Float {
        return 1.8f
    }

    override fun getSwimmingHeight(): Float {
        return 0.6f
    }

    override fun getEyeHeight(): Float {
        return (boundingBox.maxY - boundingBox.minY - 0.18).toFloat()
    }

    /**
     * 偏移客户端传输玩家位置的y轴误差
     *
     * @return the base offset
     */
    override fun getBaseOffset(): Float {
        return 1.62f
    }

    override fun getUUID(): UUID {
        return uuid
    }

    override fun setUUID(uuid: UUID) {
        this.uuid = uuid
    }

    override fun initEntity() {
        initHumanEntity(this)
        super.initEntity()
    }

    override fun getOriginalName(): String {
        return "Human"
    }

    override fun getEntityName(): String {
        return this.getNameTag()
    }

    override fun saveNBT() {
        super.saveNBT()
        saveHumanEntity(this)
    }

    override fun entityBaseTick(): Boolean {
        return this.entityBaseTick(1)
    }

    override fun entityBaseTick(tickDiff: Int): Boolean {
        val hasUpdate: Boolean = super.entityBaseTick(tickDiff)
        //handle human entity freeze
        val collidedWithPowderSnow: Boolean =
            getTickCachedCollisionBlocks()!!.stream().anyMatch { block: Block -> block.id == BlockID.POWDER_SNOW }
        if (this.getFreezingTicks() < 140 && collidedWithPowderSnow) {
            if (getFreezingTicks() == 0) { //玩家疾跑进来要设置为非疾跑，统一为默认速度0.1
                this.setSprinting(false)
            }
            this.addFreezingTicks(1)
            val event = EntityFreezeEvent(this)
            Server.instance.pluginManager.callEvent(event)
            if (!event.cancelled) {
                this.setMovementSpeedF(0.05.coerceAtLeast(movementSpeed - 3.58e-4).toFloat())
            }
        } else if (this.getFreezingTicks() > 0 && !collidedWithPowderSnow) {
            this.addFreezingTicks(-1)
            this.setMovementSpeedF(
                Player.DEFAULT_SPEED.toDouble().coerceAtMost(movementSpeed + 3.58e-4).toFloat()
            ) //This magic number is to change the player's 0.05 speed within 140tick
        }
        if (this.getFreezingTicks() == 140 && level!!.tick % 40 == 0) {
            this.attack(EntityDamageEvent(this, DamageCause.FREEZING, getFrostbiteInjury().toFloat()))
        }
        return hasUpdate
    }

    override fun moveDelta() {
        val pk = org.chorus_oss.protocol.packets.MovePlayerPacket(
            entityRuntimeID = this.getRuntimeID().toULong(),
            position = Vector3f(position),
            pitch = rotation.pitch.toFloat(),
            yaw = rotation.yaw.toFloat(),
            headYaw = headYaw.toFloat(),
            mode = when (this.riding) {
                null -> org.chorus_oss.protocol.packets.MovePlayerPacket.Companion.Mode.Normal
                else -> org.chorus_oss.protocol.packets.MovePlayerPacket.Companion.Mode.Rotation
            },
            onGround = false,
            riddenEntityRuntimeID = this.riding.let {
                when (it) {
                    null -> 0u
                    else -> it.getRuntimeID().toULong()
                }
            },
            teleportCause = org.chorus_oss.protocol.packets.MovePlayerPacket.Companion.TeleportCause.Unknown,
            teleportSourceEntityType = 0,
            tick = 0u,
        )
        Server.broadcastPacket(viewers.values, pk)
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun spawnTo(player: Player) {
        if (this !== player && !hasSpawned.containsKey(player.loaderId)) {
            hasSpawned[player.loaderId] = player

            check(skin.isValid()) { this.javaClass.getSimpleName() + " must have a valid skin set" }

            if (this is Player) Server.instance.updatePlayerListData(
                this.getUUID(),
                this.getRuntimeID(), this.displayName,
                this.skin, this.loginChainData.xuid, arrayOf(player)
            )
            else Server.instance.updatePlayerListData(
                this.getUUID(),
                this.getRuntimeID(),
                this.getEntityName(),
                this.skin,
                arrayOf(player)
            )

            player.sendPacket(
                org.chorus_oss.protocol.packets.AddPlayerPacket(
                    uuid = Uuid(this.uuid),
                    playerName = this.getEntityName(),
                    actorRuntimeID = this.getRuntimeID().toULong(),
                    platformChatID = "", // TODO: platformChatID
                    position = Vector3f(this.position),
                    velocity = Vector3f(this.motion),
                    rotation = Vector2f(this.rotation),
                    headYaw = this.headYaw.toFloat(),
                    carriedItem = ItemStack(this.itemInHand),
                    playerGameType = Server.instance.gamemode,
                    actorData = ActorDataMap(this.entityDataMap),
                    abilitiesData = AbilitiesData(
                        this.getRuntimeID(),
                        PlayerPermission.Visitor,
                        CommandPermission.Any,
                        listOf(
                            AbilityLayer(
                                AbilityLayer.Companion.Type.Base,
                                PlayerAbilitySet(
                                    PlayerAbility.entries.toMutableSet()
                                ),
                                PlayerAbilitySet(
                                    mutableSetOf(
                                        PlayerAbility.Build,
                                        PlayerAbility.Mine,
                                        PlayerAbility.DoorsAndSwitches,
                                        PlayerAbility.OpenContainers,
                                        PlayerAbility.AttackPlayers,
                                        PlayerAbility.AttackMobs
                                    )
                                ),
                                0.1f,
                                0.1f,
                                0.05f,
                            )
                        ) // TODO: AbilityLayers
                    ),
                    actorLinks = List(this.passengers.size) { i ->
                        ActorLink(
                            this.getUniqueID(),
                            this.passengers[i].uniqueId,
                            if (i == 0) ActorLink.Companion.Type.Rider else ActorLink.Companion.Type.Passenger,
                            immediate = false,
                            riderInitiated = false,
                            vehicleAngularVelocity = 0f,
                        )
                    },
                    actorProperties = this.properties(),
                    buildPlatform = Platform.Unknown, // TODO: buildPlatform
                    deviceID = "" // TODO: DeviceID
                )
            )

            inventory.sendArmorContents(player)
            offhandInventory.sendContents(player)

            if (this.riding != null) {
                val packet = SetActorLinkPacket(
                    actorLink = ActorLink(
                        riddenActorUniqueID = riding!!.getUniqueID(),
                        riderActorUniqueID = this.getUniqueID(),
                        type = ActorLink.Companion.Type.Rider,
                        immediate = true,
                        riderInitiated = false,
                        vehicleAngularVelocity = 0f,
                    )
                )
                player.sendPacket(packet)
            }

            if (this !is Player) {
                Server.instance.removePlayerListData(this.getUUID(), player)
            }
        }
    }

    override fun despawnFrom(player: Player) {
        if (hasSpawned.containsKey(player.loaderId)) {
            val pk = org.chorus_oss.protocol.packets.RemoveActorPacket(
                actorUniqueID = this.getUniqueID()
            )
            player.sendPacket(pk)
            hasSpawned.remove(player.loaderId)
        }
    }

    override fun close() {
        if (!this.closed) {
            if (this !is Player || this.loggedIn) {
                for (viewer in inventory.viewers) {
                    viewer.removeWindow(this.inventory)
                }
            }

            super.close()
        }
    }

    override fun onBlock(entity: Entity?, event: EntityDamageEvent?, animate: Boolean) {
        super.onBlock(entity, event, animate)
        var shield: Item? = inventory.itemInHand
        var shieldOffhand: Item? = offhandInventory.getItem(0)
        if (shield is ItemShield) {
            shield = damageArmor(shield, entity, event!!)
            inventory.setItemInHand(shield)
        } else if (shieldOffhand is ItemShield) {
            shieldOffhand = damageArmor(shieldOffhand, entity, event!!)
            offhandInventory.setItem(0, shieldOffhand)
        }
    }
}
