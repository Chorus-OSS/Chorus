package org.chorus_oss.chorus.level.format

enum class ChunkState {
    NEW,
    GENERATED,
    POPULATED,
    FINISHED;

    fun canSend(): Boolean {
        return this.ordinal >= 2
    }
}
