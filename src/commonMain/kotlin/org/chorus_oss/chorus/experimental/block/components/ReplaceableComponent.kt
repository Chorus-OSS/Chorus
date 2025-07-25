package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent

data object ReplaceableComponent : BlockComponent<ReplaceableComponent>, ComponentType<ReplaceableComponent>() {
    override fun type(): ComponentType<ReplaceableComponent> = ReplaceableComponent
}
