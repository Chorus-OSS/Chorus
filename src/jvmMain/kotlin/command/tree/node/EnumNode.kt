package org.chorus_oss.chorus.command.tree.node

import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.tree.ParamList

/**
 * 解析为[String]值
 *
 *
 * 所有命令枚举类型如果没有手动指定[IParamNode],则会默认使用这个解析
 */
open class EnumNode : ParamNode<String>() {
    protected var commandEnum: CommandEnum? = null
    protected var enums: Set<String>? = null

    override fun fill(arg: String) {
        if (commandEnum!!.isSoft) {
            this.value = arg
            return
        }
        if (enums!!.contains(arg)) this.value = arg
        else this.error()
    }

    override fun init(
        parent: ParamList,
        name: String?,
        optional: Boolean,
        type: CommandParamType?,
        enumData: CommandEnum?,
        postFix: String?
    ): IParamNode<String> {
        this.paramList = parent
        this.commandEnum = enumData
        this.enums = commandEnum?.getValues()?.toSet() ?: setOf()
        this.isOptional = optional
        return this
    }
}
