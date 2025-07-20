package org.chorus_oss.chorus.command

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.blockentity.ICommandBlock
import org.chorus_oss.chorus.command.data.*
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.ParamTree
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.lang.PluginI18nManager
import org.chorus_oss.chorus.lang.TextContainer
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.plugin.InternalPlugin
import org.chorus_oss.chorus.plugin.Plugin
import org.chorus_oss.chorus.plugin.PluginBase
import org.chorus_oss.chorus.utils.TextFormat
import java.util.stream.Collectors
import kotlin.math.min


abstract class Command @JvmOverloads constructor(
    name: String,
    description: String = "",
    usageMessage: String? = null,
    aliases: Array<String> = emptyArray()
) {
    @JvmField
    val name: String

    private var nextLabel: String?

    var label: String?
        private set

    var aliases: Array<String> = emptyArray()
        set(value) {
            field = value
            if (!this.isRegistered) {
                this.activeAliases = value
            }
        }

    private var activeAliases: Array<String>

    private var commandMap: CommandMap? = null

    @JvmField
    var description: String

    var usage: String

    @JvmField
    var permission: String? = null

    @JvmField
    var permissionMessage: String? = null

    var commandParameters: MutableMap<String, Array<CommandParameter>> = HashMap()


    var paramTree: ParamTree? = null
        protected set

    /**
     * Returns an CommandData containing command data
     *
     * @return CommandData
     */
    var defaultCommandData: CommandData
        protected set

    var isServerSideOnly: Boolean = false
        protected set

    init {
        this.defaultCommandData = CommandData()
        this.name = name.lowercase() // Uppercase letters crash the client?!?
        this.nextLabel = name
        this.label = name
        this.description = description
        this.usage = usageMessage ?: "/$name"
        this.aliases = aliases
        this.activeAliases = aliases
        commandParameters["default"] =
            arrayOf<CommandParameter>(
                CommandParameter.Companion.newType(
                    "args",
                    true,
                    CommandParamType.RAWTEXT
                )
            )
    }

    fun getCommandParameters(key: String): Array<CommandParameter> {
        return commandParameters[key]!!
    }

    fun addCommandParameters(key: String, parameters: Array<CommandParameter>) {
        commandParameters[key] = parameters
    }

    /**
     * Generates modified command data for the specified player
     * for AvailableCommandsPacket.
     *
     * @param player player
     * @return CommandData|null
     */
    fun generateCustomCommandData(player: Player): CommandDataVersions? {
        if (!this.testPermission(player)) {
            return null
        }

        val plugin: Plugin = if (this is PluginCommand<*>) this.plugin else InternalPlugin.INSTANCE

        val customData = defaultCommandData.clone()

        if (aliases.isNotEmpty()) {
            val aliases = aliases.toMutableList()
            if (!aliases.contains(this.name)) {
                aliases.add(this.name)
            }

            customData.aliases = CommandEnum(this.name + "Aliases", aliases)
        }

        if (plugin === InternalPlugin.INSTANCE) {
            customData.description =
                Server.instance.lang.tr(this.description, emptyArray<String>(), "commands.", false)
        } else if (plugin is PluginBase) {
            val i18n = PluginI18nManager.getI18n(plugin)
            if (i18n != null) {
                customData.description = i18n.tr(player.languageCode, this.description)
            } else {
                customData.description = Server.instance.lang.tr(this.description)
            }
        }

        commandParameters.forEach { (key, params) ->
            val overload = CommandOverload()
            overload.input.parameters = params
            customData.overloads[key] = overload
        }

        if (customData.overloads.isEmpty()) {
            customData.overloads["default"] = CommandOverload()
        }

        val versions = CommandDataVersions()
        versions.versions.add(customData)
        return versions
    }

    val overloads: Map<String, CommandOverload>
        get() = defaultCommandData.overloads

    open fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
        throw UnsupportedOperationException()
    }

    /**
     * Execute int.
     *
     * @param sender       命令发送者
     * @param commandLabel the command label
     * @param result       解析的命令结果
     * @param log          命令输出工具
     * @return int 返回0代表执行失败, 返回大于等于1代表执行成功
     */
    open fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        throw UnsupportedOperationException()
    }

    fun testPermission(target: CommandSender): Boolean {
        if (this.testPermissionSilent(target)) {
            return true
        }

        if (this.permissionMessage == null) {
            target.sendMessage(
                TranslationContainer(
                    TextFormat.RED.toString() + "%commands.generic.unknown",
                    this.name
                )
            )
        } else if (!permissionMessage!!.isEmpty()) {
            target.sendMessage(
                permissionMessage!!.replace(
                    "<permission>",
                    permission!!
                )
            )
        }

        return false
    }

    open fun testPermissionSilent(target: CommandSender): Boolean {
        if (this.permission == null || permission!!.isEmpty()) {
            return true
        }

        val permissions =
            permission!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (permission in permissions) {
            if (target.hasPermission(permission)) {
                return true
            }
        }

        return false
    }

    fun setLabel(name: String?): Boolean {
        this.nextLabel = name
        if (!this.isRegistered) {
            this.label = name
            return true
        }
        return false
    }

    fun register(commandMap: CommandMap): Boolean {
        if (this.allowChangesFrom(commandMap)) {
            this.commandMap = commandMap
            return true
        }
        return false
    }

    fun unregister(commandMap: CommandMap): Boolean {
        if (this.allowChangesFrom(commandMap)) {
            this.commandMap = null
            this.activeAliases = this.aliases
            this.label = this.nextLabel
            return true
        }
        return false
    }

    fun allowChangesFrom(commandMap: CommandMap): Boolean {
        return !isRegistered || this.commandMap == commandMap
    }

    val isRegistered: Boolean
        get() = this.commandMap != null

    val commandFormatTips: String
        get() {
            val builder = StringBuilder()
            for (form in commandParameters.keys) {
                val commandParametersValue = commandParameters[form]!!
                builder.append("- /" + this.name)
                for (commandParameter in commandParametersValue) {
                    if (!commandParameter.optional) {
                        if (commandParameter.enumData == null) {
                            builder.append(" <")
                                .append(commandParameter.name + ": " + commandParameter.type!!.name.lowercase())
                                .append(">")
                        } else {
                            builder.append(" <").append(
                                commandParameter.enumData.getValues().subList(
                                    0,
                                    min(commandParameter.enumData.getValues().size.toDouble(), 10.0)
                                        .toInt()
                                ).stream().collect(Collectors.joining("|"))
                            ).append(if (commandParameter.enumData.getValues().size > 10) "|..." else "").append(">")
                        }
                    } else {
                        if (commandParameter.enumData == null) {
                            builder.append(" [")
                                .append(commandParameter.name + ": " + commandParameter.type!!.name.lowercase())
                                .append("]")
                        } else {
                            builder.append(" [").append(
                                commandParameter.enumData.getValues().subList(
                                    0,
                                    min(commandParameter.enumData.getValues().size.toDouble(), 10.0)
                                        .toInt()
                                ).stream().collect(Collectors.joining("|"))
                            ).append(if (commandParameter.enumData.getValues().size > 10) "|..." else "").append("]")
                        }
                    }
                }
                builder.append("\n")
            }
            return builder.toString()
        }

    fun hasParamTree(): Boolean {
        return this.paramTree != null
    }

    /**
     * 若调用此方法，则将启用ParamTree用于解析命令参数
     */
    fun enableParamTree() {
        this.paramTree = ParamTree(this)
    }

    override fun toString(): String {
        return this.name
    }

    companion object {
        @JvmOverloads
        fun broadcastCommandMessage(source: CommandSender, message: String, sendToSource: Boolean = true) {
            val users =
                Server.instance.pluginManager.getPermissionSubscriptions(Server.BROADCAST_CHANNEL_ADMINISTRATIVE)

            val result = TranslationContainer("chat.type.admin", source.senderName, message)

            val colored = TranslationContainer(
                TextFormat.GRAY.toString() + "" + TextFormat.ITALIC + "%chat.type.admin",
                source.senderName,
                message
            )

            if (sendToSource && source !is ConsoleCommandSender) {
                source.sendMessage(message)
            }

            for (user in users) {
                if (user is CommandSender) {
                    if (user is ConsoleCommandSender) {
                        user.sendMessage(result)
                    } else if (user != source) {
                        user.sendMessage(colored)
                    }
                }
            }
        }

        @JvmOverloads
        fun broadcastCommandMessage(source: CommandSender, message: TextContainer, sendToSource: Boolean = true) {
            if ((source is ICommandBlock && !source.locator.level.gameRules
                    .getBoolean(GameRule.COMMAND_BLOCK_OUTPUT)) ||
                (source is ExecutorCommandSender && source.getExecutor() is ICommandBlock && !source.locator.level
                    .gameRules.getBoolean(GameRule.COMMAND_BLOCK_OUTPUT))
            ) {
                return
            }

            val m: TextContainer = message.clone()
            val resultStr =
                "[" + source.senderName + ": " + (if (m.text != Server.instance.lang.tr(m.text)) "%" else "") + m.text + "]"

            val users =
                Server.instance.pluginManager.getPermissionSubscriptions(Server.BROADCAST_CHANNEL_ADMINISTRATIVE)

            val coloredStr = TextFormat.GRAY.toString() + "" + TextFormat.ITALIC + resultStr

            m.text = (resultStr)
            val result: TextContainer = m.clone()
            m.text = (coloredStr)
            val colored: TextContainer = m.clone()

            if (sendToSource && source !is ConsoleCommandSender) {
                source.sendMessage(message)
            }

            for (user in users) {
                if (user is CommandSender) {
                    if (user is ConsoleCommandSender) {
                        user.sendMessage(result)
                    } else if (user != source) {
                        user.sendMessage(colored)
                    }
                }
            }
        }
    }
}
