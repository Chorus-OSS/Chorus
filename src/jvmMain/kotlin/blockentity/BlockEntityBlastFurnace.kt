package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.inventory.BlastFurnaceInventory
import org.chorus_oss.chorus.inventory.SmeltingInventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.recipe.SmeltingRecipe

class BlockEntityBlastFurnace(chunk: IChunk, nbt: CompoundTag) : BlockEntityFurnace(chunk, nbt) {
    override val furnaceName: String
        get() = "Blast Furnace"


    override val clientName: String
        get() = BlockEntityID.BLAST_FURNACE


    override val idleBlockId: String
        get() = BlockID.BLAST_FURNACE


    override val burningBlockId: String
        get() = BlockID.LIT_BLAST_FURNACE


    override fun createInventory(): SmeltingInventory {
        return BlastFurnaceInventory(this)
    }

    override fun matchRecipe(raw: Item): SmeltingRecipe? {
        return Server.instance.recipeRegistry.findBlastFurnaceRecipe(raw)
    }

    override val speedMultiplier: Int
        get() = 2
}
