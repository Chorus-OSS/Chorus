package org.chorus_oss.chorus.entity.mob


import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityFlyable
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityOwnable
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LiftController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.SpaceMoveController
import org.chorus_oss.chorus.entity.ai.evaluator.ConditionalProbabilityEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.MemoryCheckNotEmptyEvaluator
import org.chorus_oss.chorus.entity.ai.executor.EntityMoveToOwnerExecutor
import org.chorus_oss.chorus.entity.ai.executor.LookAtTargetExecutor
import org.chorus_oss.chorus.entity.ai.executor.MoveToTargetExecutor
import org.chorus_oss.chorus.entity.ai.executor.SpaceRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleSpaceAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.FlyingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestItemSensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.inventory.EntityInventoryHolder
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.inventory.InventorySlice
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.function.Function

class EntityAllay(chunk: IChunk?, nbt: CompoundTag) : EntityMob(chunk, nbt), EntityFlyable, EntityOwnable,
    EntityInventoryHolder {
    override fun getEntityIdentifier(): String {
        return EntityID.ALLAY
    }


    private var lastItemDropTick: Int = -1


    var dropCollectCooldown: Int = 60


    override fun initEntity() {
        super.initEntity()
        updateMemory()
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.NEAREST_ITEM, 0.22f, true),
                    MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_ITEM),
                    5,
                    1
                ),
                Behavior(EntityMoveToOwnerExecutor(0.4f, true, 64, -1), IBehaviorEvaluator { entity: EntityMob? ->
                    if (this.hasOwner()) {
                        val player: Player? = owner
                        val distanceSquared: Double = position.distanceSquared(player!!.position)
                        return@IBehaviorEvaluator distanceSquared >= 100
                    } else return@IBehaviorEvaluator false
                }, 4, 1),
                Behavior(
                    LookAtTargetExecutor(CoreMemoryTypes.NEAREST_PLAYER, 100),
                    ConditionalProbabilityEvaluator(
                        3, 7,
                        Function<Entity, Boolean> { entity: Entity? -> hasOwner(false) }, 10
                    ),
                    1,
                    1,
                    25
                ),
                Behavior(
                    SpaceRandomRoamExecutor(0.15f, 12, 100, 20, false, -1, true, 10),
                    (IBehaviorEvaluator { entity: EntityMob? -> true }),
                    1,
                    1
                )
            ),
            setOf<ISensor>(
                NearestItemSensor(32.0, 0.0, 20),
                NearestPlayerSensor(64.0, 0.0, 20)
            ),
            setOf<IController>(SpaceMoveController(), LookController(true, true), LiftController()),
            SimpleSpaceAStarRouteFinder(FlyingPosEvaluator(), this),
            this
        )
    }

    override fun getHeight(): Float {
        return 0.6f
    }

    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getOriginalName(): String {
        return "Allay"
    }

    override fun getExperienceDrops(): Int {
        return 0
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (item.isNothing) {
            setOwnerName("")
            setItemInHand(Item.AIR)
        } else {
            setOwnerName(player.getEntityName())
            val itemInHand: Item = player.inventory.itemInHand.clone().clearNamedTag()
            itemInHand.setCount(1)
            setItemInHand(itemInHand)
        }
        updateMemory()
        return super.onInteract(player, item, clickedPos)
    }

    private fun updateMemory() {
        val item = itemInHand
        if (item.isNothing) {
            memoryStorage.clear(CoreMemoryTypes.LOOKING_ITEM)
        } else memoryStorage[CoreMemoryTypes.LOOKING_ITEM] = item.javaClass
    }

    override val inventory: Inventory
        get() {
            //0 = hand, 1 = offhand
            return InventorySlice(equipment, 2, 3) // TODO
        }

    override fun onUpdate(currentTick: Int): Boolean {
        if (currentTick % 10 == 0) {
            val nearestItem = memoryStorage[CoreMemoryTypes.NEAREST_ITEM]
            if (nearestItem != null && !nearestItem.closed) {
                if (nearestItem.position.distance(this.position) < 1 && currentTick - lastItemDropTick > dropCollectCooldown) {
                    val item: Item = nearestItem.item
                    val currentItem: Item = inventory.getItem(0).clone()
                    if (inventory.canAddItem(item)) {
                        if (currentItem.isNothing) {
                            inventory.setItem(0, item)
                        } else {
                            item.setCount(item.getCount() + currentItem.getCount())
                            inventory.setItem(0, item)
                        }
                        level!!.addSound(this.position, Sound.RANDOM_POP)
                        nearestItem.close()
                    }
                }
            } else {
                if (hasOwner()) {
                    if (position.distance(owner!!.position) < 2) {
                        dropItem(currentTick)
                    }
                }
            }
        }
        return super.onUpdate(currentTick)
    }

    private fun dropItem(currentTick: Int): Boolean {
        if (!this.isAlive()) {
            return false
        }
        val item: Item = inventory.getItem(0)
        if (item.isNothing) return true
        val motion: Vector3 = getDirectionVector().multiply(0.4)
        level!!.dropItem(position.add(0.0, 1.3, 0.0), item, motion, 40)
        inventory.clearAll()
        this.lastItemDropTick = currentTick
        return true
    }

    override fun getDrops(): Array<Item> {
        return inventory.contents.values.toTypedArray()
    }

    companion object {
        const val TAG_ALLAY_DUPLICATION_COOLDOWN: String = "AllayDuplicationCooldown"
        const val TAG_VIBRATION_LISTENER: String = "VibrationListener"
    }
}
