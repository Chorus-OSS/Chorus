package org.chorus_oss.chorus.command.tree.node

import java.util.regex.Pattern

/**
 * 验证是否为整数坐标并将参数解析为[Position][Locator]值
 *
 *
 * 所有命令参数类型为[BLOCK_POSITION][org.chorus_oss.chorus.command.data.CommandParamType.BLOCK_POSITION]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class IntPositionNode : PositionNode(INT_POS_PATTERN) {
    companion object {
        val INT_POS_PATTERN: Pattern = Pattern.compile("[~^]?([-+]?\\d+)|[~^]")
    }
}
