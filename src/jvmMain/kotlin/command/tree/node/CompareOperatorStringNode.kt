package org.chorus_oss.chorus.command.tree.node

import com.google.common.collect.Sets

/**
 * 负责解析ExecuteCommand中的比较操作，解析为[String]值
 *
 *
 * 所有命令参数类型为[COMPARE_OPERATOR][org.chorus_oss.chorus.command.data.CommandParamType.COMPARE_OPERATOR]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class CompareOperatorStringNode : StringNode() {
    override fun fill(arg: String) {
        if (COMPARE_OPERATOR.contains(arg)) this.value = arg
        else this.error()
    }

    companion object {
        private val COMPARE_OPERATOR: HashSet<String> = Sets.newHashSet("<", "<=", "=", ">=", ">")
    }
}
