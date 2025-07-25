package org.chorus_oss.chorus.event.level

import org.chorus_oss.chorus.level.format.IChunk


abstract class ChunkEvent(val chunk: IChunk) : LevelEvent(chunk.provider.level)
