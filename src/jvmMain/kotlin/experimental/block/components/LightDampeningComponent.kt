package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent

data class LightDampeningComponent(
    val dampening: Int,
): BlockComponent<LightDampeningComponent> {
    override fun type(): ComponentType<LightDampeningComponent> = LightDampeningComponent

    companion object : ComponentType<LightDampeningComponent>() {
        val DEFAULT = LightDampeningComponent(
            dampening = 15
        )
    }
}