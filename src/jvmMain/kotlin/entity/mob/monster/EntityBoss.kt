package org.chorus_oss.chorus.entity.mob.monster

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

abstract class EntityBoss(chunk: IChunk?, nbt: CompoundTag) : EntityMonster(chunk, nbt) {
    protected var blockBreakSound: Sound? = null

    override fun setHealthSafe(health: Float) {
        super.setHealthSafe(health)
        Server.broadcastPacket(
            viewers.values, org.chorus_oss.protocol.packets.BossEventPacket(
                targetActorID = this.runtimeId,
                eventType = org.chorus_oss.protocol.packets.BossEventPacket.Companion.EventType.UpdatePercent,
                eventData = org.chorus_oss.protocol.packets.BossEventPacket.Companion.EventType.Companion.UpdatePercentData(
                    healthPercent = health / maxHealth
                )
            )
        )
    }

    override fun spawnTo(player: Player) {
        super.spawnTo(player)
        if (player.locallyInitialized) {
            addBossbar(player)
        }
    }

    protected fun canBreakBlock(block: Block): Boolean {
        return when (block.id) {
            BlockID.BARRIER,
            BlockID.BEDROCK,
            BlockID.COMMAND_BLOCK,
            BlockID.CHAIN_COMMAND_BLOCK,
            BlockID.REPEATING_COMMAND_BLOCK,
            BlockID.CRYING_OBSIDIAN,
            BlockID.END_STONE,
            BlockID.END_PORTAL,
            BlockID.END_PORTAL_FRAME,
            BlockID.END_GATEWAY,
            BlockID.FIRE,
            BlockID.IRON_BARS,
            BlockID.JIGSAW,
            BlockID.OBSIDIAN,
            BlockID.REINFORCED_DEEPSLATE,
            BlockID.RESPAWN_ANCHOR,
            BlockID.SOUL_FIRE,
            BlockID.STRUCTURE_BLOCK -> false

            else -> true
        }
    }

    abstract fun addBossbar(player: Player)
}
