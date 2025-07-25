package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.entity.mob.EntityIronGolem.Companion.checkAndSpawnGolem
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace

class BlockCarvedPumpkin @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPumpkin(blockstate) {
    override val name: String
        get() = "Carved Pumpkin"

    override fun canBeActivated(): Boolean {
        return false
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        return false
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
        if (!super.place(item, block, target, face, fx, fy, fz, player)) return false
        checkAndSpawnGolem(this, player)
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CARVED_PUMPKIN, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION)
    }
}
