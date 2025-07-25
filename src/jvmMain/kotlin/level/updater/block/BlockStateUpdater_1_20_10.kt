package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.OrderedUpdater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_20_10 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        for (color in COLORS) {
            if (color == "silver") {
                this.addTypeUpdater(ctx, "minecraft:concrete", "color", color, "minecraft:light_gray_concrete")
                this.addTypeUpdater(ctx, "minecraft:shulker_box", "color", color, "minecraft:light_gray_shulker_box")
            } else {
                this.addTypeUpdater(ctx, "minecraft:concrete", "color", color, "minecraft:" + color + "_concrete")
                this.addTypeUpdater(ctx, "minecraft:shulker_box", "color", color, "minecraft:" + color + "_shulker_box")
            }
        }

        this.addFacingDirectionUpdater(ctx, "minecraft:observer")
    }

    private fun addTypeUpdater(
        context: CompoundTagUpdaterContext,
        identifier: String,
        typeState: String,
        type: String,
        newIdentifier: String
    ) {
        context.addUpdater(1, 20, 10)
            .match("name", identifier)
            .visit("states")
            .match(typeState, type)
            .edit(
                typeState
            ) { helper: CompoundTagEditHelper -> helper.rootTag["name"] = newIdentifier }
            .remove(typeState)
    }

    private fun addFacingDirectionUpdater(ctx: CompoundTagUpdaterContext, identifier: String) {
        ctx.addUpdater(1, 20, 10)
            .match("name", identifier)
            .visit("states")
            .edit(
                OBSERVER_DIRECTIONS.oldProperty
            ) { helper: CompoundTagEditHelper ->
                val value = helper.tag as Int
                helper.replaceWith(
                    OBSERVER_DIRECTIONS.newProperty,
                    OBSERVER_DIRECTIONS.translate(value)
                ) // Don't ask me why namespace is in vanilla state
            }
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_20_10()

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

        /**
         * Literally the only block as of 1.20.30 that uses "minecraft:facing_direction"...
         * Seems equivalent to "minecraft:block_face"
         */
        val OBSERVER_DIRECTIONS: OrderedUpdater = OrderedUpdater(
            "facing_direction", "minecraft:facing_direction",
            "down", "up", "north", "south", "west", "east"
        )
    }
}
