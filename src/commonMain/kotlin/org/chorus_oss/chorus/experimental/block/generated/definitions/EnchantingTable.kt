package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.math.Vector3f

object EnchantingTable : BlockDefinition(
    identifier = "minecraft:enchanting_table",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 153, g = 51, b = 51, a = 255),
        LightEmissionComponent(emission = 7),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 5.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.75f, z = 1.0f)
        )
    )
)
