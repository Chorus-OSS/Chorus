package org.chorus_oss.chorus.entity.ai.sensor

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.mob.EntityMob
import java.util.function.Function

/**
 * 用来搜索最近的目标实体，构造函数中接受一个目标函数`Function<T, Boolean> target`的Set，用于实体检测，最终结果保存到`List<MemoryType<Entity>> memories`中.
 *
 *
 * The constructor accepts a Set of Integer to target function `Function<T, Boolean> target` to search for the nearest target entity, and the final result is saved to `List<MemoryType<Entity>> memories`.
 */
class NearestTargetEntitySensor<T : Entity?> @SafeVarargs constructor(
    protected var minRange: Double,
    protected var maxRange: Double,
    override var period: Int,
    protected var memories: List<NullableMemoryType<Entity>>,
    vararg allTargetFunction: Function<T, Boolean>
) :
    ISensor {
    protected var allTargetFunction: Array<Function<T, Boolean>>?

    /**
     * 不指定目标函数，默认将全部结果存入第一个记忆
     *
     *
     * Without specifying the target function, all results will be stored in the first memory by default
     *
     * @see .NearestTargetEntitySensor
     */
    constructor(minRange: Double, maxRange: Double, memories: List<NullableMemoryType<Entity>>) : this(
        minRange,
        maxRange,
        1,
        memories
    )

    /**
     * @param minRange          最小搜索范围<br></br>Minimum Search Range
     * @param maxRange          最大搜索范围<br></br>Maximum Search Range
     * @param period            传感器执行周期，单位tick<br></br>Senor execute period
     * @param allTargetFunction 接收一个Set，将指定目标函数筛选的结果映射到指定索引的记忆上，目标函数接受一个参数T，返回一个Boolean<br></br>Receives a Set that set the results filtered by the specified target function to the memory of the specified index, the target function accepts a parameter T and returns a Boolean
     * @param memories          保存结果的记忆类型<br></br>Memory class type for saving results
     */
    init {
        if (memories.isNotEmpty() && allTargetFunction.size == memories.size) {
            this.allTargetFunction = allTargetFunction.toList().toTypedArray()
        } else throw IllegalArgumentException("All Target Function must correspond to memories one by one")
    }

    override fun sense(entity: EntityMob) {
        val minRangeSquared = this.minRange * this.minRange
        val maxRangeSquared = this.maxRange * this.maxRange

        if (allTargetFunction == null && memories.size == 1) {
            val currentMemory = memories[0]
            val current = entity.memoryStorage[currentMemory]
            if (current != null && current.isAlive()) return

            //寻找范围内最近的实体
            val entities = sortedSetOf(compareBy<Entity> {
                it.position.distanceSquared(
                    entity.position
                )
            })
            for (p in entity.level!!.entities.values) {
                if (entity.position.distanceSquared(p.position) in minRangeSquared..maxRangeSquared && (p != entity)) {
                    entities.add(p)
                }
            }

            if (entities.isEmpty()) {
                entity.memoryStorage.clear(currentMemory)
            } else entity.memoryStorage[currentMemory] = entities.firstOrNull()
            return
        }
        if (allTargetFunction != null) {
            val sortEntities: MutableList<MutableSet<Entity>> = ArrayList(memories.size)

            run {
                var i = 0
                val len = memories.size
                while (i < len) {
                    sortEntities.add(sortedSetOf(compareBy {
                        it.position.distanceSquared(
                            entity.position
                        )
                    }))
                    ++i
                }
            }

            for (p in entity.level!!.entities.values) {
                if (entity.position.distanceSquared(p.position) in minRangeSquared..maxRangeSquared && (p != entity)) {
                    for ((i, targetFunction) in allTargetFunction!!.withIndex()) {
                        if (targetFunction.apply(p as T)) {
                            sortEntities[i].add(p)
                        }
                    }
                }
            }

            var i = 0
            val len = sortEntities.size
            while (i < len) {
                val currentMemory = memories[i]
                val current = entity.memoryStorage[currentMemory]
                if (current != null && current.isAlive()) {
                    ++i
                    continue
                }

                if (sortEntities[i].isEmpty()) {
                    entity.memoryStorage.clear(currentMemory)
                } else entity.memoryStorage[currentMemory] = sortEntities[i].firstOrNull()
                ++i
            }
        }
    }
}