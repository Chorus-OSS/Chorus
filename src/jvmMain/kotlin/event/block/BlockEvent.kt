package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.Event


abstract class BlockEvent(open val block: Block) : Event()
