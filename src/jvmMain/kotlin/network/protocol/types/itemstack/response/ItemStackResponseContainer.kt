package org.chorus_oss.chorus.network.protocol.types.itemstack.response

import org.chorus_oss.protocol.types.inventory.FullContainerName

/**
 * ContainerEntry holds information on what slots in a container have what item stack in them.
 */
data class ItemStackResponseContainer(
    /**
     * items holds information on what item stack should be present in specific slots in the container.
     */
    val items: MutableList<ItemStackResponseSlot>,
    val containerName: FullContainerName,
)
