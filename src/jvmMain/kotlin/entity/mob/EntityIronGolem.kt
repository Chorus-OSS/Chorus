package org.chorus_oss.chorus.entity.mob

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockIronBlock
import org.chorus_oss.chorus.block.BlockPumpkin
import org.chorus_oss.chorus.entity.Attribute
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityOwnable
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
import org.chorus_oss.chorus.entity.ai.executor.MeleeAttackExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestEntitySensor
import org.chorus_oss.chorus.entity.mob.monster.EntityMonster
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemIronIngot
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.DestroyBlockParticle
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import java.util.concurrent.ThreadLocalRandom

class EntityIronGolem(chunk: IChunk?, nbt: CompoundTag) : EntityGolem(chunk, nbt), EntityOwnable {
    override fun getEntityIdentifier(): String {
        return EntityID.IRON_GOLEM
    }

    private var attackingPlayer: Boolean = false

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(
                    MeleeAttackExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.2f, 40, true, 30), all(
                        EntityCheckEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                        not(
                            any(
                                IBehaviorEvaluator { Server.instance.getDifficulty() == 0 },
                                all(
                                    IBehaviorEvaluator {
                                        attackingPlayer = memoryStorage[CoreMemoryTypes.ATTACK_TARGET] is Player
                                        attackingPlayer
                                    },
                                    IBehaviorEvaluator { hasOwner(false) }
                                )))
                    ), 3, 1),
                Behavior(
                    MeleeAttackExecutor(CoreMemoryTypes.NEAREST_SHARED_ENTITY, 0.2f, 40, true, 30), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SHARED_ENTITY),
                        IBehaviorEvaluator {
                            attackTarget(
                                memoryStorage[CoreMemoryTypes.NEAREST_SHARED_ENTITY]!!
                            )
                        },
                        not { !attackingPlayer }
                    ), 2, 1
                ),
                Behavior(FlatRandomRoamExecutor(0.2f, 12, 100, false, -1, true, 10), none(), 1, 1)
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

    override fun getOriginalName(): String {
        return "Iron Golem"
    }

    override fun getWidth(): Float {
        return 1.4f
    }

    override fun getHeight(): Float {
        return 2.9f
    }

    override fun initEntity() {
        this.setMaxHealth(100)
        super.initEntity()
        this.syncAttribute(getHealthAttribute())
    }

    override fun spawnTo(player: Player) {
        super.spawnTo(player)
        this.syncAttribute(getHealthAttribute())
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (item is ItemIronIngot && health <= getMaxHealth() * 0.75f) {
            level!!.addSound(this.position, Sound.MOB_IRONGOLEM_REPAIR)
            if (player.gamemode != Player.CREATIVE) player.inventory.itemInHand.decrement(1)
            heal(25f)
        }
        return super.onInteract(player, item)
    }

    override fun getDrops(): Array<Item> {
        // Item drops
        val random: ThreadLocalRandom = ThreadLocalRandom.current()
        val flowerAmount: Int = random.nextInt(3)
        val drops: Array<Item> = if (flowerAmount > 0) {
            arrayOf(
                Item.get(ItemID.IRON_INGOT, 0, random.nextInt(3, 6)),
                Item.get(BlockID.POPPY, 0, flowerAmount)
            )
        } else {
            arrayOf(
                Item.get(ItemID.IRON_INGOT, 0, random.nextInt(3, 6))
            )
        }
        return drops
    }


    private fun getHealthAttribute(): Attribute {
        return Attribute.Companion.getAttribute(Attribute.Companion.MAX_HEALTH).setMaxValue(getMaxHealth().toFloat())
            .setValue(if (this.health < getMaxHealth()) this.health else getMaxHealth().toFloat())
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        val health: Float = health
        if (!super.attack(source)) return false
        for (i: Int in intArrayOf(74, 50, 25)) {
            if (health > i && health <= i) {
                level!!.addSound(this.position, Sound.MOB_IRONGOLEM_CRACK)
            }
        }
        return true
    }

    override fun setHealthSafe(health: Float) {
        super.setHealthSafe(health)
        syncAttribute(getHealthAttribute())
    }

    override fun getDiffHandDamage(difficulty: Int): Float {
        if (attackingPlayer) {
            when (Server.instance.getDifficulty()) {
                1 -> return ThreadLocalRandom.current().nextFloat(4.5f, 11.5f)
                2 -> return ThreadLocalRandom.current().nextFloat(7.5f, 21.5f)
                3 -> return ThreadLocalRandom.current().nextFloat(11.5f, 32.25f)
            }
        }
        return ThreadLocalRandom.current().nextFloat(7.5f, 11.75f)
    }

    companion object {
        @JvmStatic
        fun checkAndSpawnGolem(block: Block, player: Player?) {
            if (block.level.gameRules.getBoolean(GameRule.DO_MOB_SPAWNING)) {
                if (block is BlockPumpkin) {
                    faces@ for (blockFace in BlockFace.entries) {
                        for (i in 1..2) {
                            if (block.getSide(blockFace, i) !is BlockIronBlock) {
                                continue@faces
                            }
                        }
                        faces1@ for (face in setOf(
                            BlockFace.UP,
                            BlockFace.NORTH,
                            BlockFace.EAST
                        )) {
                            for (i in -1..1) {
                                if (block.getSide(blockFace).getSide(face, i) !is BlockIronBlock) {
                                    continue@faces1
                                }
                            }
                            for (i in 0..2) {
                                val location: Block = block.getSide(blockFace, i)
                                block.level.setBlock(location.position, Block.get(BlockID.AIR))
                                block.level.addParticle(
                                    DestroyBlockParticle(
                                        location.position.add(0.5, 0.5, 0.5),
                                        block
                                    )
                                )
                                block.level.vibrationManager.callVibrationEvent(
                                    VibrationEvent(
                                        null,
                                        location.position.add(0.5, 0.5, 0.5),
                                        VibrationType.BLOCK_DESTROY
                                    )
                                )
                            }
                            for (i in -1..1) {
                                val location: Block = block.getSide(blockFace).getSide(face, i)
                                block.level.setBlock(location.position, Block.get(BlockID.AIR))
                                block.level.addParticle(
                                    DestroyBlockParticle(
                                        location.position.add(0.5, 0.5, 0.5),
                                        block
                                    )
                                )
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

                            val irongolem: Entity? = createEntity(
                                EntityID.IRON_GOLEM,
                                block.level.getChunk(block.position.chunkX, block.position.chunkZ),
                                nbt
                            )
                            irongolem!!.spawnToAll()
                            if (irongolem is EntityIronGolem) {
                                if (player != null) {
                                    irongolem.setOwnerName(player.getEntityName())
                                }
                            }
                            return
                        }
                    }
                }
            }
        }
    }
}
