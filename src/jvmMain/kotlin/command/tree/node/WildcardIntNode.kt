package org.chorus_oss.chorus.command.tree.node

/**
 * 代表一个可以输入通配符 * 的[IntNode],当输入通配符时，将会解析结果将变成默认值[.defaultV]
 *
 *
 * 所有命令参数类型为[WILDCARD_INT][org.chorus_oss.chorus.command.data.CommandParamType.WILDCARD_INT]如果没有手动指定[IParamNode],则会默认使用这个解析
 *
 *
 * `defaultV = Integer.MIN_VALUE`
 */
class WildcardIntNode @JvmOverloads constructor(private val defaultV: Int = Int.MIN_VALUE) :
    ParamNode<Int?>() {
    override fun fill(arg: String) {
        if (arg.length == 1 && arg[0] == '*') {
            this.value = defaultV
        } else {
            try {
                this.value = arg.toInt()
            } catch (e: NumberFormatException) {
                this.error()
            }
        }
    }
}
