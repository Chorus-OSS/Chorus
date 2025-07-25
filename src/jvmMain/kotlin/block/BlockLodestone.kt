package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntityLodestone
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemLodestoneCompass
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.utils.Loggable
import java.io.IOException

class BlockLodestone(blockState: BlockState = properties.defaultState) : BlockSolid(blockState),
    BlockEntityHolder<BlockEntityLodestone> {
    override fun getBlockEntityClass() = BlockEntityLodestone::class.java

    override fun getBlockEntityType(): String {
        return BlockEntityID.LODESTONE
    }

    override fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        return BlockEntityHolder.setBlockAndCreateEntity(this) != null
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (player == null || item.isNothing || item.id != ItemID.COMPASS && item.id != ItemID.LODESTONE_COMPASS) {
            return false
        }

        val compass = Item.get(ItemID.LODESTONE_COMPASS) as ItemLodestoneCompass
        if (item.hasCompoundTag()) {
            compass.setCompoundTag(item.compoundTag.clone())
        }

        val trackingHandle: Int
        try {
            trackingHandle = getOrCreateBlockEntity().requestTrackingHandler()
            compass.trackingHandle = trackingHandle
        } catch (e: Exception) {
            BlockLodestone.log.warn(
                "Could not create a lodestone compass to {} for {}",
                locator,
                player.getEntityName(),
                e
            )
            return false
        }

        var added = true
        if (item.getCount() == 1) {
            player.inventory.setItemInHand(compass)
        } else {
            val clone: Item = item.clone()
            clone.count--
            player.inventory.setItemInHand(clone)
            for (failed in player.inventory.addItem(compass)) {
                added = false
                player.level!!.dropItem(player.locator.position, failed)
            }
        }

        level.addSound(player.locator.position, Sound.LODESTONE_COMPASS_LINK_COMPASS_TO_LODESTONE)

        if (added) {
            try {
                Server.instance.getPositionTrackingService().startTracking(player, trackingHandle, false)
            } catch (e: IOException) {
                BlockLodestone.log.warn(
                    "Failed to make the player {} track {} at {}",
                    player.getEntityName(),
                    trackingHandle,
                    locator,
                    e
                )
            }
            level.scheduler.scheduleTask(null, player::updateTrackingPositions)
        }

        return true
    }

    override val name: String
        get() = "Lodestone"

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 3.5

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    override fun canBePushed(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object : Loggable {
        val properties: BlockProperties = BlockProperties(BlockID.LODESTONE)
    }
}
