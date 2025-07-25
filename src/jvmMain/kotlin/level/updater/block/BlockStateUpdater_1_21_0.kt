package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext
import java.util.function.Function

class BlockStateUpdater_1_21_0 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        ctx.addUpdater(1, 21, 0)
            .match("name", "minecraft:coral_block")
            .edit("states") { helper: CompoundTagEditHelper ->
                val type = helper.compoundTag.remove("coral_color") as String?
                val bit = helper.compoundTag.remove("dead_bit")
                val dead =
                    bit is Byte && bit.toInt() == 1 || bit is Boolean && bit

                val newName = when (type) {
                    "blue" -> "minecraft:" + (if (dead) "dead_" else "") + "tube_coral_block"
                    "pink" -> "minecraft:" + (if (dead) "dead_" else "") + "brain_coral_block"
                    "purple" -> "minecraft:" + (if (dead) "dead_" else "") + "bubble_coral_block"
                    "yellow" -> "minecraft:" + (if (dead) "dead_" else "") + "horn_coral_block"
                    else -> "minecraft:" + (if (dead) "dead_" else "") + "fire_coral_block"
                }
                helper.rootTag["name"] = newName
            }

        this.addTypeUpdater(
            ctx, "minecraft:double_plant", "double_plant_type"
        ) { type: String? ->
            when (type) {
                "syringa" -> "minecraft:lilac"
                "grass" -> "minecraft:tall_grass"
                "fern" -> "minecraft:large_fern"
                "rose" -> "minecraft:rose_bush"
                "paeonia" -> "minecraft:peony"
                else -> "minecraft:sunflower"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:stone_block_slab", "stone_slab_type"
        ) { type: String? ->
            when (type) {
                "quartz" -> "minecraft:quartz_slab"
                "wood" -> "minecraft:petrified_oak_slab"
                "stone_brick" -> "minecraft:stone_brick_slab"
                "brick" -> "minecraft:brick_slab"
                "smooth_stone" -> "minecraft:smooth_stone_slab"
                "sandstone" -> "minecraft:sandstone_slab"
                "nether_brick" -> "minecraft:nether_brick_slab"
                else -> "minecraft:cobblestone_slab"
            }
        }

        this.addTypeUpdater(
            ctx, "minecraft:tallgrass", "tall_grass_type"
        ) { type: String? ->
            when (type) {
                "fern" -> "minecraft:fern"
                else -> "minecraft:short_grass"
            }
        }

        // These are not official updaters
        ctx.addUpdater(1, 21, 0, false, false)
            .match("name", "minecraft:trial_spawner")
            .visit("states")
            .tryAdd("ominous", 0.toByte())

        ctx.addUpdater(1, 21, 0, false, false)
            .match("name", "minecraft:vault")
            .visit("states")
            .tryAdd("ominous", 0.toByte())
    }

    private fun addTypeUpdater(
        context: CompoundTagUpdaterContext,
        identifier: String,
        typeState: String,
        rename: Function<String?, String>
    ) {
        context.addUpdater(1, 21, 0)
            .match("name", identifier)
            .visit("states")
            .edit(
                typeState
            ) { helper: CompoundTagEditHelper -> helper.rootTag["name"] = rename.apply(helper.tag as String) }
            .remove(typeState)
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_21_0()
    }
}