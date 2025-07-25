package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.utils.DyeColor
import java.util.concurrent.ThreadLocalRandom

interface EntityColor : EntityComponent {
    fun setColor(color: DyeColor) {
        memoryStorage[CoreMemoryTypes.COLOR] = Integer.valueOf(color.woolData).toByte()
    }

    fun setColor2(color: DyeColor) {
        memoryStorage[CoreMemoryTypes.COLOR2] = Integer.valueOf(color.woolData).toByte()
    }

    fun getColor(): DyeColor {
        return DyeColor.getByWoolData(memoryStorage[CoreMemoryTypes.COLOR]!!.toInt())
    }

    fun getColor2(): DyeColor {
        return DyeColor.getByWoolData(memoryStorage[CoreMemoryTypes.COLOR2]!!.toInt())
    }

    fun hasColor(): Boolean {
        return memoryStorage.notEmpty(CoreMemoryTypes.COLOR)
    }

    fun hasColor2(): Boolean {
        return memoryStorage.notEmpty(CoreMemoryTypes.COLOR2)
    }

    fun getRandomColor(): DyeColor {
        val random: ThreadLocalRandom = ThreadLocalRandom.current()
        val colors: Array<DyeColor> = DyeColor.entries.toTypedArray()
        val c: Int = random.nextInt(colors.size)
        return colors[c]
    }
}
