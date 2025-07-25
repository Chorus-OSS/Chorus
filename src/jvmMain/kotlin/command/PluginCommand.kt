package org.chorus_oss.chorus.command

import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.plugin.Plugin


class PluginCommand<T : Plugin> : Command, PluginIdentifiableCommand {
    private val owningPlugin: T

    private var executor: CommandExecutor

    constructor(name: String, owner: T) : super(name) {
        this.owningPlugin = owner
        this.executor = owner
        this.usage = ""
    }

    constructor(name: String, description: String, owner: T) : super(name, description) {
        this.owningPlugin = owner
        this.executor = owner
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        if (!owningPlugin.isEnabled) {
            return 0
        }
        return 1
    }

    override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
        if (!owningPlugin.isEnabled) {
            return false
        }

        if (!this.testPermission(sender)) {
            return false
        }

        val success = executor.onCommand(sender, this, commandLabel, args)

        if (!success && usage.isNotEmpty()) {
            sender.sendMessage(TranslationContainer("commands.generic.usage", this.usage))
        }

        return success
    }

    fun getExecutor(): CommandExecutor {
        return executor
    }

    fun setExecutor(executor: CommandExecutor?) {
        this.executor = executor ?: this.owningPlugin
    }

    override val plugin: Plugin
        get() = this.owningPlugin
}
