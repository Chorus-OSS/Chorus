package org.chorus_oss.chorus.command.tree.node

/**
 * 解析为[Double]值
 *
 *
 * 所有命令参数类型为[VALUE][org.chorus_oss.chorus.command.data.CommandParamType.VALUE]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class DoubleNode : ParamNode<Double?>() {
    override fun fill(arg: String) {
        try {
            this.value = arg.toDouble()
        } catch (e: Exception) {
            this.error()
        }
    }
}
