package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.animal.EntitySheep
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.particle.DestroyBlockParticle
import org.chorus_oss.protocol.packets.ActorEventPacket

class EatGrassExecutor(protected var duration: Int) : IBehaviorExecutor {
    protected var currentTick: Int = 0

    override fun execute(entity: EntityMob): Boolean {
        if (currentTick == 0) {
            playEatGrassAnimation(entity)
        }
        currentTick++
        if (currentTick > duration) {
            currentTick = 0
            entity.level!!.addParticle(DestroyBlockParticle(entity.position, Block.get(BlockID.TALL_GRASS)))
            if (entity.level!!.gameRules.getBoolean(GameRule.MOB_GRIEFING)) {
                if (entity.transform.levelBlock.id == BlockID.TALL_GRASS) {
                    entity.level!!.setBlock(entity.position, Block.get(BlockID.AIR))
                } else {
                    entity.level!!.setBlock(entity.position.add(0.0, -1.0, 0.0), Block.get(BlockID.DIRT))
                }
            }
            if (entity is EntitySheep) {
                if (entity.isSheared()) {
                    entity.growWool()
                    return false
                }
                if (entity.isBaby())  // TODO: Accelerated growth instead of instant growth
                    entity.setBaby(false)
            }
            return false
        }
        return true
    }

    override fun onInterrupt(entity: EntityMob) {
        currentTick = 0
    }

    protected fun playEatGrassAnimation(entity: EntityMob) {
        val pk = ActorEventPacket(
            actorRuntimeID = entity.getRuntimeID().toULong(),
            eventType = ActorEventPacket.Companion.Type.EatGrassAnimation,
            eventData = 0
        )
        Server.broadcastPacket(entity.viewers.values, pk)
    }
}
