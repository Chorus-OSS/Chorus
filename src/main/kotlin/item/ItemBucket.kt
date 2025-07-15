package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.entity.Entity.Companion.createEntity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityVariant
import org.chorus_oss.chorus.event.player.PlayerBucketEmptyEvent
import org.chorus_oss.chorus.event.player.PlayerBucketFillEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.FLAG_ALL_PRIORITY
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.particle.ExplodeParticle
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import org.chorus_oss.chorus.utils.Identifier


open class ItemBucket : Item {
    @JvmOverloads
    constructor(meta: Int = 0, count: Int = 1) : super(ItemID.Companion.BUCKET, meta, count)

    @JvmOverloads
    constructor(id: String, count: Int = 0) : super(id, 0, count)

    constructor(id: String, count: Int, name: String?) : super(id, 0, count, name)

    override fun internalAdjust() {
        if (this.id == ItemID.Companion.BUCKET) {
            when (damage) {
                1 -> {
                    this.id = ItemID.Companion.MILK_BUCKET
                    this.identifier = Identifier(ItemID.Companion.MILK_BUCKET)
                }

                2 -> {
                    this.id = ItemID.Companion.COD_BUCKET
                    this.identifier = Identifier(ItemID.Companion.COD_BUCKET)
                }

                3 -> {
                    this.id = ItemID.Companion.SALMON_BUCKET
                    this.identifier = Identifier(ItemID.Companion.SALMON_BUCKET)
                }

                4 -> {
                    this.id = ItemID.Companion.TROPICAL_FISH_BUCKET
                    this.identifier = Identifier(ItemID.Companion.TROPICAL_FISH_BUCKET)
                }

                5 -> {
                    this.id = ItemID.Companion.PUFFERFISH_BUCKET
                    this.identifier = Identifier(ItemID.Companion.PUFFERFISH_BUCKET)
                }

                8 -> {
                    this.id = ItemID.Companion.WATER_BUCKET
                    this.identifier = Identifier(ItemID.Companion.WATER_BUCKET)
                }

                10 -> {
                    this.id = ItemID.Companion.LAVA_BUCKET
                    this.identifier = Identifier(ItemID.Companion.LAVA_BUCKET)
                }

                11 -> {
                    this.id = ItemID.Companion.POWDER_SNOW_BUCKET
                    this.identifier = Identifier(ItemID.Companion.POWDER_SNOW_BUCKET)
                }

                12 -> {
                    this.id = ItemID.Companion.AXOLOTL_BUCKET
                    this.identifier = Identifier(ItemID.Companion.AXOLOTL_BUCKET)
                }

                13 -> {
                    this.id = ItemID.Companion.TADPOLE_BUCKET
                    this.identifier = Identifier(ItemID.Companion.TADPOLE_BUCKET)
                }

                else -> {
                    this.id = ItemID.Companion.BUCKET
                    this.identifier = Identifier(ItemID.Companion.BUCKET)
                }
            }
            this.meta = 0
            this.name = null
        }
    }

    val isEmpty: Boolean
        get() = this.id == ItemID.Companion.BUCKET && meta == 0

    val isWater: Boolean
        get() = when (this.id) {
            ItemID.Companion.COD_BUCKET, ItemID.Companion.SALMON_BUCKET, ItemID.Companion.TROPICAL_FISH_BUCKET, ItemID.Companion.PUFFERFISH_BUCKET, ItemID.Companion.WATER_BUCKET, ItemID.Companion.AXOLOTL_BUCKET, ItemID.Companion.TADPOLE_BUCKET -> true
            else -> false
        }

    val isMilk: Boolean
        get() = this.id == ItemID.Companion.MILK_BUCKET

    val isLava: Boolean
        get() = this.id == ItemID.Companion.LAVA_BUCKET

    val isPowderSnow: Boolean
        get() = this.id == ItemID.Companion.POWDER_SNOW_BUCKET

    val fishEntityId: String?
        get() = when (this.id) {
            ItemID.Companion.COD_BUCKET -> EntityID.COD
            ItemID.Companion.SALMON_BUCKET -> EntityID.SALMON
            ItemID.Companion.TROPICAL_FISH_BUCKET -> EntityID.TROPICALFISH
            ItemID.Companion.PUFFERFISH_BUCKET -> EntityID.PUFFERFISH
            ItemID.Companion.AXOLOTL_BUCKET -> EntityID.AXOLOTL
            ItemID.Companion.TADPOLE_BUCKET -> EntityID.TADPOLE
            else -> null
        }

    override val maxStackSize: Int
        get() = if (meta == 0 && this.id == ItemID.Companion.BUCKET) 16 else 1

    override fun canBeActivated(): Boolean {
        return true
    }

    val targetBlock: Block
        /**
         * get the placed block for this bucket
         */
        get() = Block.get(getDamageByTarget(this.id))


    override fun onActivate(
        level: Level,
        player: Player,
        block: Block,
        target: Block,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double
    ): Boolean {
        var target = target
        if (player.isAdventure) {
            return false
        }
        if (player.isItemCoolDownEnd(ItemID.Companion.BUCKET)) {
            player.setItemCoolDown(5, ItemID.Companion.BUCKET)
        } else {
            return false
        }

        val targetBlock = targetBlock

        if (targetBlock is BlockAir) {
            if (target !is BlockPowderSnow) {
                //                                       is liquid and state is
                if (target !is BlockLiquid || !target.isDefaultState) {
                    target = target.getLevelBlockAtLayer(1)
                }
                if (target !is BlockLiquid || !target.isDefaultState) {
                    target = block
                }
                if (target !is BlockLiquid || !target.isDefaultState) {
                    target = block.getLevelBlockAtLayer(1)
                }
            }
            if ((target is BlockLiquid || target is BlockPowderSnow) && target.isDefaultState) {
                val result = if (player.isCreative) {
                    get(ItemID.Companion.BUCKET, 0, 1)
                } else if (target is BlockPowderSnow) {
                    get(ItemID.Companion.BUCKET, 11, 1)
                } else if (target is BlockFlowingLava) {
                    get(ItemID.Companion.BUCKET, 10, 1)
                } else {
                    get(ItemID.Companion.BUCKET, 8, 1)
                }
                val ev: PlayerBucketFillEvent
                Server.instance.pluginManager.callEvent(
                    PlayerBucketFillEvent(
                        player, block, face, target,
                        this, result
                    ).also { ev = it })
                if (!ev.cancelled) {
                    player.level!!.setBlock(target.position, target.layer, Block.get(BlockID.AIR), true, true)

                    level.vibrationManager.callVibrationEvent(
                        VibrationEvent(
                            player,
                            target.position.add(0.5, 0.5, 0.5),
                            VibrationType.FLUID_PICKUP
                        )
                    )

                    // When water is removed ensure any adjacent still water is replaced with water that can flow.
                    for (side in BlockFace.Plane.HORIZONTAL_FACES) {
                        val b = target.getSideAtLayer(0, side)
                        if (b.id == BlockID.WATER) {
                            level.setBlock(b.position, Block.get(BlockID.FLOWING_WATER))
                        } else if (b.id == BlockID.LAVA) level.setBlock(b.position, Block.get(BlockID.FLOWING_LAVA))
                    }

                    if (player.isSurvival) {
                        if (this.getCount() - 1 <= 0) {
                            player.inventory.setItemInHand(ev.item)
                        } else {
                            val clone = this.clone()
                            clone.setCount(this.getCount() - 1)
                            player.inventory.setItemInHand(clone)
                            if (player.inventory.canAddItem(ev.item)) {
                                player.inventory.addItem(ev.item)
                            } else {
                                player.dropItem(ev.item)
                            }
                            player.inventory.sendContents(player)
                        }
                    }

                    if (target is BlockFlowingLava) {
                        level.addSound(block.position, Sound.BUCKET_FILL_LAVA)
                    } else if (target is BlockFlowingWater) {
                        level.addSound(block.position, Sound.BUCKET_FILL_WATER)
                    } else if (target is BlockPowderSnow) {
                        level.addSound(block.position, Sound.BUCKET_FILL_POWDER_SNOW)
                    }

                    return true
                } else {
                    player.inventory.sendContents(player)
                }
            }
        } else if (targetBlock is BlockLiquid) {
            val result = get(ItemID.Companion.BUCKET, 0, 1)
            val usesWaterlogging = targetBlock.usesWaterLogging()
            val placementBlock = if (usesWaterlogging) {
                if (block.id == BlockID.BAMBOO) {
                    block
                } else if (target.waterloggingLevel > 0) {
                    target.getLevelBlockAtLayer(1)
                } else if (block.waterloggingLevel > 0) {
                    block.getLevelBlockAtLayer(1)
                } else {
                    block
                }
            } else {
                block
            }

            val ev = PlayerBucketEmptyEvent(player, placementBlock, face, target, this, result)
            val canBeFlowedInto = placementBlock.canBeFlowedInto() || placementBlock.id == BlockID.BAMBOO
            if (usesWaterlogging) {
                ev.cancelled = placementBlock.waterloggingLevel <= 0 && !canBeFlowedInto
            } else {
                ev.cancelled = !canBeFlowedInto
            }

            var nether = false
            if (!canBeUsedOnDimension(player.level!!.dimension)) {
                ev.cancelled = true
                nether = !isLava
            }

            Server.instance.pluginManager.callEvent(ev)

            if (!ev.cancelled) {
                player.level!!.setBlock(placementBlock.position, placementBlock.layer, targetBlock, true, true)
                player.level!!.sendBlocks(
                    arrayOf(player),
                    arrayOf(target.getLevelBlockAtLayer(1)),
                    org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL_PRIORITY.toInt(),
                    1
                )
                target.level.vibrationManager.callVibrationEvent(
                    VibrationEvent(
                        player,
                        target.position.add(0.5, 0.5, 0.5),
                        VibrationType.FLUID_PLACE
                    )
                )
                updateBucketItem(player, ev)

                afterUse(level, block)

                return true
            } else if (nether) { //handle the logic that the player can't use water bucket in nether
                if (!player.isCreative) {
                    this.damage = 0 // Empty bucket
                    player.inventory.setItemInHand(this)
                }
                player.level!!.addLevelSoundEvent(target.position, LevelSoundEventPacket.SOUND_FIZZ)
                player.level!!.addParticle(ExplodeParticle(target.position.add(0.5, 1.0, 0.5)))
            } else {
                player.level!!.sendBlocks(
                    arrayOf(player),
                    arrayOf(block.getLevelBlockAtLayer(1)),
                    org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL_PRIORITY.toInt(),
                    1
                )
                player.inventory.sendContents(player)
            }
        } else if (targetBlock is BlockPowderSnow) {
            val result = get(ItemID.Companion.BUCKET, 0, 1)
            if (!target.canBeReplaced()) {
                val side = target.getSide(face)
                if (side.canBeReplaced()) {
                    target = side
                }
            }
            val ev = PlayerBucketEmptyEvent(player, targetBlock, face, target, this, result)
            if (!ev.cancelled) {
                target.level.setBlock(target.position, targetBlock, true, true)
                player.level!!.addSound(target.position, Sound.BUCKET_FILL_POWDER_SNOW)

                updateBucketItem(player, ev)

                target.level.vibrationManager.callVibrationEvent(
                    VibrationEvent(
                        player,
                        target.position.add(0.5, 0.5, 0.5),
                        VibrationType.BLOCK_PLACE
                    )
                )
            }
        }

        return true
    }

    /**
     * update the count of bucket and set to inventory
     */
    private fun updateBucketItem(player: Player, ev: PlayerBucketEmptyEvent) {
        if (player.isSurvival) {
            if (this.getCount() - 1 <= 0) {
                player.inventory.setItemInHand(ev.item)
            } else {
                val clone = this.clone()
                clone.setCount(this.getCount() - 1)
                player.inventory.setItemInHand(clone)
                if (player.inventory.canAddItem(ev.item)) {
                    player.inventory.addItem(ev.item)
                } else {
                    player.dropItem(ev.item)
                }
            }
        }
    }

    /**
     * whether the bucket can be used in the dimension
     */
    protected fun canBeUsedOnDimension(dimension: Int): Boolean {
        return dimension != Level.DIMENSION_NETHER || (isEmpty || isLava || isMilk)
    }

    /**
     * handle logic after use bucket.
     */
    protected fun afterUse(level: Level, block: Block) {
        if (isLava) {
            level.addSound(block.position, Sound.BUCKET_EMPTY_LAVA)
        } else {
            level.addSound(block.position, Sound.BUCKET_EMPTY_WATER)
        }

        spawnFishEntity(block.add(0.5, 0.5, 0.5))
    }

    fun spawnFishEntity(spawnPos: Locator) {
        val fishEntityId = fishEntityId
        if (fishEntityId != null) {
            val fishEntity = createEntity(fishEntityId, spawnPos)
            if (fishEntity != null) if (fishEntity is EntityVariant) {
                if (namedTag != null) {
                    if (namedTag!!.containsInt("Variant")) {
                        fishEntity.variant = (namedTag!!.getInt("Variant"))
                    }
                }
            }
            fishEntity!!.spawnToAll()
        }
    }

    override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
        return this.id == ItemID.Companion.BUCKET && isMilk // Milk
    }

    companion object {
        fun getDamageByTarget(target: String): String {
            return when (target) {
                ItemID.Companion.COD_BUCKET, ItemID.Companion.SALMON_BUCKET, ItemID.Companion.TROPICAL_FISH_BUCKET, ItemID.Companion.PUFFERFISH_BUCKET, ItemID.Companion.WATER_BUCKET, ItemID.Companion.AXOLOTL_BUCKET, ItemID.Companion.TADPOLE_BUCKET -> BlockID.FLOWING_WATER
                ItemID.Companion.LAVA_BUCKET -> BlockID.FLOWING_LAVA
                ItemID.Companion.POWDER_SNOW_BUCKET -> BlockID.POWDER_SNOW
                else -> BlockID.AIR
            }
        }
    }
}
