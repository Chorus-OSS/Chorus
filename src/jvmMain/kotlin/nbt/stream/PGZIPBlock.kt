package org.chorus_oss.chorus.nbt.stream

import java.util.concurrent.Callable

class PGZIPBlock(parent: PGZIPOutputStream) : Callable<ByteArray> {
    /**
     * This ThreadLocal avoids the recycling of a lot of memory, causing lumpy performance.
     */
    protected val STATE: ThreadLocal<PGZIPState> = PGZIPThreadLocal(parent)

    // private final int index;
    val `in`: ByteArray = ByteArray(SIZE)
    var in_length: Int = 0

    /*
    public Block(@Nonnegative int index) {
    this.index = index;
    }
    */
    // Only on worker thread
    @Throws(Exception::class)
    override fun call(): ByteArray {
        val state = STATE.get()
        // ByteArrayOutputStream buf = new ByteArrayOutputStream(in.length);   // Overestimate output size required.
        // DeflaterOutputStream def = newDeflaterOutputStream(buf);
        state.def.reset()
        state.buf.reset()
        state.str.write(`in`, 0, in_length)
        state.str.flush()

        // return Arrays.copyOf(in, in_length);
        return state.buf.toByteArray()
    }

    override fun toString(): String {
        return "Block" /* + index */ + "(" + in_length + "/" + `in`.size + " bytes)"
    }

    companion object {
        const val SIZE: Int = 64 * 1024
    }
}