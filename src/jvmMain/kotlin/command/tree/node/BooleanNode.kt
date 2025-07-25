package org.chorus_oss.chorus.command.tree.node

import com.google.common.collect.Sets
import org.chorus_oss.chorus.command.data.CommandEnum

/**
 * 解析对应参数为[Boolean]值
 *
 *
 * 所有命令枚举[ENUM_BOOLEAN][org.chorus_oss.chorus.command.data.CommandEnum.ENUM_BOOLEAN]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class BooleanNode : ParamNode<Boolean?>() {
    override fun fill(arg: String) {
        if (ENUM_BOOLEAN.contains(arg)) this.value = arg.toBoolean()
        else this.error()
    }

    companion object {
        private val ENUM_BOOLEAN: Set<String?> =
            Sets.newHashSet<String?>(CommandEnum.Companion.ENUM_BOOLEAN.getValues())
    }
}
