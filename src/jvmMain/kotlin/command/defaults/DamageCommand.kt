package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.lang.TranslationContainer
import java.util.stream.Collectors

class DamageCommand(name: String) : VanillaCommand(name, "commands.damage.description") {
    init {
        this.permission = "chorus.command.damage"
        commandParameters.clear()
        this.addCommandParameters(
            "default",
            arrayOf(
                CommandParameter.Companion.newType("target", false, CommandParamType.TARGET),
                CommandParameter.Companion.newType("amount", false, CommandParamType.INT),
                CommandParameter.Companion.newEnum(
                    "cause",
                    true,
                    EntityDamageEvent.DamageCause.entries.map { e: EntityDamageEvent.DamageCause -> e.name.lowercase() }
                        .toList().toTypedArray()
                )
            )
        )
        this.addCommandParameters(
            "damager",
            arrayOf(
                CommandParameter.Companion.newType("target", false, CommandParamType.TARGET),
                CommandParameter.Companion.newType("amount", false, CommandParamType.INT),
                CommandParameter.Companion.newEnum(
                    "cause",
                    false,
                    EntityDamageEvent.DamageCause.entries.map { e: EntityDamageEvent.DamageCause -> e.name.lowercase() }
                        .toList().toTypedArray()
                ),
                CommandParameter.Companion.newEnum("entity", false, arrayOf("entity")),
                CommandParameter.Companion.newType("damager", false, CommandParamType.TARGET)
            )
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
        val entities = list.getResult<List<Entity>>(0)!!
        if (entities.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }
        val entities_str = entities.stream().map { obj: Entity -> obj.getEntityName() }.collect(Collectors.joining(" "))
        val amount = list.getResult<Int>(1)!!
        if (amount < 0) {
            log.addError("commands.damage.specify.damage").output()
            return 0
        }
        when (result.key) {
            "default" -> {
                var cause = EntityDamageEvent.DamageCause.NONE
                if (list.hasResult(2)) {
                    val str = list.getResult<String>(2)!!
                    cause = EntityDamageEvent.DamageCause.valueOf(str)
                }
                var all_success = true
                val failed: MutableList<Entity> = ArrayList()
                for (entity in entities) {
                    val event: EntityDamageEvent = EntityDamageEvent(entity, cause, amount.toFloat())
                    val success: Boolean = entity.attack(event)
                    if (!success) {
                        if (entity is EntityItem) entity.kill()
                        all_success = false
                        failed.add(entity)
                    }
                }
                if (all_success) {
                    log.addSuccess("commands.damage.success", entities_str).output()
                    return 1
                } else {
                    log.addError(
                        "commands.damage.failed", failed.stream().map { obj: Entity -> obj.getEntityName() }.collect(
                            Collectors.joining(" ")
                        )
                    ).output()
                    return 0
                }
            }

            "damager" -> {
                val str = list.getResult<String>(2)
                val cause = EntityDamageEvent.DamageCause.valueOf(str!!.uppercase())
                val damagers = list.getResult<List<Entity>>(4)!!
                if (damagers.isEmpty()) {
                    log.addNoTargetMatch().output()
                    return 0
                }
                if (damagers.size > 1) {
                    log.addError("commands.damage.tooManySources").output()
                    return 0
                }
                val damager = damagers[0]
                var all_success = true
                val failed: MutableList<Entity> = ArrayList()
                for (entity in entities) {
                    val event = EntityDamageByEntityEvent(damager, entity, cause, amount.toFloat())
                    val success: Boolean = entity.attack(event)
                    if (!success) {
                        all_success = false
                        failed.add(entity)
                    }
                }
                if (all_success) {
                    log.addSuccess("commands.damage.success", entities_str).output()
                    sender.sendMessage(TranslationContainer("", entities_str))
                    return 1
                } else {
                    log.addError(
                        "commands.damage.failed", failed.stream().map { obj: Entity -> obj.getEntityName() }.collect(
                            Collectors.joining(" ")
                        )
                    ).output()
                    return 0
                }
            }

            else -> {
                return 0
            }
        }
    }
}
