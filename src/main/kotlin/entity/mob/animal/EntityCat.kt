package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.*
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.FluctuateController
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.*
import org.chorus_oss.chorus.entity.ai.executor.*
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestFeedingPlayerSensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestTargetEntitySensor
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemDye
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.ItemBreakParticle
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.EntityEventPacket
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import org.chorus_oss.chorus.utils.DyeColor
import org.chorus_oss.chorus.utils.Utils
import java.util.function.Function
import kotlin.math.max

class EntityCat(chunk: IChunk?, nbt: CompoundTag) : EntityAnimal(chunk, nbt), EntityWalkable, EntityOwnable,
    EntityCanSit, EntityCanAttack, EntityHealable, EntityVariant, EntityColor {
    override fun getEntityIdentifier(): String {
        return EntityID.CAT
    }

    override var variant: Int
        get() = super<EntityVariant>.variant
        set(value) {
            super<EntityVariant>.variant = value
        }

    override var diffHandDamage: FloatArray = floatArrayOf(4f, 4f, 4f)

    override fun updateMovement() {
        //猫猫流线运动怎么可能会摔落造成伤害呢~
        this.highestPosition = position.y
        super.updateMovement()
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(
                Behavior(
                    { entity: EntityMob? ->
                        //刷新随机播放音效
                        //当猫被驯服时发出的声音
                        if (this.hasOwner(false)) this.setAmbientSoundEvent(Sound.MOB_CAT_MEOW)
                        else this.setAmbientSoundEvent(Sound.MOB_CAT_STRAYMEOW)
                        false
                    }, { entity: EntityMob? -> true }, 1, 1, 20
                ),  //用于刷新InLove状态的核心行为
                Behavior(
                    InLoveExecutor(400),
                    all(
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_BE_FEED_TIME, 0, 400),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_IN_LOVE_TIME, 6000, Int.MAX_VALUE),
                        IBehaviorEvaluator { entity: EntityMob? -> this.hasOwner() }
                    ),
                    1, 1
                ),  //"流浪猫会寻找并攻击15格内的鸡[仅Java版]、兔子和幼年海龟" --- 来自Wiki https://minecraft.wiki/w/Cat#Bedrock_Edition
                Behavior(
                    IBehaviorExecutor { entity: EntityMob? ->
                        if (this.hasOwner(false)) return@IBehaviorExecutor false
                        val storage = memoryStorage
                        if (storage.notEmpty(CoreMemoryTypes.ATTACK_TARGET)) return@IBehaviorExecutor false
                        var attackTarget: Entity? = null
                        //已驯服为家猫就不攻击下述动物反之未驯服为流浪猫攻击下述动物
                        //攻击最近的小海龟，兔子
                        if (storage.notEmpty(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET) && storage[CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET]!!.isAlive()
                        ) {
                            attackTarget = storage.get<Entity>(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET)
                        }
                        storage.set(CoreMemoryTypes.ATTACK_TARGET, attackTarget)
                        false
                    },
                    { entity: EntityMob? -> true }, 20
                )
            ),
            setOf<IBehavior>( //坐下锁定 优先级8
                Behavior(
                    { entity: EntityMob? -> false },
                    { entity: EntityMob? -> this.isSitting() }, 8
                ),  //睡觉 优先级7
                Behavior(SleepOnOwnerBedExecutor(), IBehaviorEvaluator { entity: EntityMob? ->
                    val player = this.owner ?: return@IBehaviorEvaluator false
                    if (player.level!!.id != level!!.id) return@IBehaviorEvaluator false
                    player.isSleeping()
                }, 7),  //攻击仇恨目标 优先级6
                Behavior(
                    MeleeAttackExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.35f, 15, true, 10),
                    MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                    6
                ),  //猫咪繁殖 优先级5
                Behavior(
                    EntityBreedingExecutor(EntityCat::class.java, 8, 100, 0.35f),
                    { entity: EntityMob -> entity.memoryStorage.get<Boolean>(CoreMemoryTypes.IS_IN_LOVE) },
                    5
                ),  //猫咪向主人移动 优先级4
                Behavior(EntityMoveToOwnerExecutor(0.35f, true, 15), IBehaviorEvaluator { entity: EntityMob ->
                    if (this.hasOwner()) {
                        val player = owner
                        if (!player!!.isOnGround()) return@IBehaviorEvaluator false
                        val distanceSquared = entity.position.distanceSquared(player.position)
                        return@IBehaviorEvaluator distanceSquared >= 100
                    } else return@IBehaviorEvaluator false
                }, 4),  //猫在主人身边随机移动 优先级3
                Behavior(
                    FlatRandomRoamExecutor(0.1f, 4, 100, false, -1, true, 20),
                    { entity: EntityMob? -> this.hasOwner() && this.owner!!.position.distanceSquared(this.position) < 100 },
                    3
                ),  //猫咪看向食物 优先级3
                Behavior(
                    LookAtFeedingPlayerExecutor(),
                    MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_FEEDING_PLAYER),
                    3
                ),  //猫咪随机目标点移动 优先级1
                Behavior(
                    FlatRandomRoamExecutor(0.2f, 12, 150, false, -1, true, 20),
                    ProbabilityEvaluator(5, 10),
                    1,
                    1,
                    25
                ),  //猫咪看向目标玩家 优先级1
                Behavior(
                    LookAtTargetExecutor(CoreMemoryTypes.NEAREST_PLAYER, 100),
                    ConditionalProbabilityEvaluator(
                        3, 7,
                        { entity: Entity? -> hasOwner(false) }, 10
                    ),
                    1,
                    1,
                    25
                )
            ),
            setOf<ISensor>(
                NearestFeedingPlayerSensor(7.0, 0.0),
                NearestPlayerSensor(8.0, 0.0, 20),
                NearestTargetEntitySensor<Entity>(
                    0.0, 15.0, 20,
                    listOf(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET),
                    Function<Entity, Boolean> { entity: Entity? ->
                        this.attackTarget(
                            entity!!
                        )
                    })
            ),
            setOf<IController>(WalkController(), LookController(true, true), FluctuateController()),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    //猫咪身体大小来自Wiki https://minecraft.wiki/w/Cat
    override fun getWidth(): Float {
        return if (this.isBaby()) 0.24f else 0.48f
    }

    override fun getHeight(): Float {
        return if (this.isBaby()) 0.28f else 0.56f
    }


    //攻击选择器
    //流浪猫会攻击兔子,小海龟
    override fun attackTarget(entity: Entity): Boolean {
        return when (entity.getEntityIdentifier().toString()) {
            EntityID.RABBIT, EntityID.TURTLE -> true
            else -> false
        }
    }

    override fun onUpdate(currentTick: Int): Boolean {
        //同步owner eid
        if (hasOwner()) {
            val owner = owner
            if (owner != null && getDataProperty<Long>(EntityDataTypes.OWNER_EID) != owner.getUniqueID()) {
                this.setDataProperty(EntityDataTypes.OWNER_EID, owner.getUniqueID())
            }
        }
        return super.onUpdate(currentTick)
    }

    public override fun initEntity() {
        this.maxHealth = 10
        super.initEntity()
        if (this.isBaby()) {
            this.setDataProperty(
                EntityDataTypes.AMBIENT_SOUND_EVENT_NAME,
                LevelSoundEventPacket.SOUND_AMBIENT_BABY
            )
        } else {
            this.setDataProperty(
                EntityDataTypes.AMBIENT_SOUND_EVENT_NAME,
                LevelSoundEventPacket.SOUND_AMBIENT
            )
        }
        if (!hasVariant()) {
            this.variant = (randomVariant())
        }
        //update CollarColor to Color
        if (namedTag!!.contains("CollarColor")) {
            this.setColor(DyeColor.getByWoolData(namedTag!!.getByte("CollarColor").toInt()))
        }
    }

    //猫咪有11种颜色变种
    override fun getAllVariant(): IntArray {
        return VARIANTS
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (item.id === ItemID.NAME_TAG && !player.isAdventure) {
            return playerApplyNameTag(player, item)
        }
        val healable = this.getHealingAmount(item)
        if (this.isBreedingItem(item)) {
            level!!.addSound(this.position, Sound.MOB_CAT_EAT)
            if (!this.hasOwner()) {
                player.inventory.decreaseCount(player.inventory.heldItemIndex)
                if (Utils.rand(1, 3) == 3) {
                    val packet = EntityEventPacket()
                    packet.eid = this.getRuntimeID()
                    packet.event = EntityEventPacket.TAME_SUCCESS
                    player.dataPacket(packet)

                    this.maxHealth = 10
                    this.setHealthSafe(10f)
                    this.setOwnerName(player.getEntityName())
                    this.setColor(DyeColor.RED)
                    this.saveNBT()

                    level!!.dropExpOrb(this.position, Utils.rand(1, 7))

                    return true
                } else {
                    val packet = EntityEventPacket()
                    packet.eid = this.getRuntimeID()
                    packet.event = EntityEventPacket.TAME_FAIL
                    player.dataPacket(packet)
                }
            } else {
                player.inventory.decreaseCount(player.inventory.heldItemIndex)
                level!!.addSound(this.position, Sound.MOB_CAT_EAT)
                level!!.addParticle(
                    ItemBreakParticle(
                        position.add(0.0, (getHeight() * 0.75f).toDouble(), 0.0),
                        Item.get(item.id, 0, 1)
                    )
                )

                if (healable != 0) {
                    this.setHealthSafe(max(maxHealth.toDouble(), (this.health + healable).toDouble()).toFloat())
                }
                memoryStorage.set<Int>(
                    CoreMemoryTypes.LAST_BE_FEED_TIME,
                    level!!.tick
                )
                memoryStorage.set(CoreMemoryTypes.LAST_FEED_PLAYER, player)
                return true
            }
        } else if (item.id === ItemID.DYE) {
            if (this.hasOwner() && player == this.owner) {
                player.inventory.decreaseCount(player.inventory.heldItemIndex)
                this.setColor((item as ItemDye).dyeColor)
                return true
            }
        } else if (this.hasOwner() && player.getEntityName() == getOwnerName() && !this.isTouchingWater()) {
            this.setSitting(!this.isSitting())
            return false
        }
        return false
    }

    //击杀猫会掉落0-2根线
    //击杀小猫不会获得
    override fun getDrops(): Array<Item> {
        if (!this.isBaby()) {
            val catdrops = Utils.rand(0, 2)
            if (catdrops > 0) return arrayOf(Item.get(ItemID.STRING, 0, catdrops))
        }
        return Item.EMPTY_ARRAY
    }

    override fun getOriginalName(): String {
        return "Cat"
    }

    /**
     * 绑定猫繁殖物品
     * WIKI了解只能使用生鲑鱼与生鳕鱼才能繁殖
     *
     *
     * Bound cat breeding items
     * WIKI understands that only raw salmon and raw cod can be used to breed
     */
    override fun isBreedingItem(item: Item): Boolean {
        return item.id === ItemID.SALMON ||
                item.id === ItemID.COD
    }

    /**
     * 获得可以治疗猫的物品的治疗量
     * WIKI了解只有生鲑鱼与生鳕鱼才能恢复猫咪血量恢复2
     *
     *
     * Obtain healing amount of items that can heal cats
     * WIKI understands that only raw salmon and raw cod can restore the cat's blood recovery 2
     */
    override fun getHealingAmount(item: Item): Int {
        return when (item.id) {
            ItemID.COD, ItemID.SALMON -> 2
            else -> 0
        }
    }

    companion object {
        //猫咪有11种颜色变种
        private val VARIANTS = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    }
}