package org.chorus_oss.chorus.nbt.stream

import java.io.FilterOutputStream
import java.io.IOException
import java.io.InterruptedIOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.*
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

/**
 * A multithreaded version of [java.util.zip.GZIPOutputStream].
 */
class PGZIPOutputStream(
    out: OutputStream?, // TODO: Share, daemonize.
    private val executor: ExecutorService, private val nthreads: Int
) :
    FilterOutputStream(out) {
    // TODO: remove after block guessing is implemented
    // array list that contains the block sizes
    private val blockSizes = mutableListOf<Int>()

    private var level = Deflater.BEST_SPEED
    private var strategy = Deflater.DEFAULT_STRATEGY

    fun newDeflater(): Deflater {
        val def = Deflater(level, true)
        def.setStrategy(strategy)
        return def
    }

    fun setStrategy(strategy: Int) {
        this.strategy = strategy
    }

    fun setLevel(level: Int) {
        this.level = level
    }

    private val crc = CRC32()
    private val emitQueue: BlockingQueue<Future<ByteArray>> =
        ArrayBlockingQueue(nthreads)
    private var block = PGZIPBlock(this /* 0 */)

    /**
     * Used as a sentinel for 'closed'.
     */
    private var bytesWritten = 0

    // Master thread only
    init {
        writeHeader()
    }

    /**
     * Creates a PGZIPOutputStream
     * using [PGZIPOutputStream.getSharedThreadPool].
     *
     * @param out the eventual output stream for the compressed data.
     * @throws java.io.IOException if it all goes wrong.
     */
    /**
     * Creates a PGZIPOutputStream
     * using [PGZIPOutputStream.getSharedThreadPool]
     * and [Runtime.availableProcessors].
     *
     * @param out the eventual output stream for the compressed data.
     * @throws java.io.IOException if it all goes wrong.
     */
    @JvmOverloads
    constructor(out: OutputStream?, nthreads: Int = Runtime.getRuntime().availableProcessors()) : this(
        out,
        sharedThreadPool, nthreads
    )

    /*
     * @see http://www.gzip.org/zlib/rfc-gzip.html#file-format
     */
    @Throws(IOException::class)
    private fun writeHeader() {
        out.write(
            byteArrayOf(
                GZIP_MAGIC.toByte(),  // ID1: Magic number (little-endian short)
                (GZIP_MAGIC shr 8).toByte(),  // ID2: Magic number (little-endian short)
                Deflater.DEFLATED.toByte(),  // CM: Compression method
                0,  // FLG: Flags (byte)
                0, 0, 0, 0,  // MTIME: Modification time (int)
                0,  // XFL: Extra flags
                3 // OS: Operating system (3 = Linux)
            )
        )
    }

    // Master thread only
    @Throws(IOException::class)
    override fun write(b: Int) {
        val single = ByteArray(1)
        single[0] = (b and 0xFF).toByte()
        write(single)
    }

    // Master thread only
    @Throws(IOException::class)
    override fun write(b: ByteArray) {
        write(b, 0, b.size)
    }

    // Master thread only
    @Throws(IOException::class)
    override fun write(b: ByteArray, off: Int, len: Int) {
        var off = off
        var len = len
        crc.update(b, off, len)
        bytesWritten += len
        while (len > 0) {
            // assert block.in_length < block.in.length
            val capacity = block.`in`.size - block.in_length
            if (len >= capacity) {
                System.arraycopy(b, off, block.`in`, block.in_length, capacity)
                block.in_length += capacity // == block.in.length
                off += capacity
                len -= capacity
                submit()
            } else {
                System.arraycopy(b, off, block.`in`, block.in_length, len)
                block.in_length += len
                // off += len;
                // len = 0;
                break
            }
        }
    }

    // Master thread only
    @Throws(IOException::class)
    private fun submit() {
        emitUntil(nthreads - 1)
        emitQueue.add(executor.submit(block))
        block = PGZIPBlock(this /* block.index + 1 */)
    }

    // Emit If Available - submit always
    // Emit At Least one - submit when executor is full
    // Emit All Remaining - flush(), close()
    // Master thread only
    @Throws(IOException::class, InterruptedException::class, ExecutionException::class)
    private fun tryEmit() {
        while (true) {
            val future = emitQueue.peek() ?: return
            // LOG.info("Peeked future " + future);
            if (!future.isDone) return
            // It's an ordered queue. This MUST be the same element as above.
            emitQueue.remove()
            val toWrite = future.get()
            blockSizes.add(toWrite.size) // TODO: remove after block guessing is implemented
            out.write(toWrite)
        }
    }

    // Master thread only
    /**
     * Emits any opportunistically available blocks. Furthermore, emits blocks until the number of executing tasks is less than taskCountAllowed.
     */
    @Throws(IOException::class)
    private fun emitUntil(taskCountAllowed: Int) {
        try {
            while (emitQueue.size > taskCountAllowed) {
                // LOG.info("Waiting for taskCount=" + emitQueue.size() + " -> " + taskCountAllowed);
                val future = emitQueue.remove() // Valid because emitQueue.size() > 0
                val toWrite = future.get() // Blocks until this task is done.
                blockSizes.add(toWrite.size) // TODO: remove after block guessing is implemented
                out.write(toWrite)
            }
            // We may have achieved more opportunistically available blocks
            // while waiting for a block above. Let's emit them here.
            tryEmit()
        } catch (e: ExecutionException) {
            throw IOException(e)
        } catch (e: InterruptedException) {
            throw InterruptedIOException()
        }
    }

    // Master thread only
    @Throws(IOException::class)
    override fun flush() {
        // LOG.info("Flush: " + block);
        if (block.in_length > 0) submit()
        emitUntil(0)
        super.flush()
    }

    @Throws(IOException::class)
    fun finish() {
        if (bytesWritten >= 0) {
            flush()

            newDeflaterOutputStream(out, newDeflater()).finish()

            val buf = ByteBuffer.allocate(8)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            // LOG.info("CRC is " + crc.getValue());
            buf.putInt(crc.value.toInt())
            buf.putInt(bytesWritten)
            out.write(buf.array()) // allocate() guarantees a backing array.
            // LOG.info("trailer is " + Arrays.toString(buf.array()));
        }
    }

    // Master thread only
    @Throws(IOException::class)
    override fun close() {
        // LOG.info("Closing: bytesWritten=" + bytesWritten);
        if (bytesWritten >= 0) {
            finish()

            out.flush()
            out.close()

            bytesWritten = Int.MIN_VALUE
            // } else {
            // LOG.warn("Already closed.");
        }
    }

    companion object {
        val sharedThreadPool: ExecutorService = Executors.newCachedThreadPool { t: Runnable? ->
            Thread(
                t,
                "PGZIPOutputStream#EXECUTOR"
            )
        }

        // private static final Logger LOG = LoggerFactory.getLogger(PGZIPOutputStream.class);
        private const val GZIP_MAGIC = 0x8b1f

        fun newDeflaterOutputStream(out: OutputStream, deflater: Deflater): DeflaterOutputStream {
            return DeflaterOutputStream(out, deflater, 512, true)
        }
    }
}
