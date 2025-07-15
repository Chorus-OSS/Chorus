package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.protocol.types.inventory.FullContainerName
import org.chorus_oss.protocol.types.itemstack.ContainerSlotType

operator fun FullContainerName.Companion.invoke(value: org.chorus_oss.chorus.network.protocol.types.inventory.FullContainerName): FullContainerName {
    return FullContainerName(
        container = ContainerSlotType.entries[value.container.ordinal],
        dynamicId = value.dynamicId
    )
}