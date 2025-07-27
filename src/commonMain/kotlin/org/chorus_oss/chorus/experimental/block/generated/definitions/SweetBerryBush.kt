package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object SweetBerryBush : BlockDefinition(
    identifier = "minecraft:sweet_berry_bush",
    states = listOf(CommonStates.growth),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 0, g = 124, b = 0, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        FlammableComponent(catchChance = 30, destroyChance = 60),
        MineableComponent(hardness = 0.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(enabled = false)
    ),
    permutations = listOf(
        Permutation(
            { (it["growth"] == 1) || (it["growth"] == 2) || (it["growth"] == 3) || (it["growth"] == 4) || (it["growth"] == 5) || (it["growth"] == 6) || (it["growth"] == 7) },
            listOf(MineableComponent(hardness = 0.25f))
        )
    )
)
