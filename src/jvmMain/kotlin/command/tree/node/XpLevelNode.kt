package org.chorus_oss.chorus.command.tree.node

/**
 * 验证经验值或等级并解析为[Integer]值
 *
 *
 * 不会默认使用，需要手动指定
 */
class XpLevelNode : ParamNode<Int?>() {
    override fun fill(arg: String) {
        if (arg.endsWith("l") || arg.endsWith("L")) {
            try {
                this.value = arg.substring(0, arg.length - 1).toInt()
            } catch (e: NumberFormatException) {
                this.error()
            }
        } else error()
    }
}
