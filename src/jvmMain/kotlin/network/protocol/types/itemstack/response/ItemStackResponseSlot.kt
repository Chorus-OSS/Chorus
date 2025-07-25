package org.chorus_oss.chorus.network.protocol.types.itemstack.response

/**
 * ItemEntry holds information on what item stack should be present in a specific slot.
 */
data class ItemStackResponseSlot(
    val slot: Int,
    val hotbarSlot: Int,
    val count: Int,
    /**
     * stackNetworkID is the network ID of the new stack at a specific slot.
     */
    val stackNetworkId: Int,
    /**
     * Holds the final custom name of a renamed item, if relevant.
     *
     * @since v422
     */
    val customName: String,
    /** @since v766 */
    val filteredCustomName: String,
    /** @since v428 */
    val durabilityCorrection: Int
) {
    constructor(
        requestedSlot: Int,
        slot: Int,
        amount: Int,
        stackNetworkId: Int,
        customName: String,
        durabilityCorrection: Int
    ) : this(requestedSlot, slot, amount, stackNetworkId, customName, "", durabilityCorrection)
}