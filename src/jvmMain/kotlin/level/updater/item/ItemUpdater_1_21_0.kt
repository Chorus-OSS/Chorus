package org.chorus_oss.chorus.level.updater.item

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext
import java.util.function.Function

class ItemUpdater_1_21_0 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        ctx.addUpdater(1, 21, 0)
            .match("Name", "minecraft:coral_block")
            .edit("Name") { helper: CompoundTagEditHelper ->
                val block = helper.rootTag["Block"]
                if (block is Map<*, *>) {
                    val states = block["states"]
                    if (states is Map<*, *>) {
                        val type = states["coral_color"].toString()
                        val bit = states["dead_bit"]
                        val dead =
                            bit is Byte && bit.toInt() == 1 || bit is Boolean && bit
                        val newName = when (type) {
                            "blue" -> "minecraft:" + (if (dead) "dead_" else "") + "tube_coral_block"
                            "pink" -> "minecraft:" + (if (dead) "dead_" else "") + "brain_coral_block"
                            "purple" -> "minecraft:" + (if (dead) "dead_" else "") + "bubble_coral_block"
                            "yellow" -> "minecraft:" + (if (dead) "dead_" else "") + "horn_coral_block"
                            else -> "minecraft:" + (if (dead) "dead_" else "") + "fire_coral_block"
                        }
                        helper.rootTag["Name"] = newName
                    }
                }
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
    }

    private fun addTypeUpdater(
        context: CompoundTagUpdaterContext,
        identifier: String,
        typeState: String,
        rename: Function<String, String>
    ) {
        context.addUpdater(1, 21, 0)
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
        val INSTANCE: Updater = ItemUpdater_1_21_0()
    }
}
