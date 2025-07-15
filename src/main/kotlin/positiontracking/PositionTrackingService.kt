package org.chorus_oss.chorus.positiontracking

import com.google.common.base.Preconditions
import com.google.common.collect.MapMaker
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemLodestoneCompass
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.nbt.tags.ByteTag
import org.chorus_oss.nbt.tags.CompoundTag
import org.chorus_oss.nbt.tags.IntTag
import org.chorus_oss.nbt.tags.ListTag
import org.chorus_oss.protocol.packets.PositionTrackingDBServerBroadcastPacket
import java.io.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

/**
 * A position tracking db service. It holds file resources that needs to be closed when not needed anymore.
 */
class PositionTrackingService(folder: File) : Closeable {
    private val storage = TreeMap<Int, WeakReference<PositionTrackingStorage?>>(Comparator.reverseOrder())
    private val closed = AtomicBoolean(false)
    private val folder: File
    private val tracking: MutableMap<Player, MutableMap<PositionTrackingStorage, MutableSet<Int>>> =
        MapMaker().weakKeys().makeMap()

    /**
     * Creates position tracking db service. The service is ready to be used right after the creation.
     *
     * @param folder The folder that will hold the position tracking db files
     * @throws FileNotFoundException If the folder does not exist and can't be created
     */
    init {
        if (!folder.isDirectory && !folder.mkdirs()) {
            throw FileNotFoundException("Failed to create the folder $folder")
        }
        this.folder = folder
        val emptyRef = WeakReference<PositionTrackingStorage?>(null)
        Arrays.stream(Optional.ofNullable(folder.list(FILENAME_FILTER)).orElseThrow {
            FileNotFoundException("Invalid folder: $folder")
        })
            .map { name: String -> name.substring(0, name.length - 4).toInt() }
            .forEachOrdered { startIndex: Int -> storage[startIndex] = emptyRef }
    }

    @Throws(IOException::class)
    private fun hasTrackingDevice(player: Player, inventory: Inventory?, trackingHandler: Int): Boolean {
        if (inventory == null) {
            return false
        }
        val size = inventory.size
        for (i in 0..<size) {
            if (isTrackingDevice(player, inventory.getItem(i), trackingHandler)) {
                return true
            }
        }
        return false
    }

    @Throws(IOException::class)
    private fun isTrackingDevice(player: Player, item: Item?, trackingHandler: Int): Boolean {
        if (!(item != null && item.id == ItemID.LODESTONE_COMPASS && item is ItemLodestoneCompass)) {
            return false
        }

        if (item.trackingHandle != trackingHandler) {
            return false
        }

        val position = getPosition(trackingHandler)
        return position != null && position.levelName == player.locator.levelName
    }

    @Throws(IOException::class)
    fun hasTrackingDevice(player: Player, trackingHandler: Int): Boolean {
        for (inventory in inventories(player)) {
            if (hasTrackingDevice(player, inventory, trackingHandler)) {
                return true
            }
        }
        return false
    }

    private fun sendTrackingUpdate(player: Player, trackingHandler: Int, pos: PositionTracking) {
        if (player.locator.levelName == pos.levelName) {
            val packet = PositionTrackingDBServerBroadcastPacket(
                broadcastAction = PositionTrackingDBServerBroadcastPacket.Companion.Action.Update,
                trackingID = trackingHandler,
                payload = CompoundTag(
                    mapOf(
                        "pos" to ListTag(
                            listOf(
                                IntTag(pos.floorX),
                                IntTag(pos.floorY),
                                IntTag(pos.floorZ)
                            )
                        ),
                        "status" to ByteTag(0),
                        "dimension" to IntTag(player.level!!.dimension)
                    )
                )
            )
            player.sendPacket(packet)
        } else {
            sendTrackingDestroy(player, trackingHandler)
        }
    }

    private fun sendTrackingDestroy(player: Player, trackingHandler: Int) {
        val packet = destroyPacket(trackingHandler)
        player.sendPacket(packet)
    }

    @Synchronized
    @Throws(IOException::class)
    fun startTracking(player: Player, trackingHandler: Int, validate: Boolean): PositionTracking? {
        Preconditions.checkArgument(trackingHandler >= 0, "Tracking handler must be positive")
        if (isTracking(player, trackingHandler, validate)) {
            val position = getPosition(trackingHandler)
            if (position != null) {
                sendTrackingUpdate(player, trackingHandler, position)
                return position
            }
            stopTracking(player, trackingHandler)
            return null
        }

        if (validate && !hasTrackingDevice(player, trackingHandler)) {
            return null
        }

        val storage = getStorageForHandler(trackingHandler) ?: return null

        val position = storage.getPosition(trackingHandler) ?: return null

        tracking.computeIfAbsent(player) { mutableMapOf() }
            .computeIfAbsent(storage) { mutableSetOf() }.add(trackingHandler)
        return position
    }

    private fun destroyPacket(trackingHandler: Int): PositionTrackingDBServerBroadcastPacket {
        val packet = PositionTrackingDBServerBroadcastPacket(
            broadcastAction = PositionTrackingDBServerBroadcastPacket.Companion.Action.Destroy,
            trackingID = trackingHandler,
            payload = CompoundTag(
                mapOf(
                    "pos" to ListTag(
                        listOf(
                            IntTag(0),
                            IntTag(0),
                            IntTag(0)
                        )
                    ),
                    "status" to ByteTag(2),
                    "dimension" to IntTag(0)
                )
            )
        )
        return packet
    }

    @Synchronized
    fun stopTracking(player: Player): Boolean {
        val toRemove = tracking.remove(player)
        if (toRemove != null && player.isOnline) {
            val packets = toRemove.values.flatMap { it }.map { this.destroyPacket(it) }
            for (p in packets) {
                player.sendPacket(p)
            }
        }
        return toRemove != null
    }

    @Synchronized
    fun stopTracking(player: Player, trackingHandler: Int): Boolean {
        val tracking = tracking[player] ?: return false

        for ((key, value) in tracking) {
            if (value.remove(trackingHandler)) {
                if (value.isEmpty()) {
                    tracking.remove(key)
                }
                player.sendPacket(destroyPacket(trackingHandler))
                return true
            }
        }
        return false
    }

    @Synchronized
    @Throws(IOException::class)
    fun isTracking(player: Player, trackingHandler: Int, validate: Boolean): Boolean {
        val tracking = tracking[player]
            ?: return false

        for (value in tracking.values) {
            if (value.contains(trackingHandler)) {
                if (validate && !hasTrackingDevice(player, trackingHandler)) {
                    stopTracking(player, trackingHandler)
                    return false
                }
                return true
            }
        }
        return false
    }

    @Synchronized
    fun forceRecheckAllPlayers() {
        tracking.keys.removeIf { p: Player -> !p.isOnline }
        val toRemove: MutableMap<Player, MutableList<Int>> = mutableMapOf()
        for ((player, value) in tracking) {
            for ((_, value1) in value) {
                value1.forEach { trackingHandler: Int ->
                    try {
                        if (!hasTrackingDevice(player, trackingHandler)) {
                            toRemove.computeIfAbsent(player) { mutableListOf() }.add(trackingHandler)
                        }
                    } catch (e: IOException) {
                        log.error(
                            "Failed to update the tracking handler {} for player {}",
                            trackingHandler,
                            player.getEntityName(),
                            e
                        )
                    }
                }
            }
        }

        toRemove.forEach { (player, list) ->
            list.forEach { handler: Int ->
                stopTracking(
                    player,
                    handler
                )
            }
        }

        Server.instance.onlinePlayers.values.forEach(Consumer { player: Player -> this.detectNeededUpdates(player) })
    }

    private fun inventories(player: Player): Iterable<Inventory?> {
        return Iterable {
            object : Iterator<Inventory?> {
                var next: Int = 0

                override fun hasNext(): Boolean {
                    return next <= 4
                }

                override fun next(): Inventory? {
                    return when (next++) {
                        0 -> player.inventory
                        1 -> player.cursorInventory
                        2 -> player.offhandInventory
                        3 -> player.craftingGrid
                        4 -> player.topWindow.orElse(null)
                        else -> throw NoSuchElementException()
                    }
                }
            }
        }
    }

    private fun detectNeededUpdates(player: Player) {
        for (inventory in inventories(player)) {
            if (inventory == null) {
                continue
            }
            val size = inventory.size
            for (slot in 0..<size) {
                val item = inventory.getItem(slot)
                if (item.id == ItemID.LODESTONE_COMPASS && item is ItemLodestoneCompass) {
                    val trackingHandle = item.trackingHandle
                    if (trackingHandle != 0) {
                        val pos: PositionTracking?
                        try {
                            pos = getPosition(trackingHandle)
                            if (pos != null && pos.levelName == player.locator.levelName) {
                                startTracking(player, trackingHandle, false)
                            }
                        } catch (e: IOException) {
                            log.error(
                                "Failed to get the position of the tracking handler {}",
                                trackingHandle,
                                e
                            )
                        }
                    }
                }
            }
        }
    }

    fun forceRecheck(player: Player) {
        val tracking = tracking[player]
        if (tracking != null) {
            val toRemove: MutableList<Int> = mutableListOf()
            for ((_, value) in tracking) {
                value.forEach { trackingHandler: Int ->
                    try {
                        if (!hasTrackingDevice(player, trackingHandler)) {
                            toRemove.add(trackingHandler)
                        }
                    } catch (e: IOException) {
                        log.error(
                            "Failed to update the tracking handler {} for player {}",
                            trackingHandler,
                            player.getEntityName(),
                            e
                        )
                    }
                }
            }
            toRemove.forEach { handler: Int -> stopTracking(player, handler) }
        }

        detectNeededUpdates(player)
    }

    @Synchronized
    private fun findStorageForHandler(handler: Int): Int? {
        var best: Int? = null
        for (startIndex in storage.keys) {
            val comp = startIndex.compareTo(handler)
            if (comp == 0) {
                return startIndex
            }
            if (comp < 0 && (best == null || best.compareTo(startIndex) < 0)) {
                best = startIndex
            }
        }
        return best
    }

    @Synchronized
    @Throws(IOException::class)
    private fun loadStorage(startIndex: Int): PositionTrackingStorage {
        val trackingStorage = storage[startIndex]!!.get()
        if (trackingStorage != null) {
            return trackingStorage
        }
        val positionTrackingStorage = PositionTrackingStorage(
            startIndex, File(
                folder,
                "$startIndex.pnt"
            )
        )
        storage[startIndex] = WeakReference(positionTrackingStorage)
        return positionTrackingStorage
    }

    @Synchronized
    @Throws(IOException::class)
    private fun getStorageForHandler(trackingHandler: Int): PositionTrackingStorage? {
        val startIndex = findStorageForHandler(trackingHandler) ?: return null

        val storage = loadStorage(startIndex)
        if (trackingHandler > storage.maxHandler) {
            return null
        }

        return storage
    }

    /**
     * Attempts to reuse an existing and enabled trackingHandler for the given position, if none is found than a new handler is created
     * if the limit was not exceeded.
     *
     * @param position The position that needs a handler
     * @return The trackingHandler assigned to the position or an empty OptionalInt if none was found and this storage is full
     * @throws IOException If an error occurred while reading or writing the file
     */
    @Synchronized
    @Throws(IOException::class)
    fun addOrReusePosition(position: NamedPosition): Int {
        checkClosed()
        val trackingHandler = findTrackingHandler(position)
        if (trackingHandler.isPresent) {
            return trackingHandler.asInt
        }
        return addNewPosition(position)
    }

    /**
     * Adds the given position as a new entry in this storage, even if the position is already registered and enabled.
     *
     * @param position The position that needs a handler
     * @return The trackingHandler assigned to the position or an empty OptionalInt if none was found and this storage is full
     * @throws IOException If an error occurred while reading or writing the file
     */
    @Synchronized
    @Throws(IOException::class)
    fun addNewPosition(position: NamedPosition): Int {
        return addNewPosition(position, true)
    }

    /**
     * Adds the given position as a new entry in this storage, even if the position is already registered and enabled.
     *
     * @param position The position that needs a handler
     * @param enabled  If the position will be added as enabled or disabled
     * @return The trackingHandler assigned to the position or an empty OptionalInt if none was found and this storage is full
     * @throws IOException If an error occurred while reading or writing the file
     */
    @Synchronized
    @Throws(IOException::class)
    fun addNewPosition(position: NamedPosition, enabled: Boolean): Int {
        checkClosed()
        var next = 1
        if (!storage.isEmpty()) {
            val trackingStorage = loadStorage(storage.firstKey())
            val handler = trackingStorage.addNewPosition(position, enabled)
            if (handler != null) {
                return handler
            }
            next = trackingStorage.maxHandler
        }

        val trackingStorage = PositionTrackingStorage(next, File(folder, "$next.pnt"))
        storage[next] = WeakReference(trackingStorage)
        return trackingStorage.addNewPosition(position, enabled) ?: throw RuntimeException()
    }

    @Throws(IOException::class)
    fun findTrackingHandler(position: NamedPosition): OptionalInt {
        val handlers = findTrackingHandlers(position, true, 1)
        if (!handlers.isEmpty()) {
            return OptionalInt.of(handlers[0])
        }
        return OptionalInt.empty()
    }

    @Synchronized
    @Throws(IOException::class)
    fun invalidateHandler(trackingHandler: Int): Boolean {
        checkClosed()
        val storage = getStorageForHandler(trackingHandler) ?: return false

        if (!storage.hasPosition(trackingHandler, false)) {
            return false
        }
        storage.invalidateHandler(trackingHandler)

        handlerDisabled(trackingHandler)

        return true
    }

    private fun handlerDisabled(trackingHandler: Int) {
        val players: MutableList<Player> = ArrayList()
        for ((key, value1) in tracking) {
            for (value in value1.values) {
                if (value.contains(trackingHandler)) {
                    players.add(key)
                    break
                }
            }
        }

        if (!players.isEmpty()) {
            Server.broadcastPacket(players, destroyPacket(trackingHandler))
        }
    }

    @Throws(IOException::class)
    private fun handlerEnabled(trackingHandler: Int) {
        val server = Server.instance
        for (player in server.onlinePlayers.values) {
            if (hasTrackingDevice(player, trackingHandler) && !isTracking(player, trackingHandler, false)) {
                startTracking(player, trackingHandler, false)
            }
        }
    }

    @Throws(IOException::class)
    fun getPosition(trackingHandle: Int): PositionTracking? {
        return getPosition(trackingHandle, true)
    }

    @Throws(IOException::class)
    fun getPosition(trackingHandle: Int, onlyEnabled: Boolean): PositionTracking? {
        checkClosed()
        val trackingStorage = getStorageForHandler(trackingHandle) ?: return null

        return trackingStorage.getPosition(trackingHandle, onlyEnabled)
    }

    @Synchronized
    @Throws(IOException::class)
    fun isEnabled(trackingHandler: Int): Boolean {
        checkClosed()
        val trackingStorage = getStorageForHandler(trackingHandler)
        return trackingStorage != null && trackingStorage.isEnabled(trackingHandler)
    }

    @Synchronized
    @Throws(IOException::class)
    fun setEnabled(trackingHandler: Int, enabled: Boolean): Boolean {
        checkClosed()
        val trackingStorage = getStorageForHandler(trackingHandler) ?: return false
        if (trackingStorage.setEnabled(trackingHandler, enabled)) {
            if (enabled) {
                handlerEnabled(trackingHandler)
            } else {
                handlerDisabled(trackingHandler)
            }
            return true
        }

        return false
    }

    @Synchronized
    @Throws(IOException::class)
    fun hasPosition(trackingHandler: Int): Boolean {
        return hasPosition(trackingHandler, true)
    }

    @Synchronized
    @Throws(IOException::class)
    fun hasPosition(trackingHandler: Int, onlyEnabled: Boolean): Boolean {
        checkClosed()
        val startIndex = findStorageForHandler(trackingHandler) ?: return false

        if (!storage.containsKey(startIndex)) {
            return false
        }

        return loadStorage(startIndex).hasPosition(trackingHandler, onlyEnabled)
    }

    @Synchronized
    @Throws(IOException::class)
    fun findTrackingHandlers(pos: NamedPosition): MutableList<Int> {
        return findTrackingHandlers(pos, true)
    }

    @Synchronized
    @Throws(IOException::class)
    fun findTrackingHandlers(pos: NamedPosition, onlyEnabled: Boolean): MutableList<Int> {
        return findTrackingHandlers(pos, onlyEnabled, Int.MAX_VALUE)
    }

    @Synchronized
    @Throws(IOException::class)
    fun findTrackingHandlers(pos: NamedPosition, onlyEnabled: Boolean, limit: Int): MutableList<Int> {
        checkClosed()
        val list: MutableList<Int> = mutableListOf()
        for (startIndex in storage.descendingKeySet()) {
            list.addAll(loadStorage(startIndex).findTrackingHandlers(pos, onlyEnabled, limit - list.size))
            if (list.size >= limit) {
                break
            }
        }
        return list
    }

    /**
     * Close all active
     *
     * @throws IOException If any resource failed to close properly.
     * The detailed exceptions will be in getCause() and and getSuppressed()
     */
    @Synchronized
    @Throws(IOException::class)
    override fun close() {
        closed.set(true)
        var exception: IOException? = null
        for (ref in storage.values) {
            val positionTrackingStorage = ref.get()
            if (positionTrackingStorage != null) {
                try {
                    positionTrackingStorage.close()
                } catch (e: Throwable) {
                    if (exception == null) {
                        exception = IOException(e)
                    } else {
                        exception.addSuppressed(e)
                    }
                }
            }
        }
        if (exception != null) {
            throw exception
        }
    }

    @Throws(IOException::class)
    private fun checkClosed() {
        if (closed.get()) {
            throw IOException("The service is closed")
        }
    }

    companion object : Loggable {
        private val FILENAME_PATTERN = Regex("^\\d+\\.pnt$", RegexOption.IGNORE_CASE)
        private val FILENAME_FILTER = FilenameFilter { dir, name ->
            FILENAME_PATTERN.matches(name) && File(
                dir,
                name
            ).isFile
        }
    }
}
