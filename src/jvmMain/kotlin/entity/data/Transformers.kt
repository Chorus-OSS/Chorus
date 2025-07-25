package org.chorus_oss.chorus.entity.data

import org.chorus_oss.chorus.block.BlockState
import java.util.*
import java.util.function.Function

object Transformers {
    val BLOCK: Function<BlockState, Int> = (Function { it.blockStateHash() })
    val BOOLEAN_TO_BYTE: Function<Boolean, Byte> = (Function { b: Boolean -> if (b) 1.toByte() else 0.toByte() })
    val FLAGS: Function<EnumSet<EntityFlag>, Long> =
        Function { set: EnumSet<EntityFlag> ->
            var value: Long = 0
            val lower: Int = 0
            val upper: Int = lower + 64
            for (flag: EntityFlag in set) {
                val flagIndex: Int = flag.getValue()
                if (flagIndex >= lower && flagIndex < upper) {
                    value = value or (1L shl (flagIndex and 0x3f))
                }
            }
            value
        }
    val FLAGS_EXTEND: Function<EnumSet<EntityFlag>, Long> =
        Function { set: EnumSet<EntityFlag> ->
            var value: Long = 0
            val lower: Int = 64
            val upper: Int = lower + 64
            for (flag: EntityFlag in set) {
                val flagIndex: Int = flag.getValue()
                if (flagIndex >= lower && flagIndex < upper) {
                    value = value or (1L shl (flagIndex and 0x3f))
                }
            }
            value
        }
}
