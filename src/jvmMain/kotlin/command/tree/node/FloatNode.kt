package org.chorus_oss.chorus.command.tree.node


/**
 * 解析为[Float]值
 *
 *
 * 所有命令参数类型为[FLOAT][org.chorus_oss.chorus.command.data.CommandParamType.FLOAT]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class FloatNode : ParamNode<Float?>() {
    override fun fill(arg: String) {
        try {
            this.value = arg.toFloat()
        } catch (e: Exception) {
            this.error()
        }
    }
}
