package org.chorus_oss.chorus.command.tree.node


/**
 * 解析为[String]值
 *
 *
 * 所有命令参数类型为[TEXT][org.chorus_oss.chorus.command.data.CommandParamType.TEXT] , [STRING][org.chorus_oss.chorus.command.data.CommandParamType.STRING] ,
 * [FILE_PATH][org.chorus_oss.chorus.command.data.CommandParamType.FILE_PATH]的
 * 如果没有手动指定[IParamNode],则会默认使用这个解析
 */
open class StringNode : ParamNode<String?>() {
    override fun fill(arg: String) {
        this.value = arg
    }
}
