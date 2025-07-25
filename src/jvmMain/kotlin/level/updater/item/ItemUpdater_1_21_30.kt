package org.chorus_oss.chorus.level.updater.item

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext
import java.util.function.Function

class ItemUpdater_1_21_30 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        this.addTypeUpdater(
            context,
            "minecraft:chemistry_table",
            "chemistry_table_type"
        ) { type: String -> "minecraft:$type" }

        this.addTypeUpdater(
            context, "minecraft:cobblestone_wall", "wall_block_type"
        ) { type: String? ->
            when (type) {
                "prismarine" -> "minecraft:prismarine_wall"
                "red_sandstone" -> "minecraft:red_sandstone_wall"
                "mossy_stone_brick" -> "minecraft:mossy_stone_brick_wall"
                "mossy_cobblestone" -> "minecraft:mossy_cobblestone_wall"
                "sandstone" -> "minecraft:sandstone_wall"
                "nether_brick" -> "minecraft:nether_brick_wall"
                "granite" -> "minecraft:granite_wall"
                "red_nether_brick" -> "minecraft:red_nether_brick_wall"
                "stone_brick" -> "minecraft:stone_brick_wall"
                "end_brick" -> "minecraft:end_stone_brick_wall"
                "brick" -> "minecraft:brick_wall"
                "andesite" -> "minecraft:andesite_wall"
                "diorite" -> BlockID.DIORITE_WALL
                else -> "minecraft:cobblestone_wall"
            }
        }

        context.addUpdater(1, 21, 30)
            .match("Name", "minecraft:colored_torch_bp")
            .edit("Name") { helper: CompoundTagEditHelper ->
                val block =
                    helper.rootTag["Block"] as? Map<*, *> ?: return@edit
                val states = block["states"] as? MutableMap<*, *> ?: return@edit

                val bit = states.remove("color_bit")
                val toggled = bit is Byte && bit.toInt() == 1 || bit is Boolean && bit
                helper.rootTag["Name"] =
                    if (toggled) "minecraft:colored_torch_purple" else "minecraft:colored_torch_blue"
            }

        context.addUpdater(1, 21, 30)
            .match("Name", "minecraft:colored_torch_rg")
            .edit("Name") { helper: CompoundTagEditHelper ->
                val block = helper.rootTag["Block"] as? Map<*, *> ?: return@edit
                val states = block["states"] as? MutableMap<*, *> ?: return@edit

                val bit = states.remove("color_bit")
                val toggled = bit is Byte && bit.toInt() == 1 || bit is Boolean && bit
                helper.rootTag["Name"] = if (toggled) "minecraft:colored_torch_red" else "minecraft:colored_torch_green"
            }

        this.addTypeUpdater(
            context, "minecraft:purpur_block", "chisel_type"
        ) { type: String? ->
            when (type) {
                "lines" -> "minecraft:purpur_pillar"
                else -> "minecraft:purpur_block"
            }
        }

        this.addTypeUpdater(
            context, "minecraft:sponge", "sponge_type"
        ) { type: String? ->
            when (type) {
                "wet" -> "minecraft:wet_sponge"
                else -> "minecraft:sponge"
            }
        }

        this.addTypeUpdater(
            context, "minecraft:structure_void", "structure_void_type"
        ) { type: String? ->
            "minecraft:structure_void" // air was removed
        }

        context.addUpdater(1, 21, 30)
            .match("Name", "minecraft:tnt")
            .edit("Name") { helper: CompoundTagEditHelper ->
                val block = helper.rootTag["Block"] as? Map<*, *> ?: return@edit
                val states = block["states"] as? MutableMap<*, *> ?: return@edit

                val allowUnderwater = states.remove("allow_underwater_bit")
                val toggled =
                    allowUnderwater is Byte && allowUnderwater.toInt() == 1 || allowUnderwater is Boolean && allowUnderwater
                helper.rootTag["Name"] = if (toggled) "minecraft:tnt" else "minecraft:underwater_tnt"
            }
    }

    private fun addTypeUpdater(
        context: CompoundTagUpdaterContext,
        identifier: String,
        typeState: String,
        rename: Function<String, String>
    ) {
        context.addUpdater(1, 21, 30)
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
        val INSTANCE: Updater = ItemUpdater_1_21_30()
    }
}
