package org.chorus_oss.chorus.command.tree.node

/**
 * 解析为[Integer][Locator]值
 *
 *
 * 所有命令参数类型为[INT][org.chorus_oss.chorus.command.data.CommandParamType.INT]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class IntNode : ParamNode<Int?>() {
    override fun fill(arg: String) {
        try {
            this.value = arg.toInt()
        } catch (e: NumberFormatException) {
            this.error()
        }
    }
}
