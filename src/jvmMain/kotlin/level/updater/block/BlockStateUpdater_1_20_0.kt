package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.OrderedUpdater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_20_0 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        for (color in COLORS) {
            if (color == "silver") {
                this.addTypeUpdater(ctx, "minecraft:carpet", "color", color, "minecraft:light_gray_carpet")
            } else {
                this.addTypeUpdater(ctx, "minecraft:carpet", "color", color, "minecraft:" + color + "_carpet")
            }
        }

        this.addCoralUpdater(ctx, "red", "minecraft:fire_coral")
        this.addCoralUpdater(ctx, "pink", "minecraft:brain_coral")
        this.addCoralUpdater(ctx, "blue", "minecraft:tube_coral")
        this.addCoralUpdater(ctx, "yellow", "minecraft:horn_coral")
        this.addCoralUpdater(ctx, "purple", "minecraft:bubble_coral")

        ctx.addUpdater(1, 20, 0)
            .match("name", "minecraft:calibrated_sculk_sensor")
            .visit("states")
            .edit("powered_bit") { helper: CompoundTagEditHelper ->
                val value = if (helper.tag is Byte) {
                    (helper.tag as Byte).toInt()
                } else {
                    helper.tag as Int
                }
                helper.replaceWith("sculk_sensor_phase", value)
            }

        ctx.addUpdater(1, 20, 0)
            .match("name", "minecraft:sculk_sensor")
            .visit("states")
            .edit("powered_bit") { helper: CompoundTagEditHelper ->
                val value = if (helper.tag is Byte) {
                    (helper.tag as Byte).toInt()
                } else {
                    helper.tag as Int
                }
                helper.replaceWith("sculk_sensor_phase", value)
            }

        this.addPumpkinUpdater(ctx, "minecraft:carved_pumpkin")
        this.addPumpkinUpdater(ctx, "minecraft:lit_pumpkin")
        this.addPumpkinUpdater(ctx, "minecraft:pumpkin")

        this.addCauldronUpdater(ctx, "water")
        this.addCauldronUpdater(ctx, "lava")
        this.addCauldronUpdater(ctx, "powder_snow")
    }

    private fun addTypeUpdater(
        context: CompoundTagUpdaterContext,
        identifier: String,
        typeState: String,
        type: String,
        newIdentifier: String
    ) {
        context.addUpdater(1, 20, 0)
            .match("name", identifier)
            .visit("states")
            .match(typeState, type)
            .edit(
                typeState
            ) { helper: CompoundTagEditHelper -> helper.rootTag["name"] = newIdentifier }
            .remove(typeState)
    }

    private fun addPumpkinUpdater(ctx: CompoundTagUpdaterContext, identifier: String) {
        val updater: OrderedUpdater = OrderedUpdater.Companion.DIRECTION_TO_CARDINAL
        ctx.addUpdater(1, 20, 0)
            .match("name", identifier)
            .visit("states")
            .edit(updater.oldProperty) { helper: CompoundTagEditHelper ->
                val value = helper.tag as Int
                helper.replaceWith(
                    updater.newProperty,
                    updater.translate(value)
                ) // Don't ask me why namespace is in vanilla state
            }
    }

    private fun addCauldronUpdater(ctx: CompoundTagUpdaterContext, type: String) {
        ctx.addUpdater(1, 20, 0)
            .match("name", "minecraft:lava_cauldron")
            .visit("states")
            .match("cauldron_liquid", type)
            .popVisit()
            .tryEdit("states") { helper: CompoundTagEditHelper ->
                val states = helper.compoundTag
                states["cauldron_liquid"] = type
                helper.rootTag["name"] = "minecraft:cauldron"
            }
    }

    private fun addCoralUpdater(context: CompoundTagUpdaterContext, type: String, newIdentifier: String) {
        // Two updates to match final version
        context.addUpdater(1, 20, 0)
            .match("name", "minecraft:coral")
            .visit("states")
            .match("coral_color", type)
            .match("dead_bit", "0")
            .edit(
                "coral_color"
            ) { helper: CompoundTagEditHelper -> helper.rootTag["name"] = newIdentifier }
            .remove("coral_color")
            .remove("dead_bit")

        context.addUpdater(1, 20, 0)
            .match("name", "minecraft:coral")
            .visit("states")
            .match("coral_color", type)
            .match("dead_bit", "1")
            .edit(
                "coral_color"
            ) { helper: CompoundTagEditHelper -> helper.rootTag["name"] = newIdentifier }
            .remove("coral_color")
            .remove("dead_bit")
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_20_0()

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
