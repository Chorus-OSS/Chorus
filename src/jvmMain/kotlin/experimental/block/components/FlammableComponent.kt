package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent

data class FlammableComponent(
    val catchChance: Int,
    val destroyChance: Int,
): BlockComponent<FlammableComponent> {
    override fun type(): ComponentType<FlammableComponent> = FlammableComponent

    companion object : ComponentType<FlammableComponent>() {
        val DEFAULT = FlammableComponent(
            catchChance = 0,
            destroyChance = 0,
        )
    }
}
