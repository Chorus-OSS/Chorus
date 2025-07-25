package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.network.protocol.types.itemstack.request.action.ItemStackRequestActionType

interface ItemStackRequestActionProcessor<T : org.chorus_oss.protocol.types.itemstack.request.action.ItemStackRequestAction> {
    val type: ItemStackRequestActionType

    fun handle(action: T, player: Player, context: ItemStackRequestContext): ActionResponse?

    fun validateStackNetworkId(expectedSNID: Int, clientSNID: Int): Boolean {
        //若客户端发来的stackNetworkId小于0，说明客户端保证数据无误并要求遵从服务端的数据
        //这通常发生在当一个ItemStackRequest中有多个action时且多个action有相同的source/destination container
        //第一个action检查完id后后面的action就不需要重复检查了
        return clientSNID > 0 && expectedSNID != clientSNID
    }
}
