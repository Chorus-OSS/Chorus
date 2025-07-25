package org.chorus_oss.chorus.command.tree.node

import com.google.gson.JsonSyntaxException
import org.chorus_oss.chorus.command.utils.RawText
import org.chorus_oss.chorus.network.protocol.types.CommandOutputMessage

/**
 * 解析为[RawText]值
 *
 *
 * 所有命令参数类型为[RAWTEXT][org.chorus_oss.chorus.command.data.CommandParamType.RAWTEXT]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class RawTextNode : ParamNode<RawText?>() {
    override fun fill(arg: String) {
        try {
            this.value = RawText.fromRawText(arg)
        } catch (e: JsonSyntaxException) {
            var index: Int
            var s = e.message
            s = s!!.substring(s.indexOf("column") + 7, s.indexOf("path") - 1)
            try {
                index = s.toInt()
            } catch (ignore: NumberFormatException) {
                this.error()
                return
            }
            if (index == arg.length + 1) {
                this.error(
                    CommandOutputMessage("JSON parsing error:"),
                    CommandOutputMessage(arg.substring(0, arg.length - 1) + "" + arg.substring(arg.length - 1) + "§f<<")
                )
                return
            } else if (index == 1) {
                this.error(
                    CommandOutputMessage("JSON parsing error:"),
                    CommandOutputMessage("§f>>§c" + arg[0] + arg.substring(1))
                )
                return
            }
            index -= 2
            this.error(
                CommandOutputMessage("JSON parsing error:"),
                CommandOutputMessage(
                    arg.substring(0, index) + "§f<<§c" + arg[index] + arg.substring(
                        index + 1,
                        arg.length - 1
                    )
                )
            )
        }
    }
}
