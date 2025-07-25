package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.inventory.InventoryType
import org.chorus_oss.protocol.types.ContainerType

operator fun ContainerType.Companion.invoke(from: InventoryType): ContainerType {
    return ContainerType.entries.find { it.netID == from.networkType.toByte() }!!
}