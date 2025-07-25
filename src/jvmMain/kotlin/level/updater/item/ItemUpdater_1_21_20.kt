package org.chorus_oss.chorus.level.updater.item

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext
import java.util.function.Function

class ItemUpdater_1_21_20 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        this.addTypeUpdater(
            ctx, "minecraft:dirt", "dirt_type"
        ) { type: String? ->
            when (type) {
                "coarse" -> "minecraft:coarse_dirt"
                else -> "minecraft:dirt"
            }
        }


        this.addTypeUpdater(
            ctx, "minecraft:stone_block_slab", "stone_slab_type"
        ) { type: String? ->
            when (type) {
                "sandstone" -> "minecraft:sandstone_slab"
                "wood" -> "minecraft:oak_slab"
                "cobblestone" -> "minecraft:cobblestone_slab"
                "brick" -> "minecraft:brick_slab"
                "stone_brick" -> "minecraft:stone_brick_slab"
                "quartz" -> "minecraft:quartz_slab"
                "nether_brick" -> "minecraft:nether_brick_slab"
                else -> "minecraft:smooth_stone_slab"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:double_stone_block_slab", "stone_slab_type"
        ) { type: String? ->
            when (type) {
                "sandstone" -> "minecraft:sandstone_double_slab"
                "wood" -> "minecraft:oak_double_slab"
                "cobblestone" -> "minecraft:cobblestone_double_slab"
                "brick" -> "minecraft:brick_double_slab"
                "stone_brick" -> "minecraft:stone_brick_double_slab"
                "quartz" -> "minecraft:quartz_double_slab"
                "nether_brick" -> "minecraft:nether_brick_double_slab"
                else -> "minecraft:smooth_stone_double_slab"
            }
        }


        this.addTypeUpdater(
            ctx, "minecraft:stone_block_slab2", "stone_slab_type_2"
        ) { type: String? ->
            when (type) {
                "red_sandstone" -> "minecraft:red_sandstone_slab"
                "purpur" -> "minecraft:purpur_slab"
                "prismarine_rough" -> "minecraft:prismarine_slab"
                "prismarine_dark" -> "minecraft:dark_prismarine_slab"
                "prismarine_brick" -> "minecraft:prismarine_brick_slab"
                "mossy_cobblestone" -> "minecraft:mossy_cobblestone_slab"
                "smooth_sandstone" -> "minecraft:smooth_sandstone_slab"
                else -> "minecraft:red_nether_brick_slab"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:double_stone_block_slab2", "stone_slab_type_2"
        ) { type: String? ->
            when (type) {
                "red_sandstone" -> "minecraft:red_sandstone_double_slab"
                "purpur" -> "minecraft:purpur_double_slab"
                "prismarine_rough" -> "minecraft:prismarine_double_slab"
                "prismarine_dark" -> "minecraft:dark_prismarine_double_slab"
                "prismarine_brick" -> "minecraft:prismarine_brick_double_slab"
                "mossy_cobblestone" -> "minecraft:mossy_cobblestone_double_slab"
                "smooth_sandstone" -> "minecraft:smooth_sandstone_double_slab"
                else -> "minecraft:red_nether_brick_double_slab"
            }
        }


        this.addTypeUpdater(
            ctx, "minecraft:stone_block_slab3", "stone_slab_type_3"
        ) { type: String? ->
            when (type) {
                "end_stone_brick" -> "minecraft:end_stone_brick_slab"
                "smooth_red_sandstone" -> "minecraft:smooth_red_sandstone_slab"
                "polished_andesite" -> "minecraft:polished_andesite_slab"
                "andesite" -> "minecraft:andesite_slab"
                "diorite" -> "minecraft:diorite_slab"
                "polished_diorite" -> "minecraft:polished_diorite_slab"
                "granite" -> "minecraft:granite_slab"
                else -> "minecraft:polished_granite_slab"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:double_stone_block_slab3", "stone_slab_type_3"
        ) { type: String? ->
            when (type) {
                "end_stone_brick" -> "minecraft:end_stone_brick_double_slab"
                "smooth_red_sandstone" -> "minecraft:smooth_red_sandstone_double_slab"
                "polished_andesite" -> "minecraft:polished_andesite_double_slab"
                "andesite" -> "minecraft:andesite_double_slab"
                "diorite" -> "minecraft:diorite_double_slab"
                "polished_diorite" -> "minecraft:polished_diorite_double_slab"
                "granite" -> "minecraft:granite_double_slab"
                else -> "minecraft:polished_granite_double_slab"
            }
        }


        this.addTypeUpdater(
            ctx, "minecraft:stone_block_slab4", "stone_slab_type_4"
        ) { type: String? ->
            when (type) {
                "mossy_stone_brick" -> "minecraft:mossy_stone_brick_slab"
                "smooth_quartz" -> "minecraft:smooth_quartz_slab"
                "stone" -> "minecraft:normal_stone_slab"
                "cut_sandstone" -> "minecraft:cut_sandstone_slab"
                else -> "minecraft:cut_red_sandstone_slab"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:double_stone_block_slab4", "stone_slab_type_4"
        ) { type: String? ->
            when (type) {
                "mossy_stone_brick" -> "minecraft:mossy_stone_brick_double_slab"
                "smooth_quartz" -> "minecraft:smooth_quartz_double_slab"
                "stone" -> "minecraft:normal_stone_double_slab"
                "cut_sandstone" -> "minecraft:cut_sandstone_double_slab"
                else -> "minecraft:cut_red_sandstone_double_slab"
            }
        }


        ctx.addUpdater(1, 21, 20)
            .match("Name", "minecraft:light_block")
            .edit("Name") { helper: CompoundTagEditHelper ->
                val block =
                    helper.rootTag["Block"] as? Map<*, *> ?: return@edit
                val states = block["states"] as? Map<*, *> ?: return@edit

                val lightLevel = states["block_light_level"] as Int

                val newId = "minecraft:light_block_$lightLevel"
                helper.rootTag["Name"] = newId
            }


        this.addTypeUpdater(
            ctx, "minecraft:monster_egg", "monster_egg_stone_type"
        ) { type: String? ->
            when (type) {
                "stone" -> BlockID.INFESTED_STONE
                "cobblestone" -> BlockID.INFESTED_COBBLESTONE
                "stone_brick" -> BlockID.INFESTED_STONE_BRICKS
                "mossy_stone_brick" -> BlockID.INFESTED_MOSSY_STONE_BRICKS
                "cracked_stone_brick" -> BlockID.INFESTED_CRACKED_STONE_BRICKS
                else -> BlockID.INFESTED_CHISELED_STONE_BRICKS
            }
        }


        this.addTypeUpdater(
            ctx, "minecraft:prismarine", "prismarine_block_type"
        ) { type: String? ->
            when (type) {
                "default" -> BlockID.PRISMARINE
                "dark" -> BlockID.DARK_PRISMARINE
                else -> BlockID.PRISMARINE_BRICKS
            }
        }


        this.addTypeUpdater(
            ctx, "minecraft:quartz_block", "chisel_type"
        ) { type: String? ->
            when (type) {
                "default" -> BlockID.QUARTZ_BLOCK
                "chiseled" -> BlockID.CHISELED_QUARTZ_BLOCK
                "lines" -> BlockID.QUARTZ_PILLAR
                else -> BlockID.SMOOTH_QUARTZ
            }
        }


        this.addTypeUpdater(
            ctx, "minecraft:red_sandstone", "sand_stone_type"
        ) { type: String? ->
            when (type) {
                "default" -> BlockID.RED_SANDSTONE
                "heiroglyphs" -> BlockID.CHISELED_RED_SANDSTONE
                "cut" -> BlockID.CUT_SANDSTONE
                else -> BlockID.SMOOTH_SANDSTONE
            }
        }


        this.addTypeUpdater(
            ctx, "minecraft:sand", "sand_type"
        ) { type: String? ->
            when (type) {
                "normal" -> BlockID.SAND
                else -> BlockID.RED_SAND
            }
        }


        this.addTypeUpdater(
            ctx, "minecraft:sandstone", "sandstone_type"
        ) { type: String? ->
            when (type) {
                "default" -> BlockID.SANDSTONE
                "heiroglyphs" -> BlockID.CHISELED_SANDSTONE
                "cut" -> BlockID.CUT_SANDSTONE
                else -> BlockID.SMOOTH_SANDSTONE
            }
        }


        this.addTypeUpdater(
            ctx, "minecraft:stonebrick", "stone_brick_type"
        ) { type: String? ->
            when (type) {
                "default" -> BlockID.STONE_BRICKS
                "mossy" -> BlockID.MOSSY_STONE_BRICKS
                "cracked" -> BlockID.CRACKED_STONE_BRICKS
                else -> BlockID.CHISELED_STONE_BRICKS
            }
        }


        ctx.addUpdater(1, 21, 20)
            .match("Name", "minecraft:yellow_flower")
            .edit(
                "Name"
            ) { helper: CompoundTagEditHelper -> helper.rootTag["Name"] = BlockID.DANDELION }


        this.addTypeUpdater(
            ctx, "minecraft:anvil", "damage"
        ) { type: String? ->
            when (type) {
                "slightly_damaged" -> BlockID.CHIPPED_ANVIL
                "very_damaged" -> BlockID.DAMAGED_ANVIL
                "broken" -> BlockID.AIR
                else -> BlockID.ANVIL
            }
        }
    }


    private fun addTypeUpdater(
        context: CompoundTagUpdaterContext,
        identifier: String,
        typeState: String,
        rename: Function<String, String>
    ) {
        context.addUpdater(1, 21, 20)
            .match("Name", identifier)
            .edit("Name") { helper: CompoundTagEditHelper ->
                val block = helper.rootTag["Block"]
                if (block is Map<*, *>) {
                    val states = block["states"]
                    if (states is Map<*, *>) {
                        val tag = states[typeState]
                        if (tag is String) {
                            helper.rootTag["Name"] = rename.apply(tag)
                        }
                    }
                }
            }
    }

    companion object {
        val INSTANCE: Updater = ItemUpdater_1_21_20()
    }
}
