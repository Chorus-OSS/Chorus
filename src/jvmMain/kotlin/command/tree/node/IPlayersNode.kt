package org.chorus_oss.chorus.command.tree.node

import org.chorus_oss.chorus.IPlayer
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException
import org.chorus_oss.chorus.command.selector.EntitySelectorAPI
import org.chorus_oss.chorus.entity.Entity
import java.util.stream.Collectors

/**
 * 解析为`List<IPlayer>`值
 *
 *
 * 不会默认使用，需要手动指定
 */
class IPlayersNode : ParamNode<List<IPlayer?>?>() {
    override fun fill(arg: String) {
        if (arg.isBlank()) {
            this.error()
        } else if (EntitySelectorAPI.api.checkValid(arg)) {
            val entities: List<Entity>
            try {
                entities = EntitySelectorAPI.api.matchEntities(paramList.paramTree.sender!!, arg)
            } catch (exception: SelectorSyntaxException) {
                error(exception.message)
                return
            }
            val result =
                entities.stream().filter { entity: Entity? -> entity is IPlayer }
                    .map { entity: Entity -> entity as IPlayer }
                    .collect(Collectors.toList())
            if (result.isNotEmpty()) this.value = result
            else error("commands.generic.noTargetMatch")
        } else {
            val player = Server.instance.getOfflinePlayer(arg)
            if (player != null) {
                this.value = listOf(player)
            } else error("commands.generic.player.notFound")
        }
    }
}
