package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent

data class InternalFrictionComponent(
    val internalFriction: Float
) : BlockComponent<InternalFrictionComponent> {
    override fun type(): ComponentType<InternalFrictionComponent> = InternalFrictionComponent

    companion object : ComponentType<InternalFrictionComponent>() {
        val DEFAULT = InternalFrictionComponent(
            internalFriction = 1f
        )
    }
}