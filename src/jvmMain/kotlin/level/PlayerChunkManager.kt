package org.chorus_oss.chorus.level

import com.google.common.collect.Sets
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerChunkRequestEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.BlockPos
import org.jetbrains.annotations.ApiStatus
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class PlayerChunkManager(private val player: Player) {
    private val chunkDistanceComparator = Comparator<Long> { chunkHash1, chunkHash2 ->
        val floor: BlockVector3 = player.locator.position.asBlockVector3()
        val loaderChunkX = floor.x shr 4
        val loaderChunkZ = floor.z shr 4
        val chunkDX1: Int = loaderChunkX - Level.getHashX(chunkHash1)
        val chunkDZ1: Int = loaderChunkZ - Level.getHashZ(chunkHash1)
        val chunkDX2: Int = loaderChunkX - Level.getHashX(chunkHash2)
        val chunkDZ2: Int = loaderChunkZ - Level.getHashZ(chunkHash2)
        //Compare distance to loader
        (chunkDX1 * chunkDX1 + chunkDZ1 * chunkDZ1).compareTo(chunkDX2 * chunkDX2 + chunkDZ2 * chunkDZ2)
    }

    //保存着上tick已经发送的全部区块hash值
    @get:ApiStatus.Internal
    val usedChunks: HashSet<Long> = HashSet()

    //保存着这tick将要发送的全部区块hash值
    @get:ApiStatus.Internal
    val inRadiusChunks: HashSet<Long> = HashSet()
    private val trySendChunkCountPerTick: Int = player.chunkSendCountPerTick
    private val chunkSendQueue: PriorityQueue<Long> =
        PriorityQueue(player.viewDistance * player.viewDistance, chunkDistanceComparator)
    private val chunkLoadingQueue: HashMap<Long, CompletableFuture<IChunk?>?> =
        HashMap(player.viewDistance * player.viewDistance)
    private val chunkReadyToSend: HashMap<Long, IChunk> = HashMap()
    private var lastLoaderChunkPosHashed = java.lang.Long.MAX_VALUE

    /**
     * Handle chunk loading when the player teleported
     */
    @Synchronized
    fun handleTeleport() {
        if (!player.isConnected()) return
        val floor = player.position.asBlockVector3()
        updateInRadiusChunks(1, floor)
        removeOutOfRadiusChunks()
        updateChunkSendingQueue()
        loadQueuedChunks(5, true)
        sendChunk()
    }

    @Synchronized
    fun tick() {
        if (!player.isConnected()) return
        val currentLoaderChunkPosHashed: Long
        val floor = player.position.asBlockVector3()
        if ((Level.chunkHash(floor.x shr 4, floor.z shr 4)
                .also { currentLoaderChunkPosHashed = it }) != lastLoaderChunkPosHashed
        ) {
            lastLoaderChunkPosHashed = currentLoaderChunkPosHashed
            updateInRadiusChunks(player.viewDistance, floor)
            removeOutOfRadiusChunks()
            updateChunkSendingQueue()
        }
        loadQueuedChunks(trySendChunkCountPerTick, false)
        sendChunk()
    }

    @ApiStatus.Internal
    fun addSendChunk(x: Int, z: Int) {
        chunkSendQueue.add(Level.Companion.chunkHash(x, z))
    }

    private fun updateChunkSendingQueue() {
        chunkSendQueue.clear()
        //已经发送的区块不再二次发送
        val difference = Sets.difference(
            inRadiusChunks,
            usedChunks
        )
        for (v in difference) {
            chunkSendQueue.add(v)
        }
    }

    private fun updateInRadiusChunks(viewDistance: Int, currentPos: BlockVector3) {
        inRadiusChunks.clear()
        val loaderChunkX = currentPos.x shr 4
        val loaderChunkZ = currentPos.z shr 4
        for (rx in -viewDistance..viewDistance) {
            for (rz in -viewDistance..viewDistance) {
                if (ifChunkNotInRadius(rx, rz, viewDistance)) continue
                val chunkX = loaderChunkX + rx
                val chunkZ = loaderChunkZ + rz
                val hashXZ: Long = Level.Companion.chunkHash(chunkX, chunkZ)
                inRadiusChunks.add(hashXZ)
            }
        }
    }

    private fun removeOutOfRadiusChunks() {
        val difference: Set<Long> = HashSet(
            Sets.difference(
                usedChunks, inRadiusChunks
            )
        )
        // Unload blocks that are out of range
        for (hash in difference) {
            val x: Int = Level.Companion.getHashX(hash)
            val z: Int = Level.Companion.getHashZ(hash)
            if (player.level!!.unregisterChunkLoader(player, x, z)) {
                for (entity in player.level!!.getChunkEntities(x, z).values) {
                    if (entity !== player) {
                        entity.despawnFrom(player)
                    }
                }
            }
        }
        // The intersection of the remaining sentChunks and inRadiusChunks
        usedChunks.removeAll(difference)
    }

    private fun loadQueuedChunks(trySendChunkCountPerTick: Int, force: Boolean) {
        if (chunkSendQueue.isEmpty()) return
        var triedSendChunkCount = 0
        do {
            triedSendChunkCount++
            val chunkHash: Long = chunkSendQueue.poll()
            val chunkX: Int = Level.getHashX(chunkHash)
            val chunkZ: Int = Level.getHashZ(chunkHash)
            val chunkTask = chunkLoadingQueue.computeIfAbsent(
                chunkHash
            ) { player.level!!.getChunkAsync(chunkX, chunkZ) }
            if (chunkTask!!.isDone) {
                try {
                    val chunk = chunkTask[10, TimeUnit.MICROSECONDS]
                    if (chunk == null || !chunk.chunkState.canSend()) {
                        player.level!!.generateChunk(chunkX, chunkZ, force)
                        chunkSendQueue.add(chunkHash)
                        continue
                    }
                    chunkLoadingQueue.remove(chunkHash)
                    player.level!!.registerChunkLoader(player, chunkX, chunkZ, false)
                    chunkReadyToSend[chunkHash] = chunk
                } catch (ignore: InterruptedException) {
                } catch (ignore: ExecutionException) {
                } catch (e: TimeoutException) {
                    log.warn("read chunk timeout {} {}", chunkX, chunkZ)
                }
            } else {
                chunkSendQueue.add(chunkHash)
            }
        } while (!chunkSendQueue.isEmpty() && triedSendChunkCount < trySendChunkCountPerTick)
    }

    private fun sendChunk() {
        if (chunkReadyToSend.isNotEmpty()) {
            val ncp = org.chorus_oss.protocol.packets.NetworkChunkPublisherUpdatePacket(
                position = BlockPos(player.position),
                radius = (player.viewDistance shl 4).toUInt(),
                savedChunks = emptyList(),
            )
            player.sendPacket(ncp)
            for (e in chunkReadyToSend.keys) {
                val chunkX: Int = Level.getHashX(e)
                val chunkZ: Int = Level.getHashZ(e)
                val ev = PlayerChunkRequestEvent(player, chunkX, chunkZ)
                Server.instance.pluginManager.callEvent(ev)
                player.level!!.requestChunk(chunkX, chunkZ, player)
            }
            usedChunks.addAll(chunkReadyToSend.keys)
        }
        chunkReadyToSend.clear()
    }

    private fun ifChunkNotInRadius(chunkX: Int, chunkZ: Int, radius: Int): Boolean {
        return chunkX * chunkX + chunkZ * chunkZ > radius * radius
    }

    companion object : Loggable
}
