package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent

data class LightEmissionComponent(
    val emission: Int,
): BlockComponent<LightEmissionComponent> {
    override fun type(): ComponentType<LightEmissionComponent> = LightEmissionComponent

    companion object : ComponentType<LightEmissionComponent>()
}
