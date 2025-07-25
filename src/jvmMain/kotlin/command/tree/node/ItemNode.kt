package org.chorus_oss.chorus.command.tree.node

import org.chorus_oss.chorus.item.Item

/**
 * 解析对应参数为[Item]值
 *
 *
 * 所有命令枚举[ENUM_ITEM][org.chorus_oss.chorus.command.data.CommandEnum.ENUM_ITEM]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class ItemNode : ParamNode<Item?>() {
    override fun fill(arg: String) {
        var arg = arg
        if (arg.indexOf(':') == -1) {
            arg = "minecraft:$arg"
        }
        val item = Item.get(arg)
        if (item.isNothing) {
            error()
            return
        }
        this.value = item
    }
}
