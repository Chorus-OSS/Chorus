package org.chorus_oss.chorus.command.tree.node

import java.util.function.Predicate
import java.util.regex.Pattern

/**
 * 验证IP地址并解析为[String]值
 *
 *
 * 不会默认使用，需要手动指定
 */
class IPStringNode : StringNode() {
    override fun fill(arg: String) {
        if (IP_PREDICATE.test(arg)) this.value = arg
        else this.error()
    }

    companion object {
        private val IP_PREDICATE: Predicate<String> =
            Pattern.compile("^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$")
                .asPredicate()
    }
}
