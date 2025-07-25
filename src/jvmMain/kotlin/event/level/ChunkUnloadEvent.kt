package org.chorus_oss.chorus.event.level

import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.format.IChunk


class ChunkUnloadEvent(chunk: IChunk) : ChunkEvent(chunk), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
