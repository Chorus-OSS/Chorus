package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.Damage
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.event.entity.EntityBlockChangeEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.DestroyBlockParticle
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.packets.LevelEventPacket
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class EntityFallingBlock(chunk: IChunk?, nbt: CompoundTag?) : Entity(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.FALLING_BLOCK
    }

    protected var blockState: BlockState? = null
    protected var breakOnLava: Boolean = false
    protected var breakOnGround: Boolean = false
    protected var aliveTick: Int = 0

    override fun getWidth(): Float {
        return 0.98f
    }

    override fun getLength(): Float {
        return 0.98f
    }

    override fun getHeight(): Float {
        return 0.98f
    }

    override fun getGravity(): Float {
        return 0.04f
    }

    override fun getDrag(): Float {
        return 0.02f
    }

    override fun getBaseOffset(): Float {
        return 0.49f
    }

    override fun canCollide(): Boolean {
        return blockState!!.identifier == BlockID.ANVIL
    }

    override fun initEntity() {
        super.initEntity()

        if (namedTag.contains("Block")) {
            val blockState: BlockState? = NBTIO.getBlockStateHelper(namedTag.getCompound("Block"))
            if (blockState == null) {
                close()
                return
            } else this.blockState = blockState
        }

        breakOnLava = namedTag.getBoolean("BreakOnLava")
        breakOnGround = namedTag.getBoolean("BreakOnGround")
        this.fireProof = true
        this.setDataFlag(EntityFlag.FIRE_IMMUNE, true)

        setDataProperty(EntityDataTypes.VARIANT, blockState!!.blockStateHash())
    }

    override fun canCollideWith(entity: Entity): Boolean {
        return blockState!!.identifier == BlockID.ANVIL
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        return source.cause == DamageCause.VOID && super.attack(source)
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (closed) {
            return false
        }

        val tickDiff: Int = currentTick - lastUpdate
        if (tickDiff <= 0 && !justCreated) {
            return true
        }

        lastUpdate = currentTick

        var hasUpdate: Boolean = entityBaseTick(tickDiff)

        if (isAlive()) {
            val b: String = blockState!!.identifier
            if ((b == BlockID.SAND ||
                        b == BlockID.GRAVEL ||
                        b == BlockID.ANVIL
                        )
            ) {
                aliveTick++
                if (aliveTick > sandAliveTick) {
                    aliveTick = 0
                    level!!.addParticle(DestroyBlockParticle(this.position, blockState!!.toBlock()))
                    this.close()
                    dropItems()
                }
            }

            motion.y -= getGravity().toDouble()

            move(motion.x, motion.y, motion.z)

            val friction: Float = 1 - getDrag()

            motion.x *= friction.toDouble()
            motion.y *= (1 - getDrag()).toDouble()
            motion.z *= friction.toDouble()

            val pos: Vector3 = (Vector3(
                position.x - 0.5,
                position.y, position.z - 0.5
            )).round()
            if (breakOnLava && level!!.getBlock(pos.subtract(0.0, 1.0, 0.0)) is BlockFlowingLava) {
                close()
                if (level!!.gameRules.getBoolean(GameRule.DO_ENTITY_DROPS)) {
                    dropItems()
                }
                level!!.addParticle(DestroyBlockParticle(pos, Block.get(blockState)))
                return true
            }

            if (onGround) {
                close()
                val block: Block = level!!.getBlock(pos)

                val floorPos: Vector3 = (Vector3(
                    position.x - 0.5,
                    position.y, position.z - 0.5
                )).floor()
                val floorBlock: Block = level!!.getBlock(floorPos)
                //handle for snow stack
                if (getBlock().id == BlockID.SNOW_LAYER && floorBlock.id == BlockID.SNOW_LAYER && floorBlock.getPropertyValue(
                        CommonBlockProperties.HEIGHT
                    ) != 7
                ) {
                    val mergedHeight: Int =
                        floorBlock.getPropertyValue(CommonBlockProperties.HEIGHT) + 1 + blockState!!.getPropertyValue(
                            CommonBlockProperties.HEIGHT
                        ) + 1
                    if (mergedHeight > 8) {
                        val event: EntityBlockChangeEvent = EntityBlockChangeEvent(
                            this,
                            floorBlock,
                            Block.get(BlockID.SNOW_LAYER).setPropertyValue(CommonBlockProperties.HEIGHT, 7)
                        )
                        Server.instance.pluginManager.callEvent(event)
                        if (!event.cancelled) {
                            level!!.setBlock(floorPos, event.to, true)

                            val abovePos: Vector3 = floorPos.up()
                            val aboveBlock: Block = level!!.getBlock(abovePos)
                            if (aboveBlock.isAir) {
                                val event2 = EntityBlockChangeEvent(
                                    this, aboveBlock, Block.get(
                                        BlockID.SNOW_LAYER
                                    ).setPropertyValue(CommonBlockProperties.HEIGHT, mergedHeight - 8 - 1)
                                )
                                Server.instance.pluginManager.callEvent(event2)
                                if (!event2.cancelled) {
                                    level!!.setBlock(abovePos, event2.to, true)
                                }
                            }
                        }
                    } else {
                        val event = EntityBlockChangeEvent(
                            this, floorBlock,
                            Block.get(BlockID.SNOW_LAYER)
                                .setPropertyValue(CommonBlockProperties.HEIGHT, mergedHeight - 1)
                        )
                        Server.instance.pluginManager.callEvent(event)
                        if (!event.cancelled) {
                            level!!.setBlock(floorPos, event.to, true)
                        }
                    }
                } else if (!block.isAir && block.isTransparent && !block.canBeReplaced() || getBlock().id == BlockID.SNOW_LAYER && block is BlockLiquid) {
                    if (if (getBlock().id != BlockID.SNOW_LAYER) level!!.gameRules
                            .getBoolean(GameRule.DO_ENTITY_DROPS) else level!!.gameRules
                            .getBoolean(GameRule.DO_TILE_DROPS)
                    ) {
                        dropItems()
                    }
                } else {
                    val event: EntityBlockChangeEvent = EntityBlockChangeEvent(this, block, Block.get(blockState))
                    Server.instance.pluginManager.callEvent(event)
                    if (!event.cancelled) {
                        val eventTo: Block = event.to

                        if (breakOnGround) {
                            if (level!!.gameRules.getBoolean(GameRule.DO_ENTITY_DROPS)) {
                                dropItems()
                            }
                            level!!.addParticle(DestroyBlockParticle(pos, Block.get(blockState)))
                        } else {
                            while (pos.y < level!!.maxHeight) {
                                if (!level!!.getBlock(pos).isAir) pos.y++ else break
                            }
                            level!!.setBlock(pos, eventTo, true)
                        }

                        if (eventTo.id == BlockID.ANVIL) {
                            val e = level!!.getCollidingEntities(
                                this.getBoundingBox(),
                                this
                            )
                            for (entity in e) {
                                if (entity is EntityLiving && fallDistance > 0) {
                                    entity.attack(
                                        EntityDamageByBlockEvent(
                                            eventTo,
                                            entity,
                                            DamageCause.FALLING_BLOCK,
                                            min(40.0, max(0.0, (fallDistance * 2f).toDouble())).toFloat()
                                        )
                                    )
                                }
                            }

                            //handle anvil broken when fall
                            if (fallDistance > 8) {
                                val anvil: BlockAnvil = eventTo as BlockAnvil
                                if (anvil.anvilDamage == Damage.VERY_DAMAGED) {
                                    level!!.setBlock(eventTo.position, BlockAir.STATE.toBlock(), true)
                                } else {
                                    anvil.anvilDamage = (anvil.anvilDamage.next())
                                    level!!.setBlock(eventTo.position, anvil, true)
                                }
                            }
                            level!!.addLevelEvent(eventTo.position, LevelEventPacket.SOUND_ANVIL_LAND)
                        } else if (eventTo.id == BlockID.POINTED_DRIPSTONE) {
                            level!!.addLevelEvent(block.position, LevelEventPacket.SOUND_POINTED_DRIPSTONE_LAND)

                            val e = level!!.getCollidingEntities(SimpleAxisAlignedBB(pos, pos.add(1.0, 1.0, 1.0)))
                            for (entity in e) {
                                if (entity is EntityLiving && fallDistance > 0) {
                                    entity.attack(
                                        EntityDamageByBlockEvent(
                                            eventTo,
                                            entity,
                                            DamageCause.FALLING_BLOCK,
                                            min(40.0, max(0.0, (fallDistance * 2f).toDouble())).toFloat()
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                hasUpdate = true
            }

            updateMovement()
        }

        return hasUpdate || !onGround || abs(motion.x) > 0.00001 || abs(
            motion.y
        ) > 0.00001 || abs(motion.z) > 0.00001
    }

    fun getBlock(): Block {
        return blockState!!.toBlock()
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag!!.putCompound("Block", blockState!!.blockStateTag)
    }

    override fun canBeMovedByCurrents(): Boolean {
        return !onGround && blockState!!.identifier != BlockID.DRIPSTONE_BLOCK
    }

    override fun resetFallDistance() {
        if (!this.closed) { // For falling anvil: do not reset fall distance before dealing damage to entities
            this.highestPosition = position.y
        }
    }

    private fun dropItems() {
        val block: Block = this.getBlock()
        level!!.dropItem(this.position, if (block is BlockFallable) block.toFallingItem() else block.toItem())
    }

    override fun getOriginalName(): String {
        return "Falling Block"
    }

    companion object {
        private const val sandAliveTick: Int = 600
    }
}
