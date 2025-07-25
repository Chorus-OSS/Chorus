package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent
import org.chorus_oss.chorus.recipe.descriptor.ItemDescriptor

data class MineableComponent(
    val hardness: Float,
    val itemSpecificHardness: List<Pair<Float, ItemDescriptor>> = emptyList(),
) : BlockComponent<MineableComponent> {
    override fun type(): ComponentType<MineableComponent> = MineableComponent

    companion object : ComponentType<MineableComponent>() {
        val DEFAULT = MineableComponent(
            hardness = 10f
        )
    }
}
