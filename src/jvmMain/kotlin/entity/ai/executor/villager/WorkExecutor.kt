package org.chorus_oss.chorus.entity.ai.executor.villager

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockCrops
import org.chorus_oss.chorus.entity.ai.executor.NearbyFlatRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.data.profession.Profession
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.villagers.EntityVillagerV2
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.Vector2
import org.chorus_oss.chorus.math.Vector3
import java.util.*
import java.util.function.Consumer


class WorkExecutor : NearbyFlatRandomRoamExecutor(CoreMemoryTypes.SITE_BLOCK, 0.3f, 16, 60) {
    var stayTick: Int = 0
    var walkTick: Int = 0

    override fun execute(entity: EntityMob): Boolean {
        if (entity is EntityVillagerV2) {
            val site = entity.memoryStorage[CoreMemoryTypes.SITE_BLOCK]!!
            if (stayTick < 100) {
                if (site.position.distance(entity.position) < 1.5f) {
                    setLookTarget(entity, site.position)
                    stayTick++
                    if (stayTick == 40 || stayTick == 90) entity.level!!.addSound(
                        entity.position, Profession.Companion.getProfession(
                            entity.profession
                        )!!
                            .getWorkSound()
                    )
                }
                if (stayTick == 99) removeRouteTarget(entity)
            } else {
                walkTick++
                run switch@{
                    when (entity.profession) {
                        1 -> {
                            run {
                                if (walkTick % 10 == 0) {
                                    entity.setMovementSpeedF(0.3f)
                                    var minDistance = Float.MAX_VALUE.toDouble()
                                    var nearest: Block? = null
                                    for (block in Arrays.stream<Block>(
                                        entity.level!!.getCollisionBlocks(
                                            entity.getBoundingBox().grow(9.0, 2.0, 9.0), false, true
                                        )
                                    ).filter { block: Block -> block is BlockCrops && block.isFullyGrown }.toList()) {
                                        val distance = block.position.distance(entity.position)
                                        if (distance < minDistance) {
                                            minDistance = distance
                                            nearest = block
                                        }
                                    }
                                    if (nearest != null) {
                                        if (minDistance < 1.5f) {
                                            entity.level!!.breakBlock(nearest)
                                            entity.inventory.addItem(*nearest.getDrops(Item.AIR))
                                            entity.level!!.setBlock(
                                                nearest.position,
                                                nearest.properties.defaultState.toBlock()
                                            )
                                            removeLookTarget(entity)
                                        } else {
                                            if (entity.moveTarget == null) {
                                                var horizontal = Vector2(
                                                    nearest.position.x - entity.position.x,
                                                    nearest.position.z - entity.position.z
                                                )
                                                horizontal = horizontal.multiply(1 - 1 / horizontal.length())
                                                val target = Vector3(
                                                    entity.position.x + horizontal.x,
                                                    nearest.position.y,
                                                    entity.position.z + horizontal.y
                                                )
                                                setLookTarget(entity, target)
                                                setRouteTarget(entity, target)
                                            }
                                        }
                                        return@switch
                                    } else super.execute(entity)
                                } else return@switch
                            }
                            super.execute(entity)
                        }

                        else -> super.execute(entity)
                    }
                }
            }
            if (walkTick >= 300) {
                setTarget(entity)
                walkTick = 0
                stayTick = 0
            }
        }
        return true
    }

    fun setTarget(entity: EntityMob) {
        val site = entity.memoryStorage.get(CoreMemoryTypes.SITE_BLOCK)!!
        var horizontal = Vector2(site.position.x - entity.position.x, site.position.z - entity.position.z)
        horizontal = horizontal.multiply(1 - 1 / horizontal.length())
        val target = Vector3(entity.position.x + horizontal.x, site.position.y, entity.position.z + horizontal.y)
        setLookTarget(entity, target)
        setRouteTarget(entity, target)
    }

    override fun onStart(entity: EntityMob) {
        if (entity is EntityVillagerV2) {
            val shift = getShiftLength(entity.level!!.dayTime)
            if (entity.memoryStorage[CoreMemoryTypes.LAST_REFILL_SHIFT] != shift) {
                this.stayTick = 100
                this.walkTick = 200
                entity.getRecipes().all.forEach(Consumer { it!!.putInt("uses", 0) })
                entity.memoryStorage.set(CoreMemoryTypes.LAST_REFILL_SHIFT, shift)
            }
            if (stayTick < 100) setTarget(entity)
        }
        super.onStart(entity)
    }

    fun getShiftLength(daytime: Int): Int {
        if (daytime >= 0 && daytime < 8000) return 0
        if (daytime >= 10000 && daytime < 11000) return 1
        return -1
    }
}
