package org.chorus_oss.chorus.command.tree.node

import com.google.common.collect.Sets

/**
 * 验证是否为操作参数，解析对应参数为[String]值
 *
 *
 * 所有命令参数类型为[OPERATOR][org.chorus_oss.chorus.command.data.CommandParamType.OPERATOR]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class OperatorStringNode : StringNode() {
    override fun fill(arg: String) {
        if (OPERATOR.contains(arg)) this.value = arg
        else this.error()
    }

    companion object {
        private val OPERATOR: HashSet<String> = Sets.newHashSet("+=", "-=", "*=", "/=", "%=", "=", "<", ">", "><")
    }
}
