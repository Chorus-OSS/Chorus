package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.entity.item.EntityFallingBlock
import org.chorus_oss.chorus.event.player.PlayerInteractEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockSuspiciousGravel @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFallable(blockstate) {
    override val name: String
        get() = "Suspicious Gravel"

    override val hardness: Double
        get() = 0.25

    override val resistance: Double
        get() = 1.25

    override fun createFallingEntity(customNbt: CompoundTag): EntityFallingBlock? {
        customNbt.putBoolean("BreakOnGround", true)
        return super.createFallingEntity(customNbt)
    }

    override fun onTouch(
        vector: Vector3,
        item: Item,
        face: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float,
        player: Player?,
        action: PlayerInteractEvent.Action
    ) {
        val progress = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.BRUSHED_PROGRESS)
        if (progress < 3) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.BRUSHED_PROGRESS, progress + 1)
            level.addSound(this.position, Sound.HIT_SUSPICIOUS_GRAVEL)
            level.setBlock(this.position, this)
        } else {
            level.setBlock(this.position, BlockGravel.properties.defaultState.toBlock())
            level.addSound(this.position, Sound.BREAK_SUSPICIOUS_GRAVEL)
        }
        super.onTouch(vector, item, face, fx, fy, fz, player, action)
    }

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(Item.AIR)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.SUSPICIOUS_GRAVEL,
            CommonBlockProperties.HANGING,
            CommonBlockProperties.BRUSHED_PROGRESS
        )
    }
}