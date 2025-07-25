package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext
import java.util.function.Function

class BlockStateUpdater_1_21_20 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        ctx.addUpdater(1, 21, 20)
            .match("name", "minecraft:light_block")
            .visit("states")
            .edit(
                "block_light_level"
            ) { helper: CompoundTagEditHelper -> helper.rootTag["name"] = "minecraft:light_block_" + helper.tag }
            .remove("block_light_level")


        this.addTypeUpdater(
            ctx, "minecraft:sandstone", "sand_stone_type"
        ) { type: String? ->
            when (type) {
                "cut" -> "minecraft:cut_sandstone"
                "heiroglyphs" -> "minecraft:chiseled_sandstone"
                "smooth" -> "minecraft:smooth_sandstone"
                else -> "minecraft:sandstone"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:quartz_block", "chisel_type"
        ) { type: String? ->
            when (type) {
                "chiseled" -> "minecraft:chiseled_quartz_block"
                "lines" -> "minecraft:quartz_pillar"
                "smooth" -> "minecraft:smooth_quartz"
                else -> "minecraft:quartz_block"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:red_sandstone", "sand_stone_type"
        ) { type: String? ->
            when (type) {
                "cut" -> "minecraft:cut_red_sandstone"
                "heiroglyphs" -> "minecraft:chiseled_red_sandstone"
                "smooth" -> "minecraft:smooth_red_sandstone"
                else -> "minecraft:red_sandstone"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:sand", "sand_type"
        ) { type: String? ->
            when (type) {
                "red" -> "minecraft:red_sand"
                else -> "minecraft:sand"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:dirt", "dirt_type"
        ) { type: String? ->
            when (type) {
                "coarse" -> "minecraft:coarse_dirt"
                else -> "minecraft:dirt"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:anvil", "damage"
        ) { type: String? ->
            when (type) {
                "broken" -> "minecraft:damaged_anvil"
                "slightly_damaged" -> "minecraft:chipped_anvil"
                "very_damaged" -> "minecraft:deprecated_anvil"
                else -> "minecraft:anvil"
            }
        }

        // Vanilla does not use updater for this block for some reason
        ctx.addUpdater(1, 21, 20, false, false)
            .match("name", "minecraft:yellow_flower")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("name", "minecraft:dandelion")
            }
    }

    private fun addTypeUpdater(
        context: CompoundTagUpdaterContext,
        identifier: String,
        typeState: String,
        rename: Function<String?, String>
    ) {
        context.addUpdater(1, 21, 20)
            .match("name", identifier)
            .visit("states")
            .edit(
                typeState
            ) { helper: CompoundTagEditHelper -> helper.rootTag["name"] = rename.apply(helper.tag as String) }
            .remove(typeState)
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_21_20()
    }
}
