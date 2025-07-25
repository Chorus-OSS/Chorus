package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.OrderedUpdater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_20_30 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        for (color in COLORS) {
            if (color == "silver") {
                this.addColorUpdater(ctx, "minecraft:stained_glass", color, "minecraft:light_gray_stained_glass")
                this.addColorUpdater(
                    ctx,
                    "minecraft:stained_glass_pane",
                    color,
                    "minecraft:light_gray_stained_glass_pane"
                )
                this.addColorUpdater(ctx, "minecraft:concrete_powder", color, "minecraft:light_gray_concrete_powder")
                this.addColorUpdater(ctx, "minecraft:stained_hardened_clay", color, "minecraft:light_gray_terracotta")
            } else {
                this.addColorUpdater(ctx, "minecraft:stained_glass", color, "minecraft:" + color + "_stained_glass")
                this.addColorUpdater(
                    ctx,
                    "minecraft:stained_glass_pane",
                    color,
                    "minecraft:" + color + "_stained_glass_pane"
                )
                this.addColorUpdater(ctx, "minecraft:concrete_powder", color, "minecraft:" + color + "_concrete_powder")
                this.addColorUpdater(
                    ctx,
                    "minecraft:stained_hardened_clay",
                    color,
                    "minecraft:" + color + "_terracotta"
                )
            }
        }

        this.addDirectionUpdater(ctx, "minecraft:amethyst_cluster", OrderedUpdater.Companion.FACING_TO_BLOCK)
        this.addDirectionUpdater(ctx, "minecraft:medium_amethyst_bud", OrderedUpdater.Companion.FACING_TO_BLOCK)
        this.addDirectionUpdater(ctx, "minecraft:large_amethyst_bud", OrderedUpdater.Companion.FACING_TO_BLOCK)
        this.addDirectionUpdater(ctx, "minecraft:small_amethyst_bud", OrderedUpdater.Companion.FACING_TO_BLOCK)

        this.addDirectionUpdater(ctx, "minecraft:blast_furnace", OrderedUpdater.Companion.FACING_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:furnace", OrderedUpdater.Companion.FACING_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:lit_blast_furnace", OrderedUpdater.Companion.FACING_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:lit_furnace", OrderedUpdater.Companion.FACING_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:lit_smoker", OrderedUpdater.Companion.FACING_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:smoker", OrderedUpdater.Companion.FACING_TO_CARDINAL)

        this.addDirectionUpdater(ctx, "minecraft:anvil", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:big_dripleaf", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(
            ctx,
            "minecraft:calibrated_sculk_sensor",
            OrderedUpdater.Companion.DIRECTION_TO_CARDINAL
        )
        this.addDirectionUpdater(ctx, "minecraft:campfire", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:end_portal_frame", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:lectern", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:pink_petals", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:powered_comparator", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:powered_repeater", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:small_dripleaf_block", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:soul_campfire", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:unpowered_comparator", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:unpowered_repeater", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)

        ctx.addUpdater(1, 20, 30)
            .regex("name", "minecraft:.+slab(?:[2-4])?\\b")
            .visit("states")
            .edit("top_slot_bit") { helper: CompoundTagEditHelper ->
                val value = if (helper.tag is Byte) {
                    (helper.tag as Byte).toInt() == 1
                } else {
                    helper.tag as Boolean
                }
                if (value) {
                    helper.replaceWith("minecraft:vertical_half", "top")
                } else {
                    helper.replaceWith("minecraft:vertical_half", "bottom")
                }
            }

        // TODO: Mojang added 51 updaters, I managed to do the same with less. Maybe I missed something? Need to check later.
    }

    private fun addColorUpdater(
        context: CompoundTagUpdaterContext,
        identifier: String,
        color: String,
        newIdentifier: String
    ) {
        context.addUpdater(1, 20, 30)
            .match("name", identifier)
            .visit("states")
            .match("color", color)
            .edit(
                "color"
            ) { helper: CompoundTagEditHelper -> helper.rootTag["name"] = newIdentifier }
            .remove("color")
    }

    private fun addDirectionUpdater(ctx: CompoundTagUpdaterContext, identifier: String, updater: OrderedUpdater) {
        ctx.addUpdater(1, 20, 30)
            .match("name", identifier)
            .visit("states")
            .edit(updater.oldProperty) { helper: CompoundTagEditHelper ->
                val value = helper.tag as Int
                helper.replaceWith(updater.newProperty, updater.translate(value))
            }
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_20_30()

        val COLORS: Array<String> = arrayOf(
            "magenta",
            "pink",
            "green",
            "lime",
            "yellow",
            "black",
            "light_blue",
            "brown",
            "cyan",
            "orange",
            "red",
            "gray",
            "white",
            "blue",
            "purple",
            "silver"
        )
    }
}
