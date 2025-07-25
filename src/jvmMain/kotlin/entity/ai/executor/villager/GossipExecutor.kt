package org.chorus_oss.chorus.entity.ai.executor.villager

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.ai.executor.EntityControl
import org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.villagers.EntityVillagerV2
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Utils

class GossipExecutor(
    val type: NullableMemoryType<out EntityVillagerV2>
) : EntityControl, IBehaviorExecutor {
    protected var tick: Int = 0
    protected var spread: Boolean = false


    override fun execute(entity: EntityMob): Boolean {
        val entity1 = entity.memoryStorage[type]
        if (entity1 != null) {
            if (entity1.position.toHorizontal().distance(entity.position.toHorizontal()) < 2) {
                if (!spread) {
                    removeRouteTarget(entity)
                    if (entity is EntityVillagerV2) {
                        entity.spreadGossip()
                        spread = true
                    }
                }
                if (entity is EntityVillagerV2) {
                    if (entity1.isHungry() && entity.shouldShareFood()) {
                        for (i in 0..<entity.inventory.size) {
                            var item = entity.inventory.getUnclonedItem(i)
                            item.setCount(item.getCount() / 2)
                            if (item.id === BlockID.WHEAT) item = Item.get(BlockID.WHEAT, 0, item.getCount() / 3)
                            entity.level!!.dropItem(
                                entity.locator.position.add(
                                    0.0,
                                    entity.getEyeHeight().toDouble(),
                                    0.0
                                ),
                                item,
                                Vector3(
                                    entity1.position.x - entity.position.x,
                                    entity1.position.y - entity.position.y,
                                    entity1.position.z - entity.position.z
                                ).normalize().multiply(0.4)
                            )
                        }
                    }
                }
            }
            if (tick % 100 == 0) {
                if (Utils.rand(0, 10) == 0) {
                    entity.level!!.getCollidingEntities(
                        entity.getBoundingBox().grow(2.0, 0.0, 2.0)
                    ).filter { entity2: Entity -> entity2 is EntityVillagerV2 && entity2 !== entity }
                        .map { entity2: Entity -> (entity2 as EntityVillagerV2) }.forEach { entity2: EntityVillagerV2 ->
                            entity2.lookTarget =
                                entity.position
                        }
                }
            }
        }
        return true
    }

    override fun onStart(entity: EntityMob) {
        entity.moveTarget = entity.memoryStorage[type]?.position
        entity.lookTarget = entity.memoryStorage[type]?.position
        this.tick = 0
        this.spread = false
        entity.memoryStorage.set(CoreMemoryTypes.LAST_GOSSIP, entity.level!!.tick)
    }

    override fun onStop(entity: EntityMob) {
        entity.memoryStorage.clear(type)
    }

    override fun onInterrupt(entity: EntityMob) {
        onStop(entity)
    }

    companion object {
        private const val STAY_TICKS = 60
    }
}
