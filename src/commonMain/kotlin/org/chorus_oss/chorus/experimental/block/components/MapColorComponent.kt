package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent

data class MapColorComponent(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int,
) : BlockComponent<MapColorComponent> {
    override fun type(): ComponentType<MapColorComponent> = MapColorComponent

    companion object : ComponentType<MapColorComponent>() {
        val DEFAULT = MapColorComponent(
            r = 0,
            g = 0,
            b = 0,
            a = 0,
        )
    }
}
