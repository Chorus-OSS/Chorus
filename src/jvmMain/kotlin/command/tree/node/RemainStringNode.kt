package org.chorus_oss.chorus.command.tree.node

/**
 * 解析全部剩余参数拼接为`String`值
 *
 *
 * 所有命令参数类型为[JSON][org.chorus_oss.chorus.command.data.CommandParamType.JSON]的
 * 如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class RemainStringNode : ParamNode<String?>() {
    private val TMP: MutableList<String> = ArrayList()

    override fun fill(arg: String) {
        if (paramList.index != paramList.paramTree.args.size) TMP.add(arg)
        else {
            TMP.add(arg)
            this.value = java.lang.String.join("", TMP)
        }
    }

    override fun reset() {
        super.reset()
        TMP.clear()
    }
}
