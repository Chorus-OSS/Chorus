package org.chorus_oss.chorus.command

import io.netty.util.internal.EmptyArrays
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.defaults.*
import org.chorus_oss.chorus.command.simple.*
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.lang.CommandOutputContainer
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.plugin.InternalPlugin
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.TextFormat
import org.chorus_oss.chorus.utils.Utils


class SimpleCommandMap(private val server: Server) : CommandMap {
    protected val knownCommands: MutableMap<String?, Command> = HashMap()

    init {
        this.setDefaultCommands()
    }

    private fun setDefaultCommands() {
        this.register("chorus", ExecuteCommand("execute"))
        this.register("chorus", CameraCommand("camera"))
        this.register("chorus", FogCommand("fog"))
        this.register("chorus", ExecuteCommandOld("executeold"))
        this.register("chorus", PlayAnimationCommand("playanimation"))
        this.register("chorus", WorldCommand("world"))
        this.register("chorus", TpsCommand("tps"))
        this.register("chorus", TickingAreaCommand("tickingarea"))
        this.register("chorus", TellrawCommand("tellraw"))
        this.register("chorus", TitlerawCommand("titleraw"))
        this.register("chorus", FunctionCommand("function"))
        this.register("chorus", ReplaceItemCommand("replaceitem"))
        this.register("chorus", SummonCommand("summon"))
        this.register("chorus", DamageCommand("damage"))
        this.register("chorus", ClearSpawnPointCommand("clearspawnpoint"))
        this.register("chorus", AbilityCommand("ability"))
        this.register("chorus", ScoreboardCommand("scoreboard"))
        this.register("chorus", CameraShakeCommand("camerashake"))
        this.register("chorus", TagCommand("tag"))
        this.register("chorus", TestForCommand("testfor"))
        this.register("chorus", TestForBlockCommand("testforblock"))
        this.register("chorus", TestForBlocksCommand("testforblocks"))
        this.register("chorus", SpreadPlayersCommand("spreadplayers"))
        this.register("chorus", SetMaxPlayersCommand("setmaxplayers"))
        this.register("chorus", PlaySoundCommand("playsound"))
        this.register("chorus", StopSoundCommand("stopsound"))
        this.register("chorus", FillCommand("fill"))
        this.register("chorus", DayLockCommand("daylock"))
        this.register("chorus", ClearCommand("clear"))
        this.register("chorus", CloneCommand("clone"))
        this.register("chorus", VersionCommand("version"))
        this.register("chorus", PluginsCommand("plugins"))
        this.register("chorus", SeedCommand("seed"))
        this.register("chorus", HelpCommand("help"))
        this.register("chorus", StopCommand("stop"))
        this.register("chorus", TellCommand("tell"))
        this.register("chorus", DefaultGamemodeCommand("defaultgamemode"))
        this.register("chorus", BanCommand("ban"))
        this.register("chorus", BanIpCommand("ban-ip"))
        this.register("chorus", BanListCommand("banlist"))
        this.register("chorus", PardonCommand("pardon"))
        this.register("chorus", PardonIpCommand("pardon-ip"))
        this.register("chorus", SayCommand("say"))
        this.register("chorus", MeCommand("me"))
        this.register("chorus", ListCommand("list"))
        this.register("chorus", DifficultyCommand("difficulty"))
        this.register("chorus", KickCommand("kick"))
        this.register("chorus", OpCommand("op"))
        this.register("chorus", DeOpCommand("deop"))
        this.register("chorus", WhitelistCommand("whitelist"))
        this.register("chorus", SaveOnCommand("save-on"))
        this.register("chorus", SaveOffCommand("save-off"))
        this.register("chorus", SaveCommand("save-all"))
        this.register("chorus", GiveCommand("give"))
        this.register("chorus", EffectCommand("effect"))
        this.register("chorus", EnchantCommand("enchant"))
        this.register("chorus", ParticleCommand("particle"))
        this.register("chorus", GamemodeCommand("gamemode"))
        this.register("chorus", GameruleCommand("gamerule"))
        this.register("chorus", KillCommand("kill"))
        this.register("chorus", SpawnpointCommand("spawnpoint"))
        this.register("chorus", SetWorldSpawnCommand("setworldspawn"))
        this.register("chorus", TeleportCommand("tp"))
        this.register("chorus", TimeCommand("time"))
        this.register("chorus", TitleCommand("title"))
        this.register("chorus", ReloadCommand("reload"))
        this.register("chorus", WeatherCommand("weather"))
        this.register("chorus", XpCommand("xp"))
        this.register("chorus", SetBlockCommand("setblock"))
        this.register("chorus", HudCommand("hud"))

        this.register("chorus", StatusCommand("status"))
        this.register("chorus", GarbageCollectorCommand("gc"))
        if (server.settings.debugSettings.command) {
            this.register("chorus", DebugCommand("debug"))
        }
    }

    override fun registerAll(fallbackPrefix: String, commands: List<Command>) {
        for (command in commands) {
            this.register(fallbackPrefix, command)
        }
    }

    override fun register(fallbackPrefix: String, command: Command): Boolean {
        return this.register(fallbackPrefix, command, null)
    }

    override fun register(fallbackPrefix: String, command: Command, label: String?): Boolean {
        var fallbackPrefix = fallbackPrefix
        var label = label
        if (label == null) {
            label = command.name
        }
        label = label.trim { it <= ' ' }.lowercase()
        fallbackPrefix = fallbackPrefix.trim { it <= ' ' }.lowercase()

        val registered = this.registerAlias(command, false, fallbackPrefix, label)

        val aliases = mutableListOf(*command.aliases)

        val iterator = aliases.iterator()
        while (iterator.hasNext()) {
            val alias = iterator.next()
            if (!this.registerAlias(command, true, fallbackPrefix, alias)) {
                iterator.remove()
            }
        }
        command.aliases = aliases.toTypedArray()

        if (!registered) {
            command.setLabel("$fallbackPrefix:$label")
        }

        command.register(this)

        return registered
    }

    override fun registerSimpleCommands(`object`: Any) {
        for (method in `object`.javaClass.declaredMethods) {
            val def = method.getAnnotation(org.chorus_oss.chorus.command.simple.Command::class.java)
            if (def != null) {
                val sc = SimpleCommand(`object`, method, def.name, def.description, def.usageMessage, def.aliases)

                val args = method.getAnnotation(
                    Arguments::class.java
                )
                if (args != null) {
                    sc.setMaxArgs(args.max)
                    sc.setMinArgs(args.min)
                }

                val perm = method.getAnnotation(
                    CommandPermission::class.java
                )
                if (perm != null) {
                    sc.permission = perm.value
                }

                if (method.isAnnotationPresent(ForbidConsole::class.java)) {
                    sc.setForbidConsole(true)
                }

                val commandParameters = method.getAnnotation(CommandParameters::class.java)
                if (commandParameters != null) {
                    val map = commandParameters.parameters.associate {
                        it.name to it.parameters.map { param ->
                            CommandParameter.newType(
                                param.name,
                                param.optional,
                                param.type
                            )
                        }.toTypedArray()
                    }

                    sc.commandParameters.putAll(map)
                }

                this.register(def.name, sc)
            }
        }
    }

    private fun registerAlias(command: Command, isAlias: Boolean, fallbackPrefix: String, label: String?): Boolean {
        knownCommands["$fallbackPrefix:$label"] = command

        //if you're registering a command alias that is already registered, then return false
        val alreadyRegistered = knownCommands.containsKey(label)
        val existingCommand = knownCommands[label]
        val existingCommandIsNotVanilla = alreadyRegistered && existingCommand !is VanillaCommand
        //basically, if we're an alias, and it's already registered, or we're a vanilla command, then we can't override it
        if ((command is VanillaCommand || isAlias) && alreadyRegistered && existingCommandIsNotVanilla) {
            return false
        }

        //if you're registering a name (alias or label) which is identical to another command who's primary name is the same
        //so basically we can't override the main name of a command, but we can override aliases if we're not an alias

        //added the last statement which will allow us to override a VanillaCommand unconditionally
        if (alreadyRegistered && existingCommand!!.label != null && existingCommand.label == label && existingCommandIsNotVanilla) {
            return false
        }

        //you can now assume that the command is either uniquely named, or overriding another command's alias (and is not itself, an alias)
        if (!isAlias) {
            command.setLabel(label)
        }

        // Then we need to check if there isn't any command conflicts with vanilla commands
        val toRemove = ArrayList<String?>()

        for ((key, cmd) in knownCommands) {
            if (cmd.label.equals(
                    command.label,
                    ignoreCase = true
                ) && cmd != command
            ) { // If the new command conflicts... (But if it isn't the same command)
                if (cmd is VanillaCommand) { // And if the old command is a vanilla command...
                    // Remove it!
                    toRemove.add(key)
                }
            }
        }

        // Now we loop the toRemove list to remove the command conflicts from the knownCommands map
        for (cmd in toRemove) {
            knownCommands.remove(cmd)
        }

        knownCommands[label] = command

        return true
    }

    override fun executeCommand(sender: CommandSender, cmdLine: String): Int {
        val parsed = parseArguments(cmdLine)
        if (parsed.isEmpty()) {
            return -1
        }

        val sentCommandLabel = parsed.removeAt(0).lowercase() //command name
        val args = parsed.toArray(EmptyArrays.EMPTY_STRINGS)
        val target = this.getCommand(sentCommandLabel)

        if (target == null) {
            sender.sendCommandOutput(
                CommandOutputContainer(
                    TextFormat.RED.toString() + "%commands.generic.unknown",
                    arrayOf(sentCommandLabel),
                    0
                )
            )
            return -1
        }
        var output: Int
        try {
            if (target.hasParamTree()) {
                val plugin = if (target is PluginCommand<*>) target.plugin else InternalPlugin.INSTANCE
                val result = target.paramTree!!.matchAndParse(sender, sentCommandLabel, args)
                if (result == null) output = 0
                else if (target.testPermissionSilent(sender)) {
                    try {
                        output = target.execute(
                            sender,
                            sentCommandLabel,
                            result,
                            CommandLogger(target, sender, sentCommandLabel, args, result.value.messageContainer, plugin)
                        )
                    } catch (e: UnsupportedOperationException) {
                        log.error("If you use paramtree, you must override execute(CommandSender sender, String commandLabel, Map.Entry<String, ParamList> result, CommandLogger log) method to run the command!")
                        output = 0
                    }
                } else {
                    val log = CommandLogger(target, sender, sentCommandLabel, args, plugin)
                    if (target.permissionMessage == null) {
                        log.addMessage("chorus.command.generic.permission").output()
                    } else if (target.permissionMessage != "") {
                        log.addError(target.permissionMessage!!.replace("<permission>", target.permission!!)).output()
                    }
                    output = 0
                }
            } else {
                output = if (target.execute(sender, sentCommandLabel, args)) 1 else 0
            }
        } catch (e: Exception) {
            log.error(
                server.lang.tr(
                    "chorus.command.exception",
                    cmdLine,
                    target.toString(),
                    Utils.getExceptionMessage(e)
                ), e
            )
            sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.exception"))
            output = 0
        }

        return output
    }

    override fun clearCommands() {
        for (command in knownCommands.values) {
            command.unregister(this)
        }
        knownCommands.clear()
        this.setDefaultCommands()
    }

    override fun getCommand(name: String): Command? {
        var name1 = name
        name1 = name1.lowercase()
        if (knownCommands.containsKey(name1)) {
            return knownCommands[name1]
        }
        return null
    }

    val commands: Map<String?, Command>
        /**
         * 获取[.knownCommands]的未克隆实例
         *
         * @return the commands
         */
        get() = knownCommands

    companion object : Loggable {
        /**
         * 解析给定文本，从中分割参数
         *
         * @param cmdLine the cmd line
         * @return 参数数组
         */
        fun parseArguments(cmdLine: String): ArrayList<String> {
            val sb = StringBuilder(cmdLine)
            val args = ArrayList<String>()
            var notQuoted = true
            var curlyBraceCount = 0
            var start = 0

            var i = 0
            while (i < sb.length) {
                if ((sb[i] == '{' && curlyBraceCount >= 1) || (sb[i] == '{' && sb[i - 1] == ' ' && curlyBraceCount == 0)) {
                    curlyBraceCount++
                } else if (sb[i] == '}' && curlyBraceCount > 0) {
                    curlyBraceCount--
                    if (curlyBraceCount == 0) {
                        args.add(sb.substring(start, i + 1))
                        start = i + 1
                    }
                }
                if (curlyBraceCount == 0) {
                    if (sb[i] == ' ' && notQuoted) {
                        val arg = sb.substring(start, i)
                        if (!arg.isEmpty()) {
                            args.add(arg)
                        }
                        start = i + 1
                    } else if (sb[i] == '"') {
                        sb.deleteCharAt(i)
                        --i
                        notQuoted = !notQuoted
                    }
                }
                i++
            }

            val arg = sb.substring(start)
            if (!arg.isEmpty()) {
                args.add(arg)
            }
            return args
        }
    }
}
