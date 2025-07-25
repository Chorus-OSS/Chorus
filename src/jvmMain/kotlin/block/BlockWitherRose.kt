package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace

class BlockWitherRose @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlower(blockstate) {
    override fun canPlantOn(block: Block): Boolean {
        return super.canPlantOn(block) || block.id == BlockID.NETHERRACK || block.id == BlockID.SOUL_SAND
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

    override fun onEntityCollide(entity: Entity) {
        if (Server.instance.getDifficulty() != 0 && entity is EntityLiving) {
            if (!entity.invulnerable && !entity.hasEffect(EffectType.WITHER) && (entity !is Player || !entity.isCreative && !entity.isSpectator)) {
                val effect = Effect.get(EffectType.WITHER)
                effect.setDuration(40)
                effect.setAmplifier(1)
                entity.addEffect(effect)
            }
        }
    }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB {
        return this
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WITHER_ROSE)
    }
}
