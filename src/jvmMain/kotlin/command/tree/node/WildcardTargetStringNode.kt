package org.chorus_oss.chorus.command.tree.node


/**
 * 解析为[String]值
 *
 *
 * 所有命令参数类型为[WILDCARD_TARGET][org.chorus_oss.chorus.command.data.CommandParamType.WILDCARD_TARGET]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class WildcardTargetStringNode : StringNode() {
    override fun fill(arg: String) {
        //WILDCARD_TARGET不可能解析错误
        this.value = arg
    }
}
