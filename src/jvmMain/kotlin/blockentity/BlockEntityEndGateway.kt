package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.projectile.throwable.EntityEnderPearl
import org.chorus_oss.chorus.event.player.PlayerTeleportEvent.TeleportCause
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector2
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.IntTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.protocol.types.BlockPos
import kotlin.math.max

class BlockEntityEndGateway(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    // NBT data
    var age: Int = 0
    var exitPortal: BlockVector3? = null

    // Others
    var teleportCooldown: Int = 0

    override fun initBlockEntity() {
        super.initBlockEntity()
        scheduleUpdate()
    }

    override fun loadNBT() {
        super.loadNBT()
        if (namedTag.contains("Age")) {
            this.age = namedTag.getInt("Age")
        } else {
            this.age = 0
        }

        if (namedTag.contains("ExitPortal")) {
            val exitPortalList = namedTag.getList("ExitPortal", IntTag::class.java)
            this.exitPortal = BlockVector3(exitPortalList[0].data, exitPortalList[1].data, exitPortalList[2].data)
        } else {
            this.exitPortal = defaultExitPortal.clone()
            if (position.toHorizontal().distance(Vector2.ZERO) < 100) {
                shift@ for (shift in intArrayOf(0, -5, 5, -10, 10)) { //Reduces the probability of a no hit
                    for (i in 0..15) {
                        if (exitPortal!!.y <= 16 || exitPortal!!.y > 128) {
                            this.exitPortal = Vector3(
                                position.x + shift, 0.0,
                                position.z + shift
                            ).normalize().multiply((0x500 + (i * 0xF)).toDouble()).asBlockVector3()
                            this.exitPortal = this.safeExitPortal
                        } else break@shift
                    }
                }
            }
        }

        this.teleportCooldown = 0
    }

    override val isBlockEntityValid: Boolean
        get() = level
            .getBlockIdAt(floorX, floorY, floorZ) === BlockID.END_GATEWAY

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putInt("Age", this.age)
        namedTag.putList(
            "ExitPortal", ListTag<IntTag>()
                .add(IntTag(exitPortal!!.x))
                .add(IntTag(exitPortal!!.y))
                .add(IntTag(exitPortal!!.z))
        )
    }

    override fun onUpdate(): Boolean {
        if (this.closed) {
            return false
        }

        val isGenerated = isGenerating

        age++

        if (teleportCooldown > 0) {
            teleportCooldown--
            if (teleportCooldown == 0) {
                setDirty()
                this.spawnToAll()
            }
        } else {
            if (this.age % 2400 == 0) {
                this.resetTeleportCooldown()
            }
        }

        if (isGenerated != isGenerating) {
            setDirty()
            this.spawnToAll()
        }

        return true
    }

    fun teleportEntity(entity: Entity) {
        if (exitPortal != null) {
            if (entity is EntityEnderPearl) {
                if (entity.shootingEntity != null) {
                    entity.shootingEntity!!.teleport(
                        checkTeleport(safeExitPortal.asVector3().asBlockVector3()).add(
                            0.5,
                            0.0,
                            0.5
                        ), TeleportCause.END_GATEWAY
                    )
                    entity.close()
                } else {
                    entity.teleport(
                        checkTeleport(safeExitPortal.asVector3().asBlockVector3()).add(0.5, 0.0, 0.5),
                        TeleportCause.END_GATEWAY
                    )
                }
            } else {
                entity.teleport(
                    checkTeleport(safeExitPortal.asVector3().asBlockVector3()).add(0.5, 0.0, 0.5),
                    TeleportCause.END_GATEWAY
                )
            }
        }
        resetTeleportCooldown()
    }

    protected fun checkTeleport(vector3: BlockVector3): BlockVector3 {
        if (vector3.y <= 16 || vector3.y > 128) {
            // Place a little platform in case no safe spawn was found
            vector3.setY(65)
            for (i in -2..2) {
                for (j in -1..1) {
                    level.setBlock(
                        Vector3((vector3.x + j).toDouble(), 64.0, (vector3.z + j).toDouble()), Block.get(
                            BlockID.END_STONE
                        )
                    )
                    level.setBlock(
                        Vector3((vector3.x + j).toDouble(), 64.0, (vector3.z + i).toDouble()), Block.get(
                            BlockID.END_STONE
                        )
                    )
                }
            }
        }
        return vector3
    }


    val safeExitPortal: BlockVector3
        get() {
            for (x in -1..1) {
                for (z in -1..1) {
                    val chunkX = (exitPortal!!.x shr 4) + x
                    val chunkZ = (exitPortal!!.z shr 4) + z
                    val chunk = level.getChunk(chunkX, chunkZ, false)
                    if (chunk == null || !(chunk.isGenerated || chunk.isPopulated)) {
                        level.syncGenerateChunk(chunkX, chunkZ)
                    }
                }
            }

            for (x in exitPortal!!.x - 5..exitPortal!!.x + 5) {
                for (z in exitPortal!!.z - 5..exitPortal!!.z + 5) {
                    for (y in 192 downTo (max(0.0, (exitPortal!!.y + 2).toDouble()) + 1).toInt()) {
                        if (level.getBlockStateAt(x, y, z) != BlockAir.STATE) {
                            if (level.getBlockStateAt(
                                    x,
                                    y,
                                    z
                                ) !== STATE_BEDROCK
                            ) {
                                return BlockVector3(x, y + 1, z)
                            }
                        }
                    }
                }
            }

            return exitPortal!!.up(2)
        }

    val isGenerating: Boolean
        get() = age < 200

    fun isTeleportCooldown(): Boolean {
        return teleportCooldown > 0
    }

    private fun resetTeleportCooldown() {
        this.resetTeleportCooldown(40)
    }

    private fun resetTeleportCooldown(teleportCooldown: Int) {
        this.teleportCooldown = teleportCooldown
        setDirty()
        sendBlockEventPacket(0)
        this.spawnToAll()
    }

    private fun sendBlockEventPacket(eventData: Int) {
        if (this.closed) {
            return
        }

        if (this.level == null) {
            return
        }

        val pk = org.chorus_oss.protocol.packets.BlockEventPacket(
            blockPosition = BlockPos(this.position),
            eventType = 1,
            eventValue = eventData,
        )
        level.addChunkPacket(position.chunkX, position.chunkZ, pk)
    }

    companion object {
        // Default value
        private val defaultExitPortal = BlockVector3(0, 0, 0)

        private val STATE_BEDROCK: BlockState = BlockBedrock.properties.defaultState
    }
}
