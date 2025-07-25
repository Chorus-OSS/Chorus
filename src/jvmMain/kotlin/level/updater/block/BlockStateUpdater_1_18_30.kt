package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_18_30 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        this.renameIdentifier(context, "minecraft:concretePowder", "minecraft:concrete_powder")
        this.renameIdentifier(context, "minecraft:frog_egg", "minecraft:frog_spawn")
        this.renameIdentifier(context, "minecraft:invisibleBedrock", "minecraft:invisible_bedrock")
        this.renameIdentifier(context, "minecraft:movingBlock", "minecraft:moving_block")
        this.renameIdentifier(context, "minecraft:pistonArmCollision", "minecraft:piston_arm_collision")
        this.renameIdentifier(context, "minecraft:seaLantern", "minecraft:sea_lantern")
        this.renameIdentifier(context, "minecraft:stickyPistonArmCollision", "minecraft:sticky_piston_arm_collision")
        this.renameIdentifier(context, "minecraft:tripWire", "minecraft:trip_wire")

        this.addPillarAxis(context, "minecraft:ochre_froglight")
        this.addPillarAxis(context, "minecraft:pearlescent_froglight")
        this.addPillarAxis(context, "minecraft:verdant_froglight")
    }

    private fun renameIdentifier(context: CompoundTagUpdaterContext, from: String, to: String) {
        context.addUpdater(1, 18, 10, true) // Here we go again Mojang
            .match("name", from)
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper -> helper.replaceWith("name", to) }
    }

    private fun addPillarAxis(context: CompoundTagUpdaterContext, from: String) {
        context.addUpdater(1, 18, 10, true) // Here we go again Mojang
            .match("name", from)
            .visit("states")
            .tryAdd("pillar_axis", "y")
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_18_30()
    }
}
