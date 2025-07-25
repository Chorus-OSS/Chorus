package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.block.BlockAncientDebris
import org.chorus_oss.chorus.block.BlockNetheriteBlock
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.registry.Registries


open class ItemBlock @JvmOverloads constructor(
    blockState: BlockState,
    name: String = "",
    aux: Int = 0,
    count: Int = 1
) :
    Item(blockState, aux, count, name, true) {
    override var damage: Int
        get() = super.damage
        set(meta) {
            if (meta != 0) {
                val i = Registries.BLOCKSTATE_ITEMMETA[blockState!!.identifier, meta] ?: 0
                if (i != 0) this.blockState = Registries.BLOCKSTATE[i]
            }
        }

    override fun clone(): ItemBlock {
        return super.clone() as ItemBlock
    }

    override val isLavaResistant: Boolean
        get() = when (blockState?.identifier) {
            BlockAncientDebris.properties.identifier,
            BlockNetheriteBlock.properties.identifier -> true

            else -> false
        }
}
