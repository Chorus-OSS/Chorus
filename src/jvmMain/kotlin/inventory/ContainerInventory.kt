package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.AdventureSettings
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.types.BlockPos
import org.chorus_oss.protocol.types.ContainerType
import kotlin.math.floor
import kotlin.math.min

abstract class ContainerInventory(holder: InventoryHolder, type: InventoryType, size: Int) :
    BaseInventory(holder, type, size) {

    override fun onOpen(who: Player) {
        if (!who.adventureSettings[AdventureSettings.Type.OPEN_CONTAINERS]) return
        super.onOpen(who)
        who.sendPacket(
            org.chorus_oss.protocol.packets.ContainerOpenPacket(
                containerID = who.getWindowId(this).toByte(),
                containerType = ContainerType(type),
                position = BlockPos(holder.vector3),
                targetActorID = when (type) {
                    InventoryType.CONTAINER -> -1
                    else -> who.getUniqueID()
                }
            )
        )
        this.sendContents(who)

        if (canCauseVibration() && holder is Vector3) {
            who.level!!.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    who,
                    (holder as Vector3).add(0.5, 0.5, 0.5),
                    VibrationType.CONTAINER_OPEN
                )
            )
        }
    }

    override fun onClose(who: Player) {
        if (canCauseVibration() && holder is Vector3) {
            who.level!!.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    who,
                    holder.vector3.add(0.5, 0.5, 0.5),
                    VibrationType.CONTAINER_CLOSE
                )
            )
        }
        super.onClose(who)
    }

    /**
     * 若返回为true,则在inventory打开和关闭时会发生振动事件 (InventoryHolder为Vector3子类的前提下)
     *
     * @return boolean
     */
    open fun canCauseVibration(): Boolean {
        return false
    }

    companion object {
        @JvmStatic
        fun calculateRedstone(inv: Inventory?): Int {
            if (inv == null) {
                return 0
            } else {
                var itemCount = 0
                var averageCount = 0f

                for (slot in 0..<inv.size) {
                    val item = inv.getItem(slot)

                    if (!item.isNothing) {
                        averageCount += item.getCount().toFloat() / min(
                            inv.maxStackSize.toDouble(),
                            item.maxStackSize.toDouble()
                        ).toFloat()
                        ++itemCount
                    }
                }

                averageCount /= inv.size.toFloat()
                return floor(averageCount * 14).toInt() + (if (itemCount > 0) 1 else 0)
            }
        }
    }
}
