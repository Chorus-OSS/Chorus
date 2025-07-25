package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.inventory.SmeltingInventory
import org.chorus_oss.chorus.inventory.SmokerInventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.recipe.SmeltingRecipe

class BlockEntitySmoker(chunk: IChunk, nbt: CompoundTag) : BlockEntityFurnace(chunk, nbt) {
    override val furnaceName: String
        get() = "Smoker"

    override val clientName: String
        get() = BlockEntityID.SMOKER

    override val idleBlockId: String
        get() = BlockID.SMOKER

    override val burningBlockId: String
        get() = BlockID.LIT_SMOKER

    override fun createInventory(): SmeltingInventory {
        return SmokerInventory(this)
    }

    override fun matchRecipe(raw: Item): SmeltingRecipe? {
        return Server.instance.recipeRegistry.findSmokerRecipe(raw)
    }

    override val speedMultiplier: Int
        get() = 2
}
