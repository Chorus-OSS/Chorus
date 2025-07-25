package org.chorus_oss.chorus.nbt.stream

class PGZIPThreadLocal(private val parent: PGZIPOutputStream) : ThreadLocal<PGZIPState>() {
    override fun initialValue(): PGZIPState {
        return PGZIPState(parent)
    }
}
