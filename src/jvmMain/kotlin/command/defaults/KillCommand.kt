package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.utils.TextFormat
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Collectors

class KillCommand(name: String) : VanillaCommand(name, "commands.kill.description") {
    init {
        this.permission = ("chorus.command.kill.self;"
                + "chorus.command.kill.other")
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("player", true, CommandParamType.TARGET)
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        if (result.value.hasResult(0)) {
            if (!sender.hasPermission("chorus.command.kill.other")) {
                log.addError("chorus.command.generic.permission").output()
                return 0
            }
            var entities = result.value.getResult<MutableList<Entity>>(0)!!
            entities.removeIf { !it.isAlive() }
            if (entities.isEmpty()) {
                log.addNoTargetMatch().output()
                return 0
            }
            val creativePlayer = AtomicBoolean(false)
            entities = entities.stream().filter { entity: Entity ->
                if (entity is Player) if (entity.isCreative) {
                    creativePlayer.set(true)
                    return@filter false
                } else return@filter true
                else return@filter true
            }.toList()

            if (entities.isEmpty()) {
                if (creativePlayer.get()) log.addError(TextFormat.WHITE.toString() + "%commands.kill.attemptKillPlayerCreative")
                else log.addNoTargetMatch()
                log.output()
                return 0
            }

            for (entity in entities) {
                if (entity.getEntityName() == sender.senderName) {
                    if (!sender.hasPermission("chorus.command.kill.self")) {
                        continue
                    }
                }
                if (entity is Player) {
                    val ev = EntityDamageEvent(entity, EntityDamageEvent.DamageCause.SUICIDE, 1000000f)
                    entity.attack(ev)
                } else {
                    entity.kill()
                }
            }
            val message = entities.stream().map { obj: Entity -> obj.getEntityName() }.collect(Collectors.joining(", "))
            log.addSuccess("commands.kill.successful", message).successCount(entities.size).output(true)
            return entities.size
        } else {
            if (sender.isPlayer) {
                if (!sender.hasPermission("chorus.command.kill.self")) {
                    log.addError("chorus.command.generic.permission").output()
                    return 0
                }
                if (sender.asPlayer()!!.isCreative) {
                    log.addError("commands.kill.attemptKillPlayerCreative").output()
                    return 0
                }
                val ev = EntityDamageEvent(sender.asPlayer()!!, EntityDamageEvent.DamageCause.SUICIDE, 1000000f)
                sender.asPlayer()!!.attack(ev)
            } else {
                log.addError(
                    "commands.generic.usage", this.commandFormatTips.trimIndent()
                ).output()
                return 0
            }
            return 1
        }
    }
}
