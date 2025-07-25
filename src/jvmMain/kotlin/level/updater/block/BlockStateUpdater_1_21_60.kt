package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.OrderedUpdater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_21_60 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        this.addDirectionUpdater(ctx, "minecraft:acacia_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:acacia_fence_gate", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:bamboo_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:bamboo_fence_gate", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:birch_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:birch_fence_gate", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:cherry_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:cherry_fence_gate", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:copper_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:crimson_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:crimson_fence_gate", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:dark_oak_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:dark_oak_fence_gate", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:exposed_copper_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:fence_gate", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:iron_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:jungle_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:jungle_fence_gate", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:mangrove_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:mangrove_fence_gate", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:oxidized_copper_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:pale_oak_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:pale_oak_fence_gate", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:spruce_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:spruce_fence_gate", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:warped_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:warped_fence_gate", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:waxed_copper_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(
            ctx,
            "minecraft:waxed_exposed_copper_door",
            OrderedUpdater.Companion.DIRECTION_TO_CARDINAL
        )
        this.addDirectionUpdater(
            ctx,
            "minecraft:waxed_oxidized_copper_door",
            OrderedUpdater.Companion.DIRECTION_TO_CARDINAL
        )
        this.addDirectionUpdater(
            ctx,
            "minecraft:waxed_weathered_copper_door",
            OrderedUpdater.Companion.DIRECTION_TO_CARDINAL
        )
        this.addDirectionUpdater(ctx, "minecraft:weathered_copper_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:wooden_door", OrderedUpdater.Companion.DIRECTION_TO_CARDINAL)
        ctx.addUpdater(1, 21, 60)
            .match("name", "minecraft:creaking_heart")
            .edit("states") { helper: CompoundTagEditHelper ->
                val states = helper.compoundTag
                val bit = states.remove("active")
                val active =
                    bit is Byte && bit.toInt() == 1 || bit is Boolean && bit
                helper.replaceWith("creaking_heart_state", if (active) "awake" else "uprooted")
            }
    }

    private fun addDirectionUpdater(ctx: CompoundTagUpdaterContext, identifier: String, updater: OrderedUpdater) {
        ctx.addUpdater(1, 21, 60)
            .match("name", identifier)
            .visit("states")
            .edit(updater.oldProperty) { helper: CompoundTagEditHelper ->
                val value = helper.tag as Int
                helper.replaceWith(updater.newProperty, updater.translate(value))
            }
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_21_60()
    }
}