package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.protocol.packets.ActorEventPacket
import java.util.concurrent.ThreadLocalRandom

class BreezeJumpExecutor : EntityControl, IBehaviorExecutor {
    private var prepareTick: Long = -1

    override fun execute(entity: EntityMob): Boolean {
        val tick = entity.level!!.tick.toLong()
        if (tick % 80 == 0L) {
            startSequence(entity)
            prepareTick = tick
        } else {
            if (prepareTick != -1L) {
                if (tick % 10 == 0L) {
                    prepareTick = -1
                    stopSequence(entity)
                }
            }
        }
        return true
    }

    override fun onStop(entity: EntityMob) {
        entity.setMovementSpeedF(EntityLiving.DEFAULT_SPEED)
        entity.isEnablePitch = (false)
        stopSequence(entity)
    }

    override fun onInterrupt(entity: EntityMob) {
        entity.setMovementSpeedF(EntityLiving.DEFAULT_SPEED)
        entity.isEnablePitch = (false)
        stopSequence(entity)
    }


    private fun startSequence(entity: Entity) {
        entity.setDataFlag(EntityFlag.JUMP_GOAL_JUMP)
    }

    private fun stopSequence(entity: Entity) {
        val random = ThreadLocalRandom.current()
        val motion = entity.getDirectionVector()
        motion.y = 0.6 + random.nextDouble(0.5)
        entity.setMotion(motion)
        entity.setDataFlag(EntityFlag.JUMP_GOAL_JUMP, false)
        val pk = ActorEventPacket(
            actorRuntimeID = entity.getRuntimeID().toULong(),
            eventType = ActorEventPacket.Companion.Type.DustParticles,
            eventData = 0
        )
        Server.broadcastPacket(entity.viewers.values, pk)
    }
}
