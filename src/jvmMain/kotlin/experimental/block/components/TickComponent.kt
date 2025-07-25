package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent

@Suppress("unused")
data class TickComponent(
    val range: IntRange,
    val looping: Boolean,
): BlockComponent<TickComponent> {
    override fun type(): ComponentType<TickComponent> = TickComponent

    constructor(min: Int, max: Int, looping: Boolean): this(min..max, looping)

    constructor(range: Pair<Int, Int>, looping: Boolean): this(range.first..range.second, looping)

    companion object : ComponentType<TickComponent>()
}
