package org.chorus_oss.chorus.event.level

import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.format.IChunk


class ChunkLoadEvent(chunk: IChunk, val isNewChunk: Boolean) : ChunkEvent(chunk) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
