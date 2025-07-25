package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.protocol.packets.ActorEventPacket

class InLoveExecutor(protected var duration: Int) : IBehaviorExecutor {
    protected var currentTick: Int = 0

    override fun execute(entity: EntityMob): Boolean {
        if (currentTick == 0) {
            entity.memoryStorage.set(CoreMemoryTypes.LAST_IN_LOVE_TIME, entity.level!!.tick)
            entity.memoryStorage.set(CoreMemoryTypes.IS_IN_LOVE, true)
        }
        currentTick++
        if (currentTick > duration || !entity.memoryStorage[CoreMemoryTypes.IS_IN_LOVE] /*interrupt by other*/) {
            currentTick = 0
            entity.memoryStorage.set(CoreMemoryTypes.IS_IN_LOVE, false)
            return false
        }
        if (currentTick % 10 == 0) {
            sendLoveParticle(entity)
        }
        return true
    }

    override fun onInterrupt(entity: EntityMob) {
        entity.memoryStorage.set(CoreMemoryTypes.IS_IN_LOVE, false)
        currentTick = 0
    }

    protected fun sendLoveParticle(entity: EntityMob) {
        val pk = ActorEventPacket(
            actorRuntimeID = entity.getRuntimeID().toULong(),
            eventType = ActorEventPacket.Companion.Type.LoveParticles,
            eventData = 0
        )
        Server.broadcastPacket(entity.viewers.values, pk)
    }
}
