package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.Command
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.InstantEffect
import org.chorus_oss.chorus.entity.item.EntityItem

class EffectCommand(name: String) : Command(name, "commands.effect.description", "chorus.command.effect.usage") {
    init {
        this.permission = "chorus.command.effect"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.Companion.newType("player", CommandParamType.TARGET),
            CommandParameter.Companion.newEnum("effect", CommandEnum.Companion.ENUM_EFFECT),
            CommandParameter.Companion.newType("seconds", true, CommandParamType.INT),
            CommandParameter.Companion.newType("amplifier", true, CommandParamType.INT),
            CommandParameter.Companion.newEnum("hideParticle", true, CommandEnum.Companion.ENUM_BOOLEAN)
        )
        commandParameters["clear"] = arrayOf(
            CommandParameter.Companion.newType("player", CommandParamType.TARGET),
            CommandParameter.Companion.newEnum("clear", CommandEnum("ClearEffects", "clear"))
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
        var entities = list.getResult<List<Entity>>(0)!!
        entities = entities.stream().filter { e: Entity? -> e !is EntityItem }.toList()
        if (entities.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }
        when (result.key) {
            "default" -> {
                val str = list.getResult<String>(1)!!

                val effect: Effect = Effect.get(str)

                var duration = 300
                var amplification = 0
                if (list.hasResult(2)) {
                    duration = list.getResult(2)!!
                    if (effect !is InstantEffect) {
                        duration *= 20
                    }
                } else if (effect is InstantEffect) {
                    duration = 1
                }
                if (duration < 0) {
                    log.addNumTooSmall(2, 0).output()
                    return 0
                }

                if (list.hasResult(3)) {
                    amplification = list.getResult(3)!!
                }
                if (amplification < 0) {
                    log.addNumTooSmall(3, 0).output()
                    return 0
                }

                if (list.hasResult(4)) {
                    val v = list.getResult<Boolean>(4)!!
                    effect.setVisible(!v)
                }

                var success = 0
                for (entity in entities) {
                    if (duration == 0) {
                        if (!entity.hasEffect(effect.getType())) {
                            log.addError("commands.effect.failure.notActive", effect.getName(), entity.getEntityName())
                                .output()
                            continue
                        }
                        entity.removeEffect(effect.getType())
                        log.addSuccess("commands.effect.success.removed", effect.getName(), entity.getEntityName())
                            .output()
                    } else {
                        effect.setDuration(duration).setAmplifier(amplification)
                        entity.addEffect(effect.clone())
                        log.addSuccess(
                            "%commands.effect.success",
                            effect.getName(),
                            effect.getAmplifier().toString(),
                            entity.getEntityName(),
                            (effect.getDuration() / 20).toString()
                        )
                            .output(true)
                    }
                    success++
                }
                return success
            }

            "clear" -> {
                var success = 0
                for (entity in entities) {
                    if (entity.getEffects().isEmpty()) {
                        log.addError("commands.effect.failure.notActive.all", entity.getEntityName())
                        continue
                    }
                    for (effect in entity.getEffects().values) {
                        entity.removeEffect(effect.getType())
                    }
                    success++
                    log.addSuccess("commands.effect.success.removed.all", entity.getEntityName())
                }
                log.successCount(success).output()
                return success
            }

            else -> {
                return 0
            }
        }
    }
}
