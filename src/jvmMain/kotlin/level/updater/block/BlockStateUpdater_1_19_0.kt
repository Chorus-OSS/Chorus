package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_19_0 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        this.renameIdentifier(context, "minecraft:stone_slab", "minecraft:stone_block_slab")
        this.renameIdentifier(context, "minecraft:stone_slab2", "minecraft:stone_block_slab2")
        this.renameIdentifier(context, "minecraft:stone_slab3", "minecraft:stone_block_slab3")
        this.renameIdentifier(context, "minecraft:stone_slab4", "minecraft:stone_block_slab4")
        this.renameIdentifier(context, "minecraft:double_stone_slab", "minecraft:double_stone_block_slab")
        this.renameIdentifier(context, "minecraft:double_stone_slab2", "minecraft:double_stone_block_slab2")
        this.renameIdentifier(context, "minecraft:double_stone_slab3", "minecraft:double_stone_block_slab3")
        this.renameIdentifier(context, "minecraft:double_stone_slab4", "minecraft:double_stone_block_slab4")

        this.addProperty(context, "minecraft:sculk_shrieker", "can_summon", 0.toByte())
    }

    private fun renameIdentifier(context: CompoundTagUpdaterContext, from: String, to: String) {
        context.addUpdater(1, 18, 10, true) // Here we go again Mojang
            .match("name", from)
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper -> helper.replaceWith("name", to) }
    }

    private fun addProperty(context: CompoundTagUpdaterContext, identifier: String, propertyName: String, value: Any) {
        context.addUpdater(1, 18, 10, true) // Here we go again Mojang
            .match("name", identifier)
            .visit("states")
            .tryAdd(propertyName, value)
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_19_0()
    }
}
