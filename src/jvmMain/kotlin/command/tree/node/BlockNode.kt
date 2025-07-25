package org.chorus_oss.chorus.command.tree.node

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.registry.Registries

/**
 * 解析对应参数为[Block]值
 *
 *
 * 所有命令枚举[ENUM_BLOCK][CommandEnum.ENUM_BLOCK]如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class BlockNode : ParamNode<Block?>() {
    override fun fill(arg: String) {
        var arg = arg
        arg = if (arg.startsWith("minecraft:")) arg else if (arg.contains(":")) arg else "minecraft:$arg"
        var block = Registries.BLOCK[arg]
        if (block == null) {
            arg = mappingLegacyBlock(arg)
        }
        block = Registries.BLOCK[arg]
        if (block == null) {
            this.error()
            return
        }
        this.value = block
    }

    fun mappingLegacyBlock(name: String): String {
        return when (name) {
            "minecraft:stone_slab" -> "minecraft:stone_block_slab"
            "minecraft:stone_slab2" -> "minecraft:stone_block_slab2"
            "minecraft:stone_slab3" -> "minecraft:stone_block_slab3"
            "minecraft:stone_slab4" -> "minecraft:stone_block_slab4"
            else -> name
        }
    }
}
