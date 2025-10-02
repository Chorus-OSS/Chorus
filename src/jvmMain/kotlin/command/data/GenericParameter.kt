package org.chorus_oss.chorus.command.data

import org.chorus_oss.chorus.command.tree.node.ChainedCommandNode
import org.chorus_oss.chorus.command.tree.node.ItemNode

interface GenericParameter {
    fun interface CommandParameterSupplier<T> {
        fun get(optional: Boolean): T
    }

    companion object {
        val OBJECTIVES: CommandParameterSupplier<CommandParameter> =
            CommandParameterSupplier<CommandParameter> { optional: Boolean ->
                CommandParameter.newEnum(
                    "objective",
                    optional,
                    CommandEnum.SCOREBOARD_OBJECTIVES
                )
            }
        val TARGET_OBJECTIVES: CommandParameterSupplier<CommandParameter> =
            CommandParameterSupplier<CommandParameter> { optional: Boolean ->
                CommandParameter.newEnum(
                    "targetObjective",
                    optional,
                    CommandEnum.SCOREBOARD_OBJECTIVES
                )
            }
        val ITEM_NAME: CommandParameterSupplier<CommandParameter> =
            CommandParameterSupplier<CommandParameter> { optional: Boolean ->
                CommandParameter.newEnum(
                    "itemName",
                    optional,
                    CommandEnum.ENUM_ITEM,
                    ItemNode()
                )
            }
        val CHAINED_COMMAND: CommandParameterSupplier<CommandParameter> =
            CommandParameterSupplier<CommandParameter> { optional: Boolean ->
                CommandParameter.newEnum(
                    "chainedCommand",
                    optional,
                    CommandEnum.CHAINED_COMMAND_ENUM,
                    ChainedCommandNode(),
                    CommandParamOption.ENUM_AS_CHAINED_COMMAND
                )
            }
        val ORIGIN: CommandParameterSupplier<CommandParameter> =
            CommandParameterSupplier<CommandParameter> { optional: Boolean ->
                CommandParameter.newType(
                    "origin",
                    optional,
                    CommandParamType.TARGET
                )
            }
    }
}
