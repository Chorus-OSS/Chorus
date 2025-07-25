package org.chorus_oss.chorus.entity.ai.sensor

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.mob.EntityMob


//存储最近的玩家的Memory

class BlockSensor @JvmOverloads constructor(
    protected var blockClass: Class<out Block>,
    protected var memory: NullableMemoryType<Block>,
    protected var range: Int,
    protected var lookY: Int,
    override var period: Int = 1
) :
    ISensor {
    override fun sense(entity: EntityMob) {
        var block: Block? = null
        for (x in -range..range) {
            for (z in -range..range) {
                for (y in -lookY..lookY) {
                    val lookTransform = entity.transform.add(x.toDouble(), y.toDouble(), z.toDouble())
                    val lookBlock = lookTransform.levelBlock
                    if (blockClass.isAssignableFrom(lookBlock.javaClass)) {
                        block = lookBlock
                        break
                    }
                }
            }
        }
        if (block == null) {
            if (entity.memoryStorage.notEmpty(memory) && (blockClass.isAssignableFrom(
                    entity.memoryStorage[memory]!!.javaClass
                ) || entity.memoryStorage[memory]!!.isAir)
            ) {
                entity.memoryStorage.clear(memory)
            } // We don't want to clear data from different sensors
        } else entity.memoryStorage[memory] = block
    }
}
