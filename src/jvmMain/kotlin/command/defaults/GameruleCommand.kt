package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.GameRules
import java.util.*

class GameruleCommand(name: String) : VanillaCommand(name, "commands.gamerule.description") {
    init {
        this.permission = "chorus.command.gamerule"
        commandParameters.clear()

        val rules: GameRules = GameRules.default
        val boolGameRules: MutableList<String> = mutableListOf()
        val intGameRules: MutableList<String> = mutableListOf()
        val floatGameRules: MutableList<String> = mutableListOf()
        val unknownGameRules: MutableList<String> = mutableListOf()

        rules.getGameRules().forEach { (rule, value) ->
            if (rule.isDeprecated) {
                return@forEach
            }
            when (value.type) {
                GameRules.Type.BOOLEAN -> boolGameRules.add(rule.name.lowercase())
                GameRules.Type.INTEGER -> intGameRules.add(rule.name.lowercase())
                GameRules.Type.FLOAT -> floatGameRules.add(rule.name.lowercase())
                else -> unknownGameRules.add(rule.name.lowercase())
            }
        }
        commandParameters["default"] = CommandParameter.Companion.EMPTY_ARRAY
        if (!boolGameRules.isEmpty()) {
            commandParameters["boolGameRules"] = arrayOf(
                CommandParameter.Companion.newEnum("rule", CommandEnum("BoolGameRule", boolGameRules)),
                CommandParameter.Companion.newEnum("value", true, CommandEnum.Companion.ENUM_BOOLEAN)
            )
        }
        if (!intGameRules.isEmpty()) {
            commandParameters["intGameRules"] = arrayOf(
                CommandParameter.Companion.newEnum("rule", CommandEnum("IntGameRule", intGameRules)),
                CommandParameter.Companion.newType("value", true, CommandParamType.INT)
            )
        }
        if (!floatGameRules.isEmpty()) {
            commandParameters["floatGameRules"] = arrayOf(
                CommandParameter.Companion.newEnum("rule", CommandEnum("FloatGameRule", floatGameRules)),
                CommandParameter.Companion.newType("value", true, CommandParamType.FLOAT)
            )
        }
        if (!unknownGameRules.isEmpty()) {
            commandParameters["unknownGameRules"] = arrayOf(
                CommandParameter.Companion.newEnum("rule", CommandEnum("UnknownGameRule", unknownGameRules)),
                CommandParameter.Companion.newType("value", true, CommandParamType.STRING)
            )
        }
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val rules: GameRules = sender.locator.level.gameRules
        val list = result.value
        val ruleStr = list.getResult<String>(0)
        if (result.key == "default") {
            val rulesJoiner = StringJoiner(", ")
            for (rule in rules.rules) {
                rulesJoiner.add(rule.name.lowercase())
            }
            log.addSuccess(rulesJoiner.toString()).output()
            return 1
        } else if (!list.hasResult(1)) {
            val gameRule = GameRule.parseString(ruleStr)
            if (gameRule.isEmpty || !rules.hasRule(gameRule.get())) {
                log.addSyntaxErrors(0).output()
                return 0
            }
            log.addSuccess(gameRule.get().gameRuleName.lowercase() + " = " + rules.getString(gameRule.get())).output()
            return 1
        }

        val optionalRule = GameRule.parseString(ruleStr)
        if (optionalRule.isEmpty) {
            log.addSyntaxErrors(0).output()
            return 0
        }
        when (result.key) {
            "boolGameRules" -> {
                val value = list.getResult<Boolean>(1)!!
                rules.setGameRule(optionalRule.get(), value)
            }

            "intGameRules" -> {
                val value = list.getResult<Int>(1)!!
                rules.setGameRule(optionalRule.get(), value)
            }

            "floatGameRules" -> {
                val value = list.getResult<Float>(1)!!
                rules.setGameRule(optionalRule.get(), value)
            }

            "unknownGameRules" -> {
                val value = list.getResult<String>(1)!!
                rules.setGameRule(optionalRule.get(), value)
            }
        }
        val str = list.getResult<Any>(1)
        log.addSuccess("commands.gamerule.success", optionalRule.get().gameRuleName.lowercase(), str.toString())
            .output()
        return 1
    }
}
