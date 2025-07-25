package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent

@Suppress("unused")
data class MoveableComponent(
    val movement: Movement = Movement.Both,
    val sticky: Boolean = false,
): BlockComponent<MoveableComponent> {
    enum class Movement {
        Both,
        Push,
        Break,
        None
    }

    override fun type(): ComponentType<MoveableComponent> = MoveableComponent

    companion object : ComponentType<MoveableComponent>()
}
