package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent

data class ReplaceableComponent(
    val replaceable: Boolean,
) : BlockComponent<ReplaceableComponent> {
    override fun type(): ComponentType<ReplaceableComponent> = ReplaceableComponent

    companion object : ComponentType<ReplaceableComponent>()
}
