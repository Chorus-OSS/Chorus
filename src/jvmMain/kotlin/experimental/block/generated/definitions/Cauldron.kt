package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Cauldron : BlockDefinition(
    identifier = "minecraft:cauldron",
    states = listOf(CommonStates.cauldronLiquid, CommonStates.fillLevel),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 112, g = 112, b = 112, a = 255),
        LightDampeningComponent(dampening = 3),
        MineableComponent(hardness = 2.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.3f, y = 0.3f, z = 0.3f),
            size = Vector3f(x = 0.39999999999999997f, y = 0.39999999999999997f, z = 0.39999999999999997f)
        )
    ),
    permutations = listOf(
        Permutation(
        { it["cauldron_liquid"] == "lava" },
        listOf(LightEmissionComponent(emission = 15))
    ),
        Permutation(
            { it["cauldron_liquid"] == "lava" && it["fill_level"] == 1 },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["cauldron_liquid"] == "lava" && it["fill_level"] == 2 },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["cauldron_liquid"] == "lava" && it["fill_level"] == 3 },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["cauldron_liquid"] == "lava" && it["fill_level"] == 4 },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["cauldron_liquid"] == "lava" && it["fill_level"] == 5 },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["cauldron_liquid"] == "lava" && it["fill_level"] == 6 },
            listOf(LightEmissionComponent(emission = 15))
        )
    )
)
