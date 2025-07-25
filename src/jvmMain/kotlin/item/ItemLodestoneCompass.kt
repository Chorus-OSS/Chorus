package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.positiontracking.NamedPosition
import java.io.IOException

class ItemLodestoneCompass @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.LODESTONE_COMPASS, meta, count, "Lodestone Compass") {
    override val maxStackSize: Int
        get() = 1

    @get:Throws(IOException::class)
    @set:Throws(IOException::class)
    var trackingPosition: NamedPosition?
        get() {
            val trackingHandle = trackingHandle
            if (trackingHandle == 0) {
                return null
            }
            return Server.instance.getPositionTrackingService().getPosition(trackingHandle)
        }
        set(position) {
            if (position == null) {
                trackingHandle = 0
                return
            }
            trackingHandle =
                Server.instance.getPositionTrackingService().addOrReusePosition(position)
        }

    var trackingHandle: Int
        get() = if (hasCompoundTag()) namedTag!!.getInt("trackingHandle") else 0
        set(trackingHandle) {
            var tag = namedTag
            if (tag == null) {
                tag = CompoundTag()
            }
            tag.putInt("trackingHandle", trackingHandle)
            setCompoundTag(tag)
        }
}