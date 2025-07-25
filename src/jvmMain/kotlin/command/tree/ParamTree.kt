package org.chorus_oss.chorus.command.tree

import org.chorus_oss.chorus.command.Command
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.PluginCommand
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.tree.node.*
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.lang.CommandOutputContainer
import org.chorus_oss.chorus.plugin.InternalPlugin

class ParamTree {
    val root: Map<String, ParamList>
    val command: Command
    var sender: CommandSender? = null
        private set
    lateinit var args: Array<String>
        private set

    /**
     * 从给定的命令中初始化命令节点树，其中每个参数类型[CommandParamType][org.chorus_oss.chorus.command.data.CommandParamType]会对应一个默认的参数节点,或者使用<br></br>
     * [CommandParameter.newType]<br></br>
     * [CommandParameter.newEnum]<br></br>
     * 初始化指定的命令节点。
     * 应该在命令构造函数中commandParameters初始化完毕后调用Command::enableParamTree()，形如<br></br>
     * <pre>
     * public TestCommand(String name) {
     * super(name, description, usage, aliases);
     * this.setPermission("chorus.command.test");
     * this.commandParameters.clear();
     * this.commandParameters.put("pos", new CommandParameter[]{
     * CommandParameter.newType("destination", CommandParamType.POSITION)
     * });
     * this.enableParamTree();
     * }
    </pre> *
     *
     * @param command the command
     */
    //todo 优化建树和遍历
    constructor(command: Command) {
        this.command = command
        val root = HashMap<String, ParamList>()
        for ((key, value) in command.commandParameters) {
            val paramList = ParamList(this)
            for (parameter in value) {
                val node = parameter.paramNode
                    ?: if (parameter.enumData == null) {
                        when (parameter.type) {
                            CommandParamType.INT -> IntNode()
                            CommandParamType.WILDCARD_INT -> WildcardIntNode()
                            CommandParamType.FLOAT -> FloatNode()
                            CommandParamType.VALUE -> DoubleNode()
                            CommandParamType.POSITION -> FloatPositionNode()
                            CommandParamType.BLOCK_POSITION -> IntPositionNode()
                            CommandParamType.TARGET -> EntitiesNode()
                            CommandParamType.WILDCARD_TARGET -> WildcardTargetStringNode()
                            CommandParamType.STRING, CommandParamType.TEXT, CommandParamType.FILE_PATH -> StringNode()
                            CommandParamType.COMMAND -> CommandNode()
                            CommandParamType.OPERATOR -> OperatorStringNode()
                            CommandParamType.COMPARE_OPERATOR -> CompareOperatorStringNode()
                            CommandParamType.MESSAGE -> MessageStringNode()
                            CommandParamType.JSON -> RemainStringNode()
                            CommandParamType.RAWTEXT -> RawTextNode()
                            CommandParamType.BLOCK_STATES -> BlockStateNode()
                            else -> VoidNode()
                        }
                    } else {
                        when (parameter.enumData) {
                            CommandEnum.ENUM_BOOLEAN -> {
                                BooleanNode()
                            }

                            CommandEnum.ENUM_ITEM -> {
                                ItemNode()
                            }

                            CommandEnum.ENUM_BLOCK -> {
                                BlockNode()
                            }

                            CommandEnum.ENUM_ENTITY -> {
                                StringNode()
                            }

                            else -> EnumNode()
                        }
                    }
                paramList.add(
                    node.init(
                        paramList,
                        parameter.name,
                        parameter.optional,
                        parameter.type,
                        parameter.enumData,
                        parameter.postFix
                    )
                )
            }
            root[key] = paramList
        }
        this.root = root
    }

    constructor(command: Command, root: Map<String, ParamList>) {
        this.root = root
        this.command = command
    }

    /**
     * 从给定输入参数匹配该命令节点树对应命令的命令重载，并且解析对应参数。<br></br>
     * 返回值是一个[Map.Entry],它是成功匹配的命令重载，对应[]  commandParameters}。<br></br>
     * 其Key对应commandParameters中的Key,值是一个[ParamList] 其中每个节点与commandParameters的Value一一对应，并且是解析之后的结果。
     *
     * @param sender 命令发送者
     * @param args   命令的参数
     */
    fun matchAndParse(
        sender: CommandSender,
        commandLabel: String?,
        args: Array<String>
    ): Map.Entry<String, ParamList>? {
        this.args = args
        this.sender = sender
        var result: Map.Entry<String, ParamList>? = null
        val error = ArrayList<ParamList>()

        for ((key, list) in this.root) {
            list.reset()
            f2@ for (i in list.indices) {
                list.nodeIndex = i
                val node = list[i]
                while (!node.hasResult()) {
                    if (list.index >= args.size) { //参数用完
                        if (node.isOptional) break@f2
                        list.indexAndIncrement
                        node.error()
                        break@f2
                    }
                    node.fill(args[list.indexAndIncrement])
                    if (list.error != Int.MIN_VALUE) {
                        break@f2
                    }
                }
            }
            if (list.error == Int.MIN_VALUE) {
                if (list.index < args.size) { //没用完参数
                    list.indexAndIncrement
                    list.error()
                    error.add(list)
                } else {
                    result = java.util.Map.entry(key, list) //成功条件 命令链与参数长度相等 命令链必选参数全部有结果
                    break
                }
            } else {
                error.add(list)
            }
        }

        if (result == null) { //全部不成功
            val list = error.stream().max(Comparator.comparingInt { obj: ParamList -> obj.error }).orElseGet {
                val defaultList = ParamList(this)
                defaultList.error()
                defaultList
            }

            val log = CommandLogger(
                this.command, sender, commandLabel, args, CommandOutputContainer(),
                if (command is PluginCommand<*>) command.plugin else InternalPlugin.INSTANCE
            )
            if (list.messageContainer.messages.isNotEmpty()) {
                for (message in list.messageContainer.messages) {
                    log.addMessage(message.messageId, *message.parameters)
                }
            } else {
                log.addSyntaxErrors(list.error)
            }
            log.output()
            return null
        } else {
            return result
        }
    }
}
