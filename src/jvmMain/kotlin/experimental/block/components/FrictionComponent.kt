package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent

data class FrictionComponent(
    val friction: Float
) : BlockComponent<FrictionComponent> {
    override fun type(): ComponentType<FrictionComponent> = FrictionComponent

    companion object : ComponentType<FrictionComponent>() {
        val DEFAULT = FrictionComponent(
            friction = 0.6f
        )
    }
}
