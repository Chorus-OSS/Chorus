package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Loggable
import java.io.IOException
import java.util.*

class BlockEntityLodestone(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    override fun loadNBT() {
        super.loadNBT()
        if (namedTag.containsInt("trackingHandler")) {
            namedTag.put("trackingHandle", namedTag.removeAndGet("trackingHandler")!!)
        }
    }

    val trackingHandler: OptionalInt
        get() {
            if (namedTag.containsInt("trackingHandle")) {
                return OptionalInt.of(namedTag.getInt("trackingHandle"))
            }
            return OptionalInt.empty()
        }

    @Throws(IOException::class)
    fun requestTrackingHandler(): Int {
        val opt = trackingHandler
        val positionTrackingService = Server.instance.getPositionTrackingService()
        val floor = this.clone()
        if (opt.isPresent) {
            val handler = opt.asInt
            val position = positionTrackingService.getPosition(handler)
            if (position != null && position.matchesNamedPosition(floor)) {
                return handler
            }
        }

        val handler = positionTrackingService.addOrReusePosition(floor)
        namedTag.putInt("trackingHandle", handler)
        return handler
    }

    override val isBlockEntityValid: Boolean
        get() = levelBlock.id === BlockID.LODESTONE

    override fun onBreak(isSilkTouch: Boolean) {
        val handlers: MutableList<Int>
        val positionTrackingService = Server.instance.getPositionTrackingService()
        try {
            handlers = positionTrackingService.findTrackingHandlers(this)
            if (handlers.isEmpty()) {
                return
            }
        } catch (e: IOException) {
            log.error("Failed to remove the tracking position handler for {}", locator)
            return
        }

        val size = handlers.size
        for (i in 0..<size) {
            val handler = handlers[i]
            try {
                positionTrackingService.invalidateHandler(handler)
            } catch (e: IOException) {
                log.error(
                    "Failed to remove the tracking handler {} for position {}", handler,
                    locator, e
                )
            }
        }
    }

    companion object : Loggable
}
