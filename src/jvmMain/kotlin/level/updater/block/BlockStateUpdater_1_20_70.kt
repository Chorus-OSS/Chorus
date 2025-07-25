package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext
import java.util.function.Function

class BlockStateUpdater_1_20_70 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        this.addTypeUpdater(
            ctx, "minecraft:double_wooden_slab", "wood_type"
        ) { type: String? -> "minecraft:" + type + "_double_slab" }
        this.addTypeUpdater(
            ctx, "minecraft:leaves", "old_leaf_type"
        ) { type: String? -> "minecraft:" + type + "_leaves" }
        this.addTypeUpdater(
            ctx, "minecraft:leaves2", "new_leaf_type"
        ) { type: String? -> "minecraft:" + type + "_leaves" }
        this.addTypeUpdater(
            ctx, "minecraft:wooden_slab", "wood_type"
        ) { type: String? -> "minecraft:" + type + "_slab" }

        ctx.addUpdater(1, 20, 70)
            .match("name", "minecraft:wood")
            .edit("states") { helper: CompoundTagEditHelper ->
                val states = helper.compoundTag
                val bit = states.remove("stripped_bit")
                val toggles =
                    bit is Byte && bit.toInt() == 1 || bit is Boolean && bit

                val type = states.remove("wood_type") as String?
                helper.rootTag["name"] =
                    if (toggles) "minecraft:stripped_" + type + "_wood" else "minecraft:" + type + "_wood"
            }

        // Vanilla does not use updater for this block for some reason
        ctx.addUpdater(1, 20, 70, false)
            .match("name", "minecraft:grass")
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper -> helper.replaceWith("name", "minecraft:grass_block") }
    }

    private fun addTypeUpdater(
        context: CompoundTagUpdaterContext,
        identifier: String,
        typeState: String,
        rename: Function<String?, String>
    ) {
        context.addUpdater(1, 20, 70)
            .match("name", identifier)
            .visit("states")
            .edit(
                typeState
            ) { helper: CompoundTagEditHelper -> helper.rootTag["name"] = rename.apply(helper.tag as String) }
            .remove(typeState)
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_20_70()
    }
}
