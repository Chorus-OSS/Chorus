package org.chorus_oss.chorus.entity.mob

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.EntityCheckEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator
import org.chorus_oss.chorus.entity.ai.executor.FlatRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.executor.SnowGolemShootExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestEntitySensor
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.monster.EntityMonster
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemShears
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.DestroyBlockParticle
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import org.chorus_oss.chorus.registry.Registries

class EntitySnowGolem(chunk: IChunk?, nbt: CompoundTag) : EntityGolem(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.SNOW_GOLEM
    }

    var waterTicks: Int = 0

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(
                    SnowGolemShootExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.4f, 16, true, 20, 0),
                    all(
                        EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                        IBehaviorEvaluator { entity: EntityMob? -> memoryStorage.get<Entity>(CoreMemoryTypes.ATTACK_TARGET) !is EntitySnowGolem }),
                    3,
                    1
                ),
                Behavior(
                    SnowGolemShootExecutor(CoreMemoryTypes.NEAREST_SHARED_ENTITY, 0.4f, 10, true, 20, 0),
                    all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SHARED_ENTITY),
                        IBehaviorEvaluator {
                            attackTarget(
                                memoryStorage.get(
                                    CoreMemoryTypes.ATTACK_TARGET
                                )!!
                            )
                        }
                    ),
                    2,
                    1),
                Behavior(FlatRandomRoamExecutor(0.3f, 12, 100, false, -1, true, 10), none(), 1, 1)
            ),
            setOf<ISensor>(
                NearestEntitySensor(
                    EntityMonster::class.java,
                    CoreMemoryTypes.NEAREST_SHARED_ENTITY,
                    16.0,
                    0.0
                )
            ),
            setOf<IController>(WalkController(), LookController(true, true)),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (item is ItemShears) {
            if (!sheared) {
                this.sheared = (true)
                level!!.addLevelSoundEvent(this.position, LevelSoundEventPacket.SOUND_SHEAR)
                if (player.gamemode != Player.CREATIVE) player.inventory.itemInHand
                    .damage = (item.damage + 1)
                level!!.dropItem(
                    position.add(
                        0.0,
                        getEyeHeight().toDouble(), 0.0
                    ), Item.get(BlockID.CARVED_PUMPKIN)
                )
            }
        }
        return super.onInteract(player, item)
    }

    override fun getOriginalName(): String {
        return "Snow Golem"
    }

    override fun getWidth(): Float {
        return 0.4f
    }

    override fun getHeight(): Float {
        return 1.8f
    }

    override fun initEntity() {
        this.setMaxHealth(4)
        this.sheared = (false)
        super.initEntity()
    }

    override var sheared: Boolean
        get() = getDataFlag(EntityFlag.SHEARED)
        set(value) = setDataFlag(EntityFlag.SHEARED, value)


    override fun onUpdate(currentTick: Int): Boolean {
        waterTicks++
        if (level!!.gameRules.getBoolean(GameRule.MOB_GRIEFING)) {
            if (locator.levelBlock.isAir) {
                val support: Block = locator.levelBlock.down()
                if (support.isFullBlock && !support.isAir) {
                    level!!.setBlock(locator.levelBlock.position, Block.get(BlockID.SNOW_LAYER))
                }
            }
        }
        if (this.waterTicks >= 20) {
            if ((level!!.isRaining && !this.isUnderBlock()) || locator.levelBlock is BlockLiquid || Registries.BIOME[level!!.getBiomeId(
                    position.floorX,
                    position.floorY, position.floorZ
                )]!!.data.temperature > 1.0
            ) {
                this.attack(EntityDamageEvent(this, DamageCause.WEATHER, 1f))
            }
            this.waterTicks = 0
        }
        return super.onUpdate(currentTick)
    }

    companion object {
        @JvmStatic
        fun checkAndSpawnGolem(block: Block) {
            if (block.level.gameRules.getBoolean(GameRule.DO_MOB_SPAWNING)) {
                if (block is BlockPumpkin) {
                    faces@ for (blockFace in BlockFace.entries) {
                        for (i in 1..2) {
                            if (block.getSide(blockFace, i) !is BlockSnow) {
                                continue@faces
                            }
                        }
                        for (i in 0..2) {
                            val location: Block = block.getSide(blockFace, i)
                            block.level.setBlock(location.position, Block.get(BlockID.AIR))
                            block.level.addParticle(DestroyBlockParticle(location.position.add(0.5, 0.5, 0.5), block))
                            block.level.vibrationManager.callVibrationEvent(
                                VibrationEvent(
                                    null,
                                    location.position.add(0.5, 0.5, 0.5),
                                    VibrationType.BLOCK_DESTROY
                                )
                            )
                        }
                        val pos: Block = block.getSide(blockFace, 2)
                        val nbt: CompoundTag = CompoundTag()
                            .putList(
                                "Pos", ListTag<FloatTag>()
                                    .add(FloatTag(pos.position.x + 0.5))
                                    .add(FloatTag(pos.position.y))
                                    .add(FloatTag(pos.position.z + 0.5))
                            )
                            .putList(
                                "Motion", ListTag<FloatTag>()
                                    .add(FloatTag(0f))
                                    .add(FloatTag(0f))
                                    .add(FloatTag(0f))
                            )
                            .putList(
                                "Rotation", ListTag<FloatTag>()
                                    .add(FloatTag(0f))
                                    .add(FloatTag(0f))
                            )

                        val snowgolem: Entity? = createEntity(
                            EntityID.SNOW_GOLEM,
                            block.level.getChunk(block.position.chunkX, block.position.chunkZ),
                            nbt
                        )
                        snowgolem!!.spawnToAll()
                        return
                    }
                }
            }
        }
    }
}
