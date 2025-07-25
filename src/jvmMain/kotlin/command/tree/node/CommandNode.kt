package org.chorus_oss.chorus.command.tree.node

import org.chorus_oss.chorus.Server

/**
 * 将全部剩余的参数以空格为分隔符合并，解析为[String]值
 *
 *
 * 所有命令参数类型为[COMMAND][org.chorus_oss.chorus.command.data.CommandParamType.COMMAND]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class CommandNode : ParamNode<String?>() {
    private val TMP: MutableList<String> = ArrayList()

    private var first = true

    override fun fill(arg: String) {
        var arg = arg
        if (arg.contains(" ")) {
            arg = "\"" + arg + "\""
        }
        if (first && !Server.instance.commandMap.commands.containsKey(arg)) {
            this.error("commands.generic.unknown", arg)
            return
        }
        if (paramList.index != paramList.paramTree.args.size) {
            first = false
            TMP.add(arg)
        } else {
            TMP.add(arg)
            this.value = java.lang.String.join(" ", TMP)
            first = true
        }
    }

    override fun reset() {
        super.reset()
        TMP.clear()
    }
}
