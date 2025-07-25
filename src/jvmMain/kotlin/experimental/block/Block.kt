package org.chorus_oss.chorus.experimental.block

import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.Vector3i

class Block(
    val permutation: BlockPermutation,
    val position: Vector3i,
    val layer: Int,
    val level: Level,
) {
    val collisionComponent: CollisionBoxComponent
        get() = permutation[CollisionBoxComponent] ?: CollisionBoxComponent.DEFAULT

    val mapColorComponent: MapColorComponent
        get() = permutation[MapColorComponent] ?: MapColorComponent.DEFAULT

    val mineableComponent: MineableComponent
        get() = permutation[MineableComponent] ?: MineableComponent.DEFAULT

    val flammableComponent: FlammableComponent
        get() = permutation[FlammableComponent] ?: FlammableComponent.DEFAULT

    val frictionComponent: FrictionComponent
        get() = permutation[FrictionComponent] ?: FrictionComponent.DEFAULT

    val internalFrictionComponent: InternalFrictionComponent
        get() = permutation[InternalFrictionComponent] ?: InternalFrictionComponent.DEFAULT

    val tickComponent: TickComponent?
        get() = permutation[TickComponent]
}
