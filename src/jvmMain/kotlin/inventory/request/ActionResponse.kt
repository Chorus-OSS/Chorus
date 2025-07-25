package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseContainer

@JvmRecord
data class ActionResponse(@JvmField val ok: Boolean, @JvmField val containers: List<ItemStackResponseContainer>)
