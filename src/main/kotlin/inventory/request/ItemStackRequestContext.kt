package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseContainer


class ItemStackRequestContext(val itemStackRequest: org.chorus_oss.protocol.types.itemstack.request.ItemStackRequest) {
    var currentActionIndex = 0
    val extraData: MutableMap<String, Any> =
        HashMap()

    fun put(key: String, value: Any) {
        extraData[key] = value
    }

    fun has(key: String): Boolean {
        return extraData.containsKey(key)
    }

    fun <T> get(key: String): T? {
        return extraData[key] as T?
    }

    fun error(): ActionResponse {
        return ActionResponse(false, listOf())
    }

    fun success(containers: List<ItemStackResponseContainer>): ActionResponse {
        return ActionResponse(true, containers)
    }
}
