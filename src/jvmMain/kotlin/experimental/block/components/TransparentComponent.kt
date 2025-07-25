package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent

data class TransparentComponent(
    val transparent: Boolean,
): BlockComponent<TransparentComponent> {
    override fun type(): ComponentType<TransparentComponent> = TransparentComponent

    companion object : ComponentType<TransparentComponent>() {
        val DEFAULT = TransparentComponent(
            transparent = false,
        )
    }
}
