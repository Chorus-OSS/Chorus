package org.chorus_oss.chorus.level.updater.item

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext
import java.util.function.Function

class ItemUpdater_1_21_40 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        // Skull blocks was now split into individual blocks
        // however, skull type was determined by block entity or item data, and we do not have that information here
        context.addUpdater(1, 21, 40)
            .match("Name", "minecraft:skull")
            .edit(
                "Name"
            ) { helper: CompoundTagEditHelper ->
                helper.replaceWith("Name", "minecraft:skeleton_skull")
            }
        // these are not vanilla updaters
        // but use this one to bump the version to 18163713 as that's what vanilla does
        context.addUpdater(1, 21, 40)
            .match("Name", "minecraft:cherry_wood")
            .visit("Block")
            .visit("states")
            .remove("stripped_bit")
        context.addUpdater(1, 21, 40, false, false)
            .match("Name", "minecraft:mangrove_wood")
            .visit("Block")
            .visit("states")
            .remove("stripped_bit")
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
        val INSTANCE: Updater = ItemUpdater_1_21_40()
    }
}
