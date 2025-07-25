package org.chorus_oss.chorus.entity.ai.behaviorgroup

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.ai.EntityAI
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.BehaviorState
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.memory.IMemoryStorage
import org.chorus_oss.chorus.entity.ai.memory.MemoryStorage
import org.chorus_oss.chorus.entity.ai.route.RouteFindingManager
import org.chorus_oss.chorus.entity.ai.route.RouteFindingManager.RouteFindingTask
import org.chorus_oss.chorus.entity.ai.route.finder.IRouteFinder
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.Vector3
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.math.max
import kotlin.math.min

/**
 * 标准行为组实现
 */


class BehaviorGroup(
    /**
     * 记录距离上次路径更新过去的gt数
     */
    private var currentRouteUpdateTick: Int, //gt
    /**
     * 不会被其他行为覆盖的"核心“行为
     */
    private val coreBehaviors: Set<IBehavior>,
    /**
     * 全部行为
     */
    private val behaviors: Set<IBehavior>,
    /**
     * 传感器
     */
    private val sensors: Set<ISensor>,
    /**
     * 控制器
     */
    private val controllers: Set<IController>,
    /**
     * 寻路器(非异步，因为没必要，生物AI本身就是并行的)
     */
    private val routeFinder: IRouteFinder?,
    /**
     * 此行为组所属实体
     */
    entity: EntityMob
) : IBehaviorGroup {

    override fun getBehaviors(): Set<IBehavior> {
        return behaviors
    }

    override fun getCoreBehaviors(): Set<IBehavior> {
        return coreBehaviors
    }

    override fun getSensors(): Set<ISensor> {
        return sensors
    }

    override fun getControllers(): Set<IController> {
        return controllers
    }

    override fun getRouteFinder(): IRouteFinder? {
        return routeFinder
    }

    /**
     * 正在运行的”核心“行为
     */
    private val runningCoreBehaviors: MutableSet<IBehavior> = HashSet()

    override fun getRunningCoreBehaviors(): Set<IBehavior> {
        return runningCoreBehaviors
    }

    /**
     * 正在运行的行为
     */
    private val runningBehaviors: MutableSet<IBehavior> = HashSet()

    override fun getRunningBehaviors(): Set<IBehavior> {
        return runningBehaviors
    }

    /**
     * 用于存储核心行为距离上次评估逝去的gt数
     */
    private val coreBehaviorPeriodTimer: MutableMap<IBehavior, Int> = HashMap()

    /**
     * 用于存储行为距离上次评估逝去的gt数
     */
    private val behaviorPeriodTimer: MutableMap<IBehavior, Int> = HashMap()

    /**
     * 用于存储传感器距离上次刷新逝去的gt数
     */
    private val sensorPeriodTimer: MutableMap<ISensor, Int> = HashMap()

    /**
     * 记忆存储器
     */
    //此参数用于错开各个实体路径更新的时间，避免在1gt内提交过多路径更新任务
    override val memoryStorage: IMemoryStorage = MemoryStorage(entity)

    /**
     * 寻路任务
     */
    private var routeFindingTask: RouteFindingTask? = null

    private var blockChangeCache: Long = 0

    override var isForceUpdateRoute: Boolean = false

    init {
        this.initPeriodTimer()
    }

    /**
     * 运行并刷新正在运行的行为
     */
    override fun tickRunningBehaviors(entity: EntityMob) {
        val iterator = runningBehaviors.iterator()
        while (iterator.hasNext()) {
            val behavior = iterator.next()
            if (!behavior.execute(entity)) {
                behavior.onStop(entity)
                behavior.behaviorState = BehaviorState.STOP
                iterator.remove()
            }
        }
    }

    override fun tickRunningCoreBehaviors(entity: EntityMob) {
        val iterator = runningCoreBehaviors.iterator()
        while (iterator.hasNext()) {
            val coreBehavior = iterator.next()
            if (coreBehavior is Behavior) {
                if (coreBehavior.reevaluate && !coreBehavior.evaluate(entity)) {
                    coreBehavior.onInterrupt(entity)
                    coreBehavior.behaviorState = (BehaviorState.STOP)
                    iterator.remove()
                    continue
                }
            }
            if (!coreBehavior.execute(entity)) {
                coreBehavior.onStop(entity)
                coreBehavior.behaviorState = BehaviorState.STOP
                iterator.remove()
            }
        }
    }

    override fun collectSensorData(entity: EntityMob) {
        sensorPeriodTimer.forEach { (sensor, tick) ->
            var tick1 = tick
            tick1++
            //刷新gt数
            sensorPeriodTimer[sensor] = tick1
            //没到周期就不评估
            if (sensorPeriodTimer[sensor]!! < sensor.period) return@forEach
            sensorPeriodTimer[sensor] = 0
            sensor.sense(entity)
        }
    }

    override fun evaluateCoreBehaviors(entity: EntityMob) {
        coreBehaviorPeriodTimer.forEach { (coreBehavior, tick) ->
            var tick1 = tick
            tick1++
            //若已经在运行了，就不需要评估了
            if (runningCoreBehaviors.contains(coreBehavior)) return@forEach
            val nextTick: Int = tick1
            //刷新gt数
            coreBehaviorPeriodTimer[coreBehavior] = nextTick
            //没到周期就不评估
            if (nextTick < coreBehavior.period) return@forEach
            coreBehaviorPeriodTimer[coreBehavior] = 0
            if (coreBehavior.evaluate(entity)) {
                coreBehavior.onStart(entity)
                coreBehavior.behaviorState = BehaviorState.ACTIVE
                runningCoreBehaviors.add(coreBehavior)
            }
        }
    }

    /**
     * 评估所有行为
     *
     * @param entity 评估的实体对象
     */
    override fun evaluateBehaviors(entity: EntityMob) {
        //存储评估成功的行为（未过滤优先级）
        val evalSucceed = HashSet<IBehavior>(behaviors.size)
        var highestPriority = Int.MIN_VALUE
        for (entry in behaviorPeriodTimer.entries) {
            val behavior = entry.key
            //若已经在运行了，就不需要评估了
            if (runningBehaviors.contains(behavior)) continue
            var tick = entry.value
            val nextTick = ++tick
            //刷新gt数
            behaviorPeriodTimer[behavior] = nextTick
            //没到周期就不评估
            if (nextTick < behavior.period) continue
            behaviorPeriodTimer[behavior] = 0
            if (behavior.evaluate(entity)) {
                if (behavior.priority > highestPriority) {
                    evalSucceed.clear()
                    highestPriority = behavior.priority
                } else if (behavior.priority < highestPriority) {
                    continue
                }
                evalSucceed.add(behavior)
            }
        }
        //如果没有评估结果，则返回空
        if (evalSucceed.isEmpty()) return
        val first = if (runningBehaviors.isEmpty()) null else runningBehaviors.iterator().next()
        val runningBehaviorPriority = first?.priority ?: Int.MIN_VALUE
        var firstEval = true
        if (first != null) {
            if (first is Behavior) {
                if (first.reevaluate) {
                    firstEval = first.evaluate(entity)
                }
            }
        }
        //如果result的优先级低于当前运行的行为，则不执行
        if (highestPriority < runningBehaviorPriority && firstEval) {
            //do nothing
        } else if (highestPriority > runningBehaviorPriority || !firstEval) {
            //如果result的优先级比当前运行的行为的优先级高，则替换当前运行的所有行为
            interruptAllRunningBehaviors(entity)
            addToRunningBehaviors(entity, evalSucceed)
        } else {
            //如果result的优先级和当前运行的行为的优先级一样，则添加result的行为
            addToRunningBehaviors(entity, evalSucceed)
        }
    }

    override fun applyController(entity: EntityMob) {
        for (controller in controllers) {
            controller.control(entity)
        }
    }

    override fun updateRoute(entity: EntityMob) {
        currentRouteUpdateTick++
        val reachUpdateCycle = currentRouteUpdateTick >= calcActiveDelay(
            entity,
            ROUTE_UPDATE_CYCLE + (entity.level!!.tickRateOptDelay shl 1)
        )
        if (reachUpdateCycle) currentRouteUpdateTick = 0
        val target = entity.moveTarget
        if (target == null) {
            //没有路径目标，则清除路径信息
            entity.moveDirectionStart = null
            entity.moveDirectionEnd = null
            return
        }
        //到达更新周期时，开始重新计算新路径
        if (isForceUpdateRoute || (reachUpdateCycle && shouldUpdateRoute(entity))) {
            //若有路径目标，则计算新路径
            var reSubmit = false
            //         第一次计算                       上一次计算已完成                                         超时，重新提交任务
            if (routeFindingTask == null || routeFindingTask!!.getFinished() || ((!routeFindingTask!!.getStarted() && Server.instance.nextTick - routeFindingTask!!.getStartTime() > 8).also {
                    reSubmit = it
                })) {
                if (reSubmit) routeFindingTask!!.cancel(true)
                //clone防止寻路器潜在的修改
                RouteFindingManager.instance
                    .submit(RouteFindingTask(routeFinder!!, object : RouteFindingTask.FinishCallback {
                        override fun onFinish(task: RouteFindingTask?) {
                            updateMoveDirection(entity)
                            entity.isShouldUpdateMoveDirection = false
                            isForceUpdateRoute = (false)
                            //写入section变更记录
                            cacheSectionBlockChange(
                                entity.level!!,
                                calPassByChunkSections(
                                    routeFinder.route.stream()
                                        .map { it.vector3 }
                                        .toList(),
                                    entity.level!!))
                        }
                    }).setStart(entity.position.clone()).setTarget(target).also { routeFindingTask = it })
            }
        }
        if (routeFindingTask != null && routeFindingTask!!.getFinished() && !hasNewUnCalMoveTarget(entity)) {
            //若不能再移动了，且没有正在计算的寻路任务，则清除路径信息
            val reachableTarget = routeFinder!!.reachableTarget
            if (reachableTarget != null && entity.position.floor() == reachableTarget.floor()) {
                entity.moveTarget = null
                entity.moveDirectionStart = null
                entity.moveDirectionEnd = null
                return
            }
        }
        if (entity.isShouldUpdateMoveDirection) {
            if (routeFinder!!.hasNext()) {
                //若有新的移动方向，则更新
                updateMoveDirection(entity)
                entity.isShouldUpdateMoveDirection = false
            }
        }
    }

    /**
     * 检查路径是否需要更新。此方法检测路径经过的ChunkSection是否发生了变化
     *
     * @return 是否需要更新路径
     */
    private fun shouldUpdateRoute(entity: EntityMob): Boolean {
        //此优化只针对处于非active区块的实体
        if (entity.isActive) return true
        //终点发生变化或第一次计算，需要重算
        if (routeFinder!!.target == null || hasNewUnCalMoveTarget(entity)) return true
        val passByChunkSections =
            calPassByChunkSections(
                routeFinder.route.stream().map { it.vector3 }.toList(),
                entity.level!!
            )
        val total = passByChunkSections.stream().mapToLong { vector3: ChunkSectionVector ->
            getSectionBlockChange(
                entity.level!!, vector3
            )
        }.sum()
        //Section发生变化，需要重算
        return blockChangeCache != total
    }

    /**
     * 通过比对寻路器中设置的moveTarget与entity的moveTarget来确认实体是否设置了新的未计算的moveTarget
     *
     * @param entity 实体
     * @return 是否存在新的未计算的寻路目标
     */
    private fun hasNewUnCalMoveTarget(entity: EntityMob): Boolean {
        return entity.moveTarget != routeFinder?.target
    }

    /**
     * 缓存section的blockChanges到blockChangeCache
     */
    private fun cacheSectionBlockChange(level: Level, vectors: Set<ChunkSectionVector>) {
        this.blockChangeCache =
            vectors.stream().mapToLong { vector3: ChunkSectionVector -> getSectionBlockChange(level, vector3) }.sum()
    }

    /**
     * 返回sectionVector对应的section的blockChanges
     */
    private fun getSectionBlockChange(level: Level, vector: ChunkSectionVector): Long {
        val chunk = level.getChunk(vector.chunkX, vector.chunkZ)
        return chunk.getSectionBlockChanges(vector.sectionY)
    }

    /**
     * 计算坐标集经过的ChunkSection
     *
     * @return (chunkX | chunkSectionY | chunkZ)
     */
    private fun calPassByChunkSections(nodes: Collection<Vector3>, level: Level): Set<ChunkSectionVector> {
        return nodes.stream()
            .map { vector3: Vector3 ->
                val dimensionData = level.dimensionData
                val chunkX = vector3.chunkX
                val y = min(
                    dimensionData.maxHeight.toDouble(),
                    max(dimensionData.minHeight.toDouble(), (vector3.floorY - dimensionData.minHeight).toDouble())
                ).toInt()
                val chunkZ = vector3.chunkZ
                ChunkSectionVector(chunkX, y shr 4, chunkZ)
            }
            .collect(Collectors.toSet())
    }

    override fun debugTick(entity: EntityMob) {
        val strBuilder = StringBuilder()

        if (EntityAI.checkDebugOption(EntityAI.DebugOption.MEMORY)) {
            val sortedMemory = ArrayList(memoryStorage.all.entries)
            sortedMemory.sortWith(
                Comparator.comparing(
                    { s -> s.key.identifier.path },
                    { obj: String, anotherString: String? ->
                        obj.compareTo(
                            anotherString!!
                        )
                    })
            )
            sortedMemory.reverse()

            for ((key, value) in sortedMemory) {
                strBuilder.append("§e" + key.identifier.path)
                strBuilder.append("=")
                strBuilder.append("§7$value")
                strBuilder.append("\n")
            }
            strBuilder.append("\n\n")
        }

        if (EntityAI.checkDebugOption(EntityAI.DebugOption.BEHAVIOR)) {
            if (coreBehaviors.isNotEmpty()) {
                val sortedCoreBehaviors = ArrayList(coreBehaviors)
                sortedCoreBehaviors.sortWith(
                    Comparator.comparing(
                        { obj: IBehavior -> obj.priority },
                        { obj: Int, anotherInteger: Int? ->
                            obj.compareTo(
                                anotherInteger!!
                            )
                        })
                )
                sortedCoreBehaviors.reverse()

                for (behavior in sortedCoreBehaviors) {
                    strBuilder.append(if (behavior.behaviorState == BehaviorState.ACTIVE) "§b" else "§7")
                    strBuilder.append(behavior)
                    strBuilder.append("\n")
                }
                strBuilder.append("\n\n")
            }

            val sortedBehaviors = ArrayList(behaviors)
            sortedBehaviors.sortWith(
                Comparator.comparing(
                    { obj: IBehavior -> obj.priority },
                    { obj: Int, anotherInteger: Int? ->
                        obj.compareTo(
                            anotherInteger!!
                        )
                    })
            )
            sortedBehaviors.reverse()

            for (behavior in sortedBehaviors) {
                strBuilder.append(if (behavior.behaviorState == BehaviorState.ACTIVE) "§b" else "§7")
                strBuilder.append(behavior)
                strBuilder.append("\n")
            }
        }

        entity.setNameTag(strBuilder.toString())
        entity.setNameTagAlwaysVisible(true)
    }

    /**
     * 计算活跃实体延迟
     *
     * @param entity        实体
     * @param originalDelay 原始延迟
     * @return 如果实体是非活跃的，则延迟*4，否则返回原始延迟
     */
    private fun calcActiveDelay(entity: EntityMob, originalDelay: Int): Int {
        if (!entity.isActive) {
            return originalDelay shl 2
        }
        return originalDelay
    }

    private fun initPeriodTimer() {
        coreBehaviors.forEach(Consumer { coreBehavior: IBehavior ->
            coreBehaviorPeriodTimer[coreBehavior] = 0
        })
        behaviors.forEach(Consumer { behavior: IBehavior ->
            behaviorPeriodTimer[behavior] = 0
        })
        sensors.forEach(Consumer { sensor: ISensor -> sensorPeriodTimer[sensor] = 0 })
    }

    private fun updateMoveDirection(entity: EntityMob) {
        var end = entity.moveDirectionEnd
        if (end == null) {
            end = entity.position.clone()
        }
        val next = routeFinder?.next()
        if (next != null) {
            entity.moveDirectionStart = end
            entity.moveDirectionEnd = next.vector3
        }
    }

    /**
     * 添加评估成功后的行为到[BehaviorGroup.runningBehaviors]
     *
     * @param entity    评估的实体
     * @param behaviors 要添加的行为
     */
    private fun addToRunningBehaviors(entity: EntityMob, behaviors: Set<IBehavior>) {
        behaviors.forEach(Consumer { behavior: IBehavior ->
            behavior.onStart(entity)
            behavior.behaviorState = BehaviorState.ACTIVE
            runningBehaviors.add(behavior)
        })
    }

    /**
     * 中断所有正在运行的行为
     */
    private fun interruptAllRunningBehaviors(entity: EntityMob) {
        for (behavior in runningBehaviors) {
            behavior.onInterrupt(entity)
            behavior.behaviorState = BehaviorState.STOP
        }
        runningBehaviors.clear()
    }

    /**
     * 描述一个ChunkSection的位置
     *
     * @param chunkX
     * @param sectionY
     * @param chunkZ
     */
    @JvmRecord
    private data class ChunkSectionVector(val chunkX: Int, val sectionY: Int, val chunkZ: Int) {
        override fun equals(other: Any?): Boolean {
            if (other !is ChunkSectionVector) {
                return false
            }

            return this.chunkX == other.chunkX && this.sectionY == other.sectionY && this.chunkZ == other.chunkZ
        }

        override fun hashCode(): Int {
            return (chunkX xor (chunkZ shl 12)) xor (sectionY shl 24)
        }
    }

    companion object {
        /**
         * 决定多少gt更新一次路径
         */
        private var ROUTE_UPDATE_CYCLE: Int = 16 //gt
    }
}
