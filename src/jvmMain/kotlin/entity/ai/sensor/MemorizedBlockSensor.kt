package org.chorus_oss.chorus.entity.ai.sensor

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob


//存储最近的玩家的Memory

class MemorizedBlockSensor @JvmOverloads constructor(
    protected var range: Int,
    protected var lookY: Int,
    override var period: Int = 1
) :
    ISensor {
    override fun sense(entity: EntityMob) {
        val blockClass = entity.memoryStorage[CoreMemoryTypes.LOOKING_BLOCK]!!
        var block: Block? = null
        for (x in -range..range) {
            for (z in -range..range) {
                for (y in -lookY..lookY) {
                    val lookTransform = entity.transform.add(x.toDouble(), y.toDouble(), z.toDouble())
                    val lookBlock = lookTransform.levelBlock
                    if (lookBlock.id == BlockID.DIRT || lookBlock.id == BlockID.GRASS_BLOCK || lookBlock.isAir || lookBlock.id == BlockID.BEDROCK) continue
                    if (blockClass.isAssignableFrom(lookBlock.javaClass)) {
                        block = lookBlock
                        break
                    }
                }
            }
        }
        entity.memoryStorage[CoreMemoryTypes.NEAREST_BLOCK] = block
    }
}
