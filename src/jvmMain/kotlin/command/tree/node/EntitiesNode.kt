package org.chorus_oss.chorus.command.tree.node

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException
import org.chorus_oss.chorus.command.selector.EntitySelectorAPI
import org.chorus_oss.chorus.entity.Entity

/**
 * 解析为`List<Entity>`值
 *
 *
 * 所有命令参数类型为[TARGET][org.chorus_oss.chorus.command.data.CommandParamType.TARGET]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class EntitiesNode : TargetNode<Entity?>() {
    //todo 支持uuid 或者 xuid
    override fun fill(arg: String) {
        val entities: MutableList<Entity>
        if (arg.isBlank()) {
            this.error()
        } else if (EntitySelectorAPI.Companion.api.checkValid(arg)) {
            try {
                entities = EntitySelectorAPI.api.matchEntities(paramList.paramTree.sender!!, arg)
            } catch (exception: SelectorSyntaxException) {
                error(exception.message)
                return
            }
            this.value = entities
        } else {
            entities = mutableListOf()
            val player = Server.instance.getPlayer(arg)
            if (player != null) {
                entities.add(player)
            }
            this.value = entities
        }
    }
}
