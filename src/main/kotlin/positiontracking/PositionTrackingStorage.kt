package org.chorus_oss.chorus.positiontracking

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.io.*
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * 在一个文件中存储[PositionTracking]对象的顺序范围。读取操作被缓存。
 *
 * 这个对象持有一个文件处理程序，当它不再需要时，必须关闭。
 *
 * 一旦关闭，该实例就不能被重新使用。
 *
 *
 * Stores a sequential range of [PositionTracking] objects in a file. The read operation is cached.
 *
 * This object holds a file handler and must be closed when it is no longer needed.
 *
 * Once closed the instance cannot be reused.
 */
class PositionTrackingStorage @JvmOverloads constructor(startIndex: Int, persistenceFile: File, maxStorage: Int = 0) :
    Closeable {
    val startingHandler: Int
    private var maxStorage = 0
    private var garbagePos: Long = 0
    private var stringHeapPos: Long = 0
    private val persistence: RandomAccessFile
    private val cache: Cache<Int, PositionTracking?> =
        CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).concurrencyLevel(1).build()
    private var nextIndex = 0

    /**
     * Opens or create the file and all directories in the path automatically. The given start index will be used
     * in new files and will be checked when opening files. If the file being opened don't matches this value
     * internally than an `IllegalArgumentException` will be thrown.
     *
     * @param startIndex      The number of the first handler. Must be higher than 0 and must match the number of the existing file.
     * @param persistenceFile The file being opened or created. Parent directories will also be created if necessary.
     * @param maxStorage      The maximum amount of positions that this storage may hold. It cannot be changed after creation.
     * Ignored when loading an existing file. When zero or negative, a default value will be used.
     * @throws IOException              If an error has occurred while reading, parsing or creating the file
     * @throws IllegalArgumentException If opening an existing file and the internal startIndex don't match the given startIndex
     */
    /**
     * Opens or create the file and all directories in the path automatically. The given start index will be used
     * in new files and will be checked when opening files. If the file being opened don't matches this value
     * internally than an `IllegalArgumentException` will be thrown.
     *
     * @param startIndex      The number of the first handler. Must be higher than 0 and must match the number of the existing file.
     * @param persistenceFile The file being opened or created. Parent directories will also be created if necessary.
     * @throws IOException              If an error has occurred while reading, parsing or creating the file
     * @throws IllegalArgumentException If opening an existing file and the internal startIndex don't match the given startIndex
     */
    init {
        var maxStorage = maxStorage
        check(startIndex > 0) { "Start index must be positive. Got $startIndex" }
        this.startingHandler = startIndex
        if (maxStorage <= 0) {
            maxStorage = DEFAULT_MAX_STORAGE
        }

        var created = false
        if (!persistenceFile.isFile) {
            if (!persistenceFile.parentFile.isDirectory && !persistenceFile.parentFile.mkdirs()) {
                throw FileNotFoundException("Could not create the directory " + persistenceFile.parent)
            }
            if (!persistenceFile.createNewFile()) {
                throw FileNotFoundException("Could not create the file $persistenceFile")
            }
            created = true
        } else if (persistenceFile.length() == 0L) {
            created = true
        }

        this.persistence = RandomAccessFile(persistenceFile, "rwd")
        try {
            if (created) {
                persistence.write(
                    ByteBuffer.allocate(HEADER.size + 4 + 4 + 4)
                        .put(HEADER)
                        .putInt(maxStorage)
                        .putInt(startIndex)
                        .putInt(startIndex)
                        .array()
                )
                this.maxStorage = maxStorage
                nextIndex = startIndex
            } else {
                val check = ByteArray(HEADER.size)
                var eof: EOFException? = null
                var max: Int
                var next: Int
                var start: Int
                try {
                    persistence.readFully(check)
                    val buf = ByteArray(4 + 4 + 4)
                    persistence.readFully(buf)
                    val buffer = ByteBuffer.wrap(buf)
                    max = buffer.getInt()
                    next = buffer.getInt()
                    start = buffer.getInt()
                } catch (e: EOFException) {
                    eof = e
                    max = 0
                    next = 0
                    start = 0
                }
                if (eof != null || max <= 0 || next <= 0 || start <= 0 || !check.contentEquals(HEADER)) {
                    throw IOException(
                        "The file $persistenceFile is not a valid PowerNukkit TrackingPositionDB persistence file.",
                        eof
                    )
                }
                require(start == startIndex) { "The start index $startIndex was given but the file $persistenceFile has start index $start" }
                maxStorage = max
                this.maxStorage = maxStorage
                this.nextIndex = next
            }
            garbagePos = getAxisPos(startIndex + maxStorage)

            //                          cnt  off len  max
            stringHeapPos = garbagePos + 4 + (8 + 4) * 15

            if (created) {
                persistence.seek(stringHeapPos - 1)
                persistence.writeByte(0)
            }
        } catch (e: Throwable) {
            try {
                persistence.close()
            } catch (e2: Throwable) {
                e.addSuppressed(e2)
            }
            throw e
        }
    }

    private fun getAxisPos(trackingHandler: Int): Long {
        //                    max str cur  on  nam len  x   y   z 
        return HEADER.size + 4 + 4 + 4 + (1 + 8 + 4 + 8 + 8 + 8) * (trackingHandler - startingHandler).toLong()
    }

    private fun validateHandler(trackingHandler: Int) {
        check(
            trackingHandler >= startingHandler
        ) { "The trackingHandler $trackingHandler is too low for this storage (starts at $startingHandler)" }
        val limit = startingHandler + maxStorage
        check(
            trackingHandler <= limit
        ) { "The trackingHandler $trackingHandler is too high for this storage (ends at $limit)" }
    }

    /**
     * Retrieves the [PositionTracking] object that is assigned to the given trackingHandler.
     * The handler must be valid for this storage.
     *
     * This call may return a cached result but the returned object can be modified freely.
     *
     * @param trackingHandler A valid handler for this storage
     * @return A clone of the cached result.
     * @throws IOException              If an error has occurred while accessing the file
     * @throws IllegalArgumentException If the trackingHandler is not valid for this storage
     */
    @Throws(IOException::class)
    fun getPosition(trackingHandler: Int): PositionTracking? {
        validateHandler(trackingHandler)
        try {
            return cache[trackingHandler, { loadPosition(trackingHandler, true) }]?.clone()
        } catch (e: ExecutionException) {
            throw handleExecutionException(e)
        }
    }

    /**
     * Retrieves the [PositionTracking] object that is assigned to the given trackingHandler.
     * The handler must be valid for this storage.
     *
     * This call may return a cached result but the returned object can be modified freely.
     *
     * @param trackingHandler A valid handler for this storage
     * @param onlyEnabled     When false, disabled positions that wasn't invalidated may be returned.
     * Caching only works when this is set to true
     * @return A clone of the cached result.
     * @throws IOException              If an error has occurred while accessing the file
     * @throws IllegalArgumentException If the trackingHandler is not valid for this storage
     */
    @Throws(IOException::class)
    fun getPosition(trackingHandler: Int, onlyEnabled: Boolean): PositionTracking? {
        if (onlyEnabled) {
            return getPosition(trackingHandler)
        }
        validateHandler(trackingHandler)
        return loadPosition(trackingHandler, false)
    }

    /**
     * Attempts to reuse an existing and enabled trackingHandler for the given position, if none is found than a new handler is created
     * if the limit was not exceeded.
     *
     * @param position The position that needs a handler
     * @return The trackingHandler assigned to the position or an empty OptionalInt if none was found and this storage is full
     * @throws IOException If an error occurred while reading or writing the file
     */
    @Throws(IOException::class)
    fun addOrReusePosition(position: NamedPosition): Int? {
        val handler = findTrackingHandler(position)
        if (handler != null) {
            return handler
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
    fun addNewPosition(position: NamedPosition): Int? {
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
    fun addNewPosition(position: NamedPosition, enabled: Boolean): Int? {
        val handler = addNewPos(position, enabled) ?: return null
        if (enabled) {
            cache.put(handler, PositionTracking(position))
        }
        return handler
    }

    @Throws(IOException::class)
    fun findTrackingHandler(position: NamedPosition): Int? {
        val cached = cache.asMap().entries
            .filter { e ->
                e.value?.matchesNamedPosition(
                    position
                ) ?: false
            }
            .map { it.key }
            .first()
        if (cached != null) {
            return cached
        }
        val handlers = findTrackingHandlers(position, true, 1)
        if (handlers.isEmpty()) {
            return null
        }
        val found = handlers[0]
        cache.put(found, PositionTracking(position))
        return found
    }

    private fun handleExecutionException(e: ExecutionException): IOException {
        val cause = e.cause
        if (cause is IOException) {
            return cause
        }
        return IOException(e)
    }

    @Synchronized
    @Throws(IOException::class)
    fun invalidateHandler(trackingHandler: Int) {
        validateHandler(trackingHandler)
        invalidatePos(trackingHandler)
    }

    @Synchronized
    @Throws(IOException::class)
    fun isEnabled(trackingHandler: Int): Boolean {
        validateHandler(trackingHandler)
        persistence.seek(getAxisPos(trackingHandler))
        return persistence.readBoolean()
    }

    @Synchronized
    @Throws(IOException::class)
    fun setEnabled(trackingHandler: Int, enabled: Boolean): Boolean {
        validateHandler(trackingHandler)
        val pos = getAxisPos(trackingHandler)
        persistence.seek(pos)
        if (persistence.readBoolean() == enabled) {
            return false
        }
        if (persistence.readLong() == 0L && enabled) {
            return false
        }
        persistence.seek(pos)
        persistence.writeBoolean(enabled)
        cache.invalidate(trackingHandler)
        return true
    }

    @Synchronized
    @Throws(IOException::class)
    fun hasPosition(trackingHandler: Int): Boolean {
        return hasPosition(trackingHandler, true)
    }

    @Synchronized
    @Throws(IOException::class)
    fun hasPosition(trackingHandler: Int, onlyEnabled: Boolean): Boolean {
        validateHandler(trackingHandler)
        persistence.seek(getAxisPos(trackingHandler))
        val enabled = persistence.readBoolean()
        if (!enabled && onlyEnabled) {
            return false
        }
        return persistence.readLong() != 0L
    }

    @Synchronized
    @Throws(IOException::class)
    private fun invalidatePos(trackingHandler: Int) {
        val pos = getAxisPos(trackingHandler)
        persistence.seek(pos)
        persistence.writeBoolean(false)
        val buf = ByteArray(8 + 4)
        persistence.readFully(buf)
        val buffer = ByteBuffer.wrap(buf)
        val namePos = buffer.getLong()
        val nameLen = buffer.getInt()
        persistence.seek(pos + 1)
        persistence.write(ByteArray(8 + 4))
        cache.put(trackingHandler, null)
        addGarbage(namePos, nameLen)
    }

    @Synchronized
    @Throws(IOException::class)
    private fun addGarbage(pos: Long, len: Int) {
        persistence.seek(garbagePos)
        val count = persistence.readInt()
        if (count >= 15) {
            return
        }
        val buf = ByteArray(4 + 8)
        val buffer = ByteBuffer.wrap(buf)
        if (count > 0) {
            for (attempt in 0..14) {
                persistence.readFully(buf)
                buffer.rewind()
                val garbage = buffer.getLong()
                val garbageLen = buffer.getInt()
                if (garbage != 0L) {
                    if (garbage + garbageLen == pos) {
                        persistence.seek(persistence.filePointer - 4 - 8)
                        buffer.rewind()
                        buffer.putLong(garbage)
                            .putInt(garbageLen + len)
                        persistence.write(buf)
                        return
                    } else if (pos + len == garbage) {
                        persistence.seek(persistence.filePointer - 4 - 8)
                        buffer.rewind()
                        buffer.putLong(pos)
                            .putInt(garbageLen + len)
                        persistence.write(buf)
                        return
                    }
                }
            }

            persistence.seek(garbagePos + 4)
        }

        for (attempt in 0..14) {
            persistence.readFully(buf)
            buffer.rewind()
            val garbage = buffer.getLong()
            if (garbage == 0L) {
                persistence.seek(persistence.filePointer - 4 - 8)
                buffer.rewind()
                buffer.putLong(pos).putInt(len)
                persistence.write(buf)
                persistence.seek(garbagePos)
                persistence.writeInt(count + 1)
                return
            }
        }
    }

    @Synchronized
    @Throws(IOException::class)
    private fun findSpaceInStringHeap(len: Int): Long {
        persistence.seek(garbagePos)
        val remaining = persistence.readInt()
        if (remaining <= 0) {
            return persistence.length()
        }

        val buf = ByteArray(4 + 8)
        val buffer = ByteBuffer.wrap(buf)
        for (attempt in 0..14) {
            persistence.readFully(buf)
            buffer.rewind()
            val garbage = buffer.getLong()
            val garbageLen = buffer.getInt()
            if (garbage >= stringHeapPos && len <= garbageLen) {
                persistence.seek(persistence.filePointer - 4 - 8)
                if (garbageLen == len) {
                    persistence.write(ByteArray(8 + 4))
                    persistence.seek(garbagePos)
                    persistence.writeInt(remaining - 1)
                } else {
                    buffer.rewind()
                    buffer.putLong(garbage + len).putInt(garbageLen - len)
                    persistence.write(buf)
                }
                return garbage
            }
        }
        return persistence.length()
    }

    @Synchronized
    @Throws(IOException::class)
    private fun addNewPos(pos: NamedPosition, enabled: Boolean): Int? {
        if (nextIndex - startingHandler >= maxStorage) {
            return null
        }
        val handler = nextIndex++
        writePos(handler, pos, enabled)
        persistence.seek((HEADER.size + 4).toLong())
        persistence.writeInt(nextIndex)
        return handler
    }

    @Synchronized
    @Throws(IOException::class)
    private fun writePos(trackingHandler: Int, pos: NamedPosition, enabled: Boolean) {
        val name = pos.levelName.toByteArray(StandardCharsets.UTF_8)
        val namePos = addLevelName(name)
        persistence.seek(getAxisPos(trackingHandler))
        persistence.write(
            ByteBuffer.allocate(1 + 8 + 4 + 8 + 8 + 8)
                .put(if (enabled) 1.toByte() else 0)
                .putLong(namePos)
                .putInt(name.size)
                .putDouble(pos.x)
                .putDouble(pos.y)
                .putDouble(pos.z)
                .array()
        )
    }

    @Synchronized
    @Throws(IOException::class)
    private fun addLevelName(name: ByteArray): Long {
        val pos = findSpaceInStringHeap(name.size)
        persistence.seek(pos)
        persistence.write(name)
        return pos
    }

    @Synchronized
    @Throws(IOException::class)
    fun findTrackingHandlers(pos: NamedPosition): List<Int> {
        return findTrackingHandlers(pos, true)
    }

    @Synchronized
    @Throws(IOException::class)
    fun findTrackingHandlers(pos: NamedPosition, onlyEnabled: Boolean): List<Int> {
        return findTrackingHandlers(pos, onlyEnabled, Int.MAX_VALUE)
    }

    @Synchronized
    @Throws(IOException::class)
    fun findTrackingHandlers(pos: NamedPosition, onlyEnabled: Boolean, limit: Int): List<Int> {
        persistence.seek((HEADER.size + 4 + 4 + 4).toLong())
        var handler = startingHandler - 1
        val lookingX = pos.x
        val lookingY = pos.y
        val lookingZ = pos.z
        val lookingName = pos.levelName.toByteArray(StandardCharsets.UTF_8)
        val results = mutableListOf(limit.coerceIn(1, 16))
        val buf = ByteArray(8 + 4 + 8 + 8 + 8)
        val buffer = ByteBuffer.wrap(buf)
        while (true) {
            handler++
            if (handler >= nextIndex) {
                return results
            }
            val enabled = persistence.readBoolean()
            if (onlyEnabled && !enabled) {
                if (persistence.skipBytes(36) != 36) throw EOFException()
                continue
            }

            persistence.readFully(buf)

            buffer.rewind()
            val namePos = buffer.getLong()
            val nameLen = buffer.getInt()
            val x = buffer.getDouble()
            val y = buffer.getDouble()
            val z = buffer.getDouble()
            if (namePos > 0 && nameLen > 0 && x == lookingX && y == lookingY && z == lookingZ) {
                val fp = persistence.filePointer
                val nameBytes = ByteArray(nameLen)
                persistence.seek(namePos)
                persistence.readFully(nameBytes)
                if (lookingName.contentEquals(nameBytes)) {
                    results.add(handler)
                    if (results.size >= limit) {
                        return results
                    }
                }
                persistence.seek(fp)
            }
        }
    }

    @Synchronized
    @Throws(IOException::class)
    private fun loadPosition(trackingHandler: Int, onlyEnabled: Boolean): PositionTracking? {
        if (trackingHandler >= nextIndex) {
            return null
        }

        persistence.seek(getAxisPos(trackingHandler))
        val buf = ByteArray(1 + 8 + 4 + 8 + 8 + 8)
        persistence.readFully(buf)
        val enabled = buf[0].toInt() == 1
        if (!enabled && onlyEnabled) {
            return null
        }

        val buffer = ByteBuffer.wrap(buf, 1, buf.size - 1)

        val namePos = buffer.getLong()
        if (namePos == 0L) {
            return null
        }
        val nameLen = buffer.getInt()

        val x = buffer.getDouble()
        val y = buffer.getDouble()
        val z = buffer.getDouble()

        val nameBytes = ByteArray(nameLen)
        persistence.seek(namePos)
        persistence.readFully(nameBytes)
        val name = String(nameBytes, StandardCharsets.UTF_8)
        return PositionTracking(name, x, y, z)
    }

    val maxHandler: Int
        get() = startingHandler + maxStorage - 1

    @Synchronized
    @Throws(IOException::class)
    override fun close() {
        persistence.close()
    }

    companion object {
        const val DEFAULT_MAX_STORAGE: Int = 500
        private val HEADER = byteArrayOf(
            12,
            32,
            32,
            'P'.code.toByte(),
            'N'.code.toByte(),
            'P'.code.toByte(),
            'T'.code.toByte(),
            'D'.code.toByte(),
            'B'.code.toByte(),
            '1'.code.toByte()
        )
    }
}
