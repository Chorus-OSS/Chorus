package org.chorus_oss.chorus.command.tree.node

import java.util.regex.Pattern

/**
 * 验证是否为浮点坐标并解析为[Position][Locator]值
 *
 *
 * 所有命令参数类型为[POSITION][org.chorus_oss.chorus.command.data.CommandParamType.POSITION]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class FloatPositionNode : PositionNode(FLOAT_POS_PATTERN) {
    companion object {
        val FLOAT_POS_PATTERN: Pattern = Pattern.compile("[~^]?[-+]?\\d+(?:\\.\\d+)?|[~^]")
    }
}
