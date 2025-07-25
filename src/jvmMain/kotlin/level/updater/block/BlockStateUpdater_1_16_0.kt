package org.chorus_oss.chorus.level.updater.block


import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_16_0 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        context.addUpdater(1, 16, 0)
            .match("name", "jigsaw")
            .visit("states")
            .tryAdd("rotation", 0)

        context.addUpdater(1, 16, 0)
            .match("name", "minecraft:blue_fire")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("name", "soul_fire")
            }

        context.addUpdater(1, 16, 0)
            .match("name", "minecraft:blue_nether_wart_block")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("name", "warped_wart_block")
            }

        context.addUpdater(1, 16, 0)
            .match("name", "minecraft:shroomlight_block")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("name", "minecraft:shroomlight")
            }

        context.addUpdater(1, 16, 0)
            .match("name", "minecraft:weeping_vines_block")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("name", "minecraft:weeping_vines")
            }

        context.addUpdater(1, 16, 0)
            .match("name", "minecraft:basalt_block")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("name", "minecraft:basalt")
            }

        context.addUpdater(1, 16, 0)
            .match("name", "minecraft:polished_basalt_block")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("name", "minecraft:polished_basalt")
            }

        context.addUpdater(1, 16, 0)
            .match("name", "minecraft:soul_soil_block")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("name", "minecraft:soul_soil")
            }

        context.addUpdater(1, 16, 0)
            .match("name", "minecraft:target_block")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("name", "minecraft:target")
            }

        context.addUpdater(1, 16, 0)
            .match("name", "minecraft:crimson_trap_door")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("name", "minecraft:crimsom_trapdoor")
            }

        context.addUpdater(1, 16, 0)
            .match("name", "minecraft:lodestone_block")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("name", "minecraft:lodestone")
            }

        context.addUpdater(1, 16, 0)
            .match("name", "minecraft:twisted_vines_block")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("name", "minecraft:twisted_vines")
            }

        // This is not a vanilla state updater. In vanilla 1.16, the invalid block state is updated when the chunk is
        // loaded in so it can generate the connection data however the state set below should never occur naturally.
        // Checking for this block state instead means we don't have to break our loading system in order to support it.
        this.addLegacyWallUpdater(context, "minecraft:.+_wall")
        this.addLegacyWallUpdater(context, "minecraft:border_block")

        this.addWallUpdater(context, "minecraft:blackstone_wall")
        this.addWallUpdater(context, "minecraft:polished_blackstone_brick_wall")
        this.addWallUpdater(context, "minecraft:polished_blackstone_wall")

        this.addBeeHiveUpdater(context, "minecraft:beehive")
        this.addBeeHiveUpdater(context, "minecraft:bee_nest")

        this.addRequiredValueUpdater(context, "minecraft:pumpkin_stem", "facing_direction", 0)
        this.addRequiredValueUpdater(context, "minecraft:melon_stem", "facing_direction", 0)
    }

    private fun addLegacyWallUpdater(context: CompoundTagUpdaterContext, name: String) {
        context.addUpdater(1, 16, 0)
            .regex("name", name)
            .tryEdit("states") { helper: CompoundTagEditHelper ->
                val states = helper.compoundTag
                states["wall_post_bit"] = 0.toByte()
                states["wall_connection_type_north"] = "none"
                states["wall_connection_type_east"] = "none"
                states["wall_connection_type_south"] = "none"
                states["wall_connection_type_west"] = "none"
            }
    }

    private fun addWallUpdater(context: CompoundTagUpdaterContext, name: String) {
        context.addUpdater(1, 16, 0)
            .match("name", name)
            .visit("states")
            .remove("wall_block_type")
    }

    private fun addBeeHiveUpdater(context: CompoundTagUpdaterContext, name: String) {
        context.addUpdater(1, 16, 0)
            .match("name", name)
            .visit("states")
            .edit("facing_direction") { helper: CompoundTagEditHelper ->
                val facingDirection = helper.tag as Int
                helper.replaceWith(
                    "direction",
                    convertFacingDirectionToDirection(facingDirection)
                )
            }
    }

    private fun addRequiredValueUpdater(contex: CompoundTagUpdaterContext, name: String, state: String, value: Any) {
        contex.addUpdater(1, 16, 0)
            .match("name", name)
            .visit("states")
            .tryAdd(state, value)
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_16_0()

        private fun convertFacingDirectionToDirection(facingDirection: Int): Int {
            return when (facingDirection) {
                2 -> 2
                3 -> 0
                4 -> 1
                5 -> 3
                else -> 0
            }
        }
    }
}
