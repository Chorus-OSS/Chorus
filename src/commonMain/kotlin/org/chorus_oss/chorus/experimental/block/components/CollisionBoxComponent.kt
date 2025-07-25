package org.chorus_oss.chorus.experimental.block.components

import com.github.quillraven.fleks.ComponentType
import org.chorus_oss.chorus.experimental.block.BlockComponent
import org.chorus_oss.chorus.math.Vector3f

data class CollisionBoxComponent(
    val origin: Vector3f,
    val size: Vector3f,
    val enabled: Boolean,
): BlockComponent<CollisionBoxComponent> {
    constructor(enabled: Boolean): this(Vector3f(0f, 0f, 0f), Vector3f(1f, 1f, 1f), enabled)

    constructor(origin: Vector3f, size: Vector3f): this(origin, size, true)

    override fun type(): ComponentType<CollisionBoxComponent> = CollisionBoxComponent

    companion object : ComponentType<CollisionBoxComponent>() {
        val DEFAULT = CollisionBoxComponent(
            origin = Vector3f(0f, 0f, 0f),
            size = Vector3f(1f, 1f, 1f),
            enabled = true,
        )
    }
}
