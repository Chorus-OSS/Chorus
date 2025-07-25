package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext
import java.util.function.Function

class BlockStateUpdater_1_21_10 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        this.addTypeUpdater(
            ctx, "minecraft:stone_block_slab2", "stone_slab_type_2"
        ) { type: String? ->
            when (type) {
                "prismarine_rough" -> "minecraft:prismarine_slab"
                "prismarine_dark" -> "minecraft:dark_prismarine_slab"
                "smooth_sandstone" -> "minecraft:smooth_sandstone_slab"
                "purpur" -> "minecraft:purpur_slab"
                "red_nether_brick" -> "minecraft:red_nether_brick_slab"
                "prismarine_brick" -> "minecraft:prismarine_brick_slab"
                "mossy_cobblestone" -> "minecraft:mossy_cobblestone_slab"
                else -> "minecraft:red_sandstone_slab"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:stone_block_slab3", "stone_slab_type_3"
        ) { type: String? ->
            when (type) {
                "smooth_red_sandstone" -> "minecraft:smooth_red_sandstone_slab"
                "polished_granite" -> "minecraft:polished_granite_slab"
                "granite" -> "minecraft:granite_slab"
                "polished_diorite" -> "minecraft:polished_diorite_slab"
                "andesite" -> "minecraft:andesite_slab"
                "polished_andesite" -> "minecraft:polished_andesite_slab"
                "diorite" -> "minecraft:diorite_slab"
                else -> "minecraft:end_stone_brick_slab"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:stone_block_slab4", "stone_slab_type_4"
        ) { type: String? ->
            when (type) {
                "smooth_quartz" -> "minecraft:smooth_quartz_slab"
                "cut_sandstone" -> "minecraft:cut_sandstone_slab"
                "cut_red_sandstone" -> "minecraft:cut_red_sandstone_slab"
                "stone" -> "minecraft:normal_stone_slab"
                else -> "minecraft:mossy_stone_brick_slab"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:double_stone_block_slab", "stone_slab_type"
        ) { type: String? ->
            when (type) {
                "quartz" -> "minecraft:quartz_double_slab"
                "wood" -> "minecraft:petrified_oak_double_slab"
                "stone_brick" -> "minecraft:stone_brick_double_slab"
                "brick" -> "minecraft:brick_double_slab"
                "sandstone" -> "minecraft:sandstone_double_slab"
                "nether_brick" -> "minecraft:nether_brick_double_slab"
                "cobblestone" -> "minecraft:cobblestone_double_slab"
                else -> "minecraft:smooth_stone_double_slab"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:double_stone_block_slab2", "stone_slab_type_2"
        ) { type: String? ->
            when (type) {
                "prismarine_rough" -> "minecraft:prismarine_double_slab"
                "prismarine_dark" -> "minecraft:dark_prismarine_double_slab"
                "smooth_sandstone" -> "minecraft:smooth_sandstone_double_slab"
                "purpur" -> "minecraft:purpur_double_slab"
                "red_nether_brick" -> "minecraft:red_nether_brick_double_slab"
                "prismarine_brick" -> "minecraft:prismarine_brick_double_slab"
                "mossy_cobblestone" -> "minecraft:mossy_cobblestone_double_slab"
                else -> "minecraft:red_sandstone_double_slab"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:double_stone_block_slab3", "stone_slab_type_3"
        ) { type: String? ->
            when (type) {
                "smooth_red_sandstone" -> "minecraft:smooth_red_sandstone_double_slab"
                "polished_granite" -> "minecraft:polished_granite_double_slab"
                "granite" -> "minecraft:granite_double_slab"
                "polished_diorite" -> "minecraft:polished_diorite_double_slab"
                "andesite" -> "minecraft:andesite_double_slab"
                "polished_andesite" -> "minecraft:polished_andesite_double_slab"
                "diorite" -> "minecraft:diorite_double_slab"
                else -> "minecraft:end_stone_brick_double_slab"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:double_stone_block_slab4", "stone_slab_type_4"
        ) { type: String? ->
            when (type) {
                "smooth_quartz" -> "minecraft:smooth_quartz_double_slab"
                "cut_sandstone" -> "minecraft:cut_sandstone_double_slab"
                "cut_red_sandstone" -> "minecraft:cut_red_sandstone_double_slab"
                "stone" -> "minecraft:normal_stone_double_slab"
                else -> "minecraft:mossy_stone_brick_double_slab"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:prismarine", "prismarine_block_type"
        ) { type: String? ->
            when (type) {
                "bricks" -> "minecraft:prismarine_bricks"
                "dark" -> "minecraft:dark_prismarine"
                else -> "minecraft:prismarine"
            }
        }

        this.addCoralUpdater(ctx, "minecraft:coral_fan_hang", "tube_coral_wall_fan", "brain_coral_wall_fan")
        this.addCoralUpdater(ctx, "minecraft:coral_fan_hang2", "bubble_coral_wall_fan", "fire_coral_wall_fan")
        this.addCoralUpdater(ctx, "minecraft:coral_fan_hang3", "horn_coral_wall_fan", null)

        this.addTypeUpdater(
            ctx, "minecraft:monster_egg", "monster_egg_stone_type"
        ) { type: String? ->
            when (type) {
                "cobblestone" -> "minecraft:infested_cobblestone"
                "stone_brick" -> "minecraft:infested_stone_bricks"
                "mossy_stone_brick" -> "minecraft:infested_mossy_stone_bricks"
                "cracked_stone_brick" -> "minecraft:infested_cracked_stone_bricks"
                "chiseled_stone_brick" -> "minecraft:infested_chiseled_stone_bricks"
                else -> "minecraft:infested_stone"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:stonebrick", "stone_brick_type"
        ) { type: String? ->
            when (type) {
                "mossy" -> "minecraft:mossy_stone_bricks"
                "cracked" -> "minecraft:cracked_stone_bricks"
                "chiseled" -> "minecraft:chiseled_stone_bricks"
                else -> "minecraft:stone_bricks"
            }
        }
    }

    private fun addTypeUpdater(
        context: CompoundTagUpdaterContext,
        identifier: String,
        typeState: String,
        rename: Function<String?, String>
    ) {
        context.addUpdater(1, 21, 10)
            .match("name", identifier)
            .visit("states")
            .edit(
                typeState
            ) { helper: CompoundTagEditHelper -> helper.rootTag["name"] = rename.apply(helper.tag as String) }
            .remove(typeState)
    }

    private fun addCoralUpdater(context: CompoundTagUpdaterContext, identifier: String, type1: String, type2: String?) {
        context.addUpdater(1, 21, 10)
            .match("name", identifier)
            .edit("states") { helper: CompoundTagEditHelper ->
                val deadBit = helper.compoundTag.remove("dead_bit")
                val dead =
                    deadBit is Byte && deadBit.toInt() == 1 || deadBit is Boolean && deadBit

                val typeBit = helper.compoundTag.remove("coral_hang_type_bit") // always remove
                val type = if (type2 == null) {
                    type1
                } else {
                    if (typeBit is Byte && typeBit.toInt() == 1 || typeBit is Boolean && typeBit) type2 else type1
                }
                helper.rootTag["name"] = if (dead) "minecraft:dead_$type" else "minecraft:$type"
            }
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_21_10()
    }
}
