package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.selector.args.impl.Type
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.registry.Registries

class SummonCommand(name: String) : VanillaCommand(name, "commands.summon.description") {
    init {
        this.permission = "chorus.command.summon"
        commandParameters.clear()
        val entity_key: MutableList<String> = ArrayList()
        for (key in Registries.ENTITY.knownEntities.keys) {
            entity_key.add(key.replace("minecraft:", ""))
        }
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newEnum("entityType", false, entity_key.toTypedArray<String>(), true),
            CommandParameter.Companion.newType("spawnPos", true, CommandParamType.POSITION),
            CommandParameter.Companion.newType("nameTag", true, CommandParamType.STRING),
            CommandParameter.Companion.newEnum("nameTagAlwaysVisible", true, CommandEnum.Companion.ENUM_BOOLEAN)
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val list = result.value
        val entityType = completionPrefix(list.getResult(0)!!)
        if (entityType == "minecraft:player") {
            log.addError("commands.summon.failed").output()
            return 0
        }
        val entityId = Type.Companion.ENTITY_TYPE2ID[entityType]
        var pos: Locator? = sender.locator
        if (list.hasResult(1)) {
            pos = list.getResult(1)
        }
        if (!pos!!.level.isYInRange(pos.position.y.toInt()) || !pos.chunk.isLoaded) {
            log.addError("commands.summon.outOfWorld").output()
            return 0
        }
        var nameTag: String? = null
        if (list.hasResult(2)) {
            nameTag = list.getResult(2)
        }
        var nameTagAlwaysVisible = false
        if (list.hasResult(3)) {
            nameTagAlwaysVisible = list.getResult(3)!!
        }
        val entity = if (entityId != null) {
            //原版生物
            Entity.createEntity(entityId, pos)
        } else {
            //自定义生物
            Entity.createEntity(entityType, pos)
        }
        if (entity == null) {
            log.addError("commands.summon.failed").output()
            return 0
        }
        if (nameTag != null) {
            entity.setNameTag(nameTag)
            entity.setNameTagAlwaysVisible(nameTagAlwaysVisible)
        }
        entity.spawnToAll()
        log.addSuccess("commands.summon.success").output()
        return 1
    }

    protected fun completionPrefix(type: String): String {
        val completed = if (type.contains(":")) type else "minecraft:$type"
        if (!Type.Companion.ENTITY_TYPE2ID.containsKey(type) && !Type.Companion.ENTITY_TYPE2ID.containsKey(completed)) {
            //是自定义生物，不需要补全
            return type
        }
        return completed
    }
}
