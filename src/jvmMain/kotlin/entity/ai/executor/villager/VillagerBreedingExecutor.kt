package org.chorus_oss.chorus.entity.ai.executor.villager

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockBed
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.ai.executor.EntityBreedingExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.villagers.EntityVillagerV2
import org.chorus_oss.protocol.packets.ActorEventPacket

class VillagerBreedingExecutor(
    entityClass: Class<out EntityMob>,
    findingRangeSquared: Int,
    duration: Int,
    moveSpeed: Float
) :
    EntityBreedingExecutor<EntityMob>(entityClass, findingRangeSquared, duration, moveSpeed) {
    override fun onStart(entity: EntityMob) {
        super.onStart(entity)
        finded = true
        another = entity.memoryStorage.get(CoreMemoryTypes.ENTITY_SPOUSE) as EntityMob
    }

    override fun onStop(entity: EntityMob) {
        clearData(entity)
        if (another != null) {
            clearData(another!!)
        }
    }

    override fun execute(uncasted: EntityMob): Boolean {
        if (another == null) return false
        return super.execute(uncasted)
    }

    override fun bear(entity: EntityMob) {
        val range = 48
        val lookY = 5
        var block: BlockBed? = null
        for (x in -range..range) {
            for (z in -range..range) {
                for (y in -lookY..lookY) {
                    val lookTransform = entity.transform.add(x.toDouble(), y.toDouble(), z.toDouble())
                    val lookBlock = lookTransform.levelBlock
                    if (lookBlock is BlockBed) {
                        if (!lookBlock.isHeadPiece && (entity.level!!.entities).values
                                .none { entity1: Entity ->
                                    entity1 is EntityVillagerV2 && entity1.memoryStorage
                                        .notEmpty(CoreMemoryTypes.OCCUPIED_BED) && entity1.getBed() == lookBlock
                                }
                        ) {
                            block = lookBlock.footPart
                        }
                    }
                }
            }
        }
        if (block == null) {
            sendAngryParticles(entity)
            sendAngryParticles(another!!)
            return
        } else {
            sendInLoveParticles(entity)
            sendInLoveParticles(another!!)
        }

        val baby = Entity.Companion.createEntity(entity.getNetworkID(), entity.locator) as EntityVillagerV2
        baby.setBaby(true)
        //防止小屁孩去生baby
        baby.memoryStorage.set(CoreMemoryTypes.LAST_IN_LOVE_TIME, entity.level!!.tick)
        baby.memoryStorage.set(CoreMemoryTypes.PARENT, entity)
        baby.spawnToAll()
    }

    override fun clearData(entity: EntityMob) {
        entity.memoryStorage.clear(CoreMemoryTypes.ENTITY_SPOUSE)
        //clear move target
        entity.moveTarget = null
        //clear look target
        entity.lookTarget = null
        //reset move speed
        entity.setMovementSpeedF(0.1f)
        //interrupt in love status
        entity.memoryStorage.set(CoreMemoryTypes.WILLING, false)
        entity.memoryStorage.set(CoreMemoryTypes.LAST_IN_LOVE_TIME, entity.level!!.tick)
    }

    protected fun sendInLoveParticles(entity: EntityMob) {
        val pk = ActorEventPacket(
            actorRuntimeID = entity.getRuntimeID().toULong(),
            eventType = ActorEventPacket.Companion.Type.LoveParticles,
            eventData = 0,
        )
        Server.broadcastPacket(entity.viewers.values, pk)
    }

    protected fun sendAngryParticles(entity: EntityMob) {
        val pk = ActorEventPacket(
            actorRuntimeID = entity.getRuntimeID().toULong(),
            eventType = ActorEventPacket.Companion.Type.VillagerAngry,
            eventData = 0,
        )
        Server.broadcastPacket(entity.viewers.values, pk)
    }
}
