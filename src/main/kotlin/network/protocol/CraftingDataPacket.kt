package org.chorus_oss.chorus.network.protocol

import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import org.chorus_oss.chorus.network.protocol.types.MaterialReducerDataEntry
import org.chorus_oss.chorus.network.protocol.types.RecipeUnlockingRequirement
import org.chorus_oss.chorus.network.protocol.types.RecipeUnlockingRequirement.UnlockingContext
import org.chorus_oss.chorus.recipe.*
import org.chorus_oss.chorus.recipe.descriptor.DefaultDescriptor

import java.util.*

data class CraftingDataPacket(
    val craftingEntries: List<Recipe>,
    val potionMixes: List<BrewingRecipe>,
    val containerMixes: List<ContainerRecipe>,
    val materialReducers: List<MaterialReducerDataEntry>
) : DataPacket() {
    private val entries: MutableList<Recipe> = mutableListOf()
    private val brewingEntries: MutableList<BrewingRecipe> = mutableListOf()
    private val containerEntries: MutableList<ContainerRecipe> = mutableListOf()

    @JvmField
    var cleanRecipes: Boolean = false

    override fun encode(byteBuf: HandleByteBuf) {
        byteBuf.writeUnsignedVarInt(entries.size)

        var recipeNetworkId = 1
        for (recipe in entries) {
            byteBuf.writeVarInt(recipe.type.networkType)
            when (recipe.type) {
                RecipeType.STONECUTTER -> {
                    val stonecutter = recipe as StonecutterRecipe
                    byteBuf.writeString(stonecutter.recipeId)
                    byteBuf.writeUnsignedVarInt(1)
                    byteBuf.writeRecipeIngredient(DefaultDescriptor(stonecutter.ingredient))
                    byteBuf.writeUnsignedVarInt(1)
                    byteBuf.writeSlot(stonecutter.result, true)
                    byteBuf.writeUUID(stonecutter.uuid)
                    byteBuf.writeString(CRAFTING_TAG_STONECUTTER)
                    byteBuf.writeVarInt(stonecutter.priority)
                    this.writeRequirement(byteBuf, stonecutter.requirement)
                    byteBuf.writeUnsignedVarInt(recipeNetworkId++)
                }

                RecipeType.SHAPELESS, RecipeType.CARTOGRAPHY, RecipeType.USER_DATA_SHAPELESS_RECIPE -> {
                    val shapeless = recipe as ShapelessRecipe
                    byteBuf.writeString(shapeless.recipeId)
                    val ingredients = shapeless.ingredients
                    byteBuf.writeUnsignedVarInt(ingredients.size)
                    for (ingredient in ingredients) {
                        byteBuf.writeRecipeIngredient(ingredient)
                    }
                    byteBuf.writeUnsignedVarInt(1)
                    byteBuf.writeSlot(shapeless.result, true)
                    byteBuf.writeUUID(shapeless.uuid)
                    when (recipe.type) {
                        RecipeType.CARTOGRAPHY -> byteBuf.writeString(CRAFTING_TAG_CARTOGRAPHY_TABLE)
                        RecipeType.SHAPELESS, RecipeType.USER_DATA_SHAPELESS_RECIPE -> byteBuf.writeString(
                            CRAFTING_TAG_CRAFTING_TABLE
                        )

                        else -> Unit
                    }
                    byteBuf.writeVarInt(shapeless.priority)
                    this.writeRequirement(byteBuf, shapeless.requirement)
                    byteBuf.writeUnsignedVarInt(recipeNetworkId++)
                }

                RecipeType.SMITHING_TRANSFORM -> {
                    val smithing = recipe as SmithingTransformRecipe
                    byteBuf.writeString(smithing.recipeId)
                    byteBuf.writeRecipeIngredient(smithing.template)
                    byteBuf.writeRecipeIngredient(smithing.base)
                    byteBuf.writeRecipeIngredient(smithing.addition)
                    byteBuf.writeSlot(smithing.result, true)
                    byteBuf.writeString(CRAFTING_TAG_SMITHING_TABLE)
                    byteBuf.writeUnsignedVarInt(recipeNetworkId++)
                }

                RecipeType.SMITHING_TRIM -> {
                    val shaped = recipe as SmithingTrimRecipe
                    byteBuf.writeString(shaped.recipeId)
                    byteBuf.writeRecipeIngredient(shaped.ingredients[0])
                    byteBuf.writeRecipeIngredient(shaped.ingredients[1])
                    byteBuf.writeRecipeIngredient(shaped.ingredients[2])
                    byteBuf.writeString(shaped.tag)
                    byteBuf.writeUnsignedVarInt(recipeNetworkId++)
                }

                RecipeType.SHAPED -> {
                    val shaped = recipe as ShapedRecipe
                    byteBuf.writeString(shaped.recipeId)
                    byteBuf.writeVarInt(shaped.width)
                    byteBuf.writeVarInt(shaped.height)
                    for (z in 0..<shaped.height) {
                        for (x in 0..<shaped.width) {
                            byteBuf.writeRecipeIngredient(shaped.getIngredient(x, z))
                        }
                    }
                    val results = shaped.results
                    byteBuf.writeUnsignedVarInt(results.size)
                    for (output in results) {
                        byteBuf.writeSlot(output, true)
                    }
                    byteBuf.writeUUID(shaped.uuid)
                    byteBuf.writeString(CRAFTING_TAG_CRAFTING_TABLE)
                    byteBuf.writeVarInt(shaped.priority)
                    byteBuf.writeBoolean(shaped.isMirror)
                    this.writeRequirement(byteBuf, shaped.requirement)
                    byteBuf.writeUnsignedVarInt(recipeNetworkId++)
                }

                RecipeType.MULTI -> {
                    byteBuf.writeUUID((recipe as MultiRecipe).id)
                    byteBuf.writeUnsignedVarInt(recipeNetworkId++)
                }

                RecipeType.FURNACE,
                RecipeType.SMOKER,
                RecipeType.BLAST_FURNACE,
                RecipeType.CAMPFIRE,
                RecipeType.SOUL_CAMPFIRE -> {
                    val smelting = recipe as SmeltingRecipe
                    val input = smelting.input.toItem()
                    byteBuf.writeVarInt(input.runtimeId)
                    if (recipe.type.name.endsWith("_DATA")) {
                        byteBuf.writeVarInt(input.damage)
                    }
                    byteBuf.writeSlot(smelting.result, true)
                    when (recipe.type) {
                        RecipeType.FURNACE -> byteBuf.writeString(CRAFTING_TAG_FURNACE)
                        RecipeType.SMOKER -> byteBuf.writeString(CRAFTING_TAG_SMOKER)
                        RecipeType.BLAST_FURNACE -> byteBuf.writeString(CRAFTING_TAG_BLAST_FURNACE)
                        RecipeType.CAMPFIRE -> byteBuf.writeString(CRAFTING_TAG_CAMPFIRE)
                        RecipeType.SOUL_CAMPFIRE -> byteBuf.writeString(CRAFTING_TAG_SOUL_CAMPFIRE)
                        else -> Unit
                    }
                }

                else -> Unit
            }
        }

        byteBuf.writeUnsignedVarInt(brewingEntries.size)
        for (recipe in brewingEntries) {
            byteBuf.writeVarInt(recipe.input.runtimeId)
            byteBuf.writeVarInt(recipe.input.damage)
            byteBuf.writeVarInt(recipe.ingredient.runtimeId)
            byteBuf.writeVarInt(recipe.ingredient.damage)
            byteBuf.writeVarInt(recipe.result.runtimeId)
            byteBuf.writeVarInt(recipe.result.damage)
        }

        byteBuf.writeUnsignedVarInt(containerEntries.size)
        for (recipe in containerEntries) {
            byteBuf.writeVarInt(recipe.input.runtimeId)
            byteBuf.writeVarInt(recipe.ingredient.runtimeId)
            byteBuf.writeVarInt(recipe.result.runtimeId)
        }

        byteBuf.writeUnsignedVarInt(0) // Material reducers size

        byteBuf.writeBoolean(cleanRecipes)
    }

    fun addNetworkIdRecipe(recipes: List<Recipe>) {
        entries.addAll(recipes)
    }

    fun addFurnaceRecipe(vararg recipe: FurnaceRecipe) {
        entries.addAll(recipe)
    }

    fun addSmokerRecipe(vararg recipe: SmokerRecipe) {
        entries.addAll(recipe)
    }

    fun addBlastFurnaceRecipe(vararg recipe: BlastFurnaceRecipe) {
        entries.addAll(recipe)
    }

    fun addCampfireRecipeRecipe(vararg recipe: CampfireRecipe) {
        entries.addAll(recipe)
    }

    fun addBrewingRecipe(vararg recipe: BrewingRecipe) {
        brewingEntries.addAll(recipe)
    }

    fun addContainerRecipe(vararg recipe: ContainerRecipe) {
        Collections.addAll(containerEntries, *recipe)
    }

    protected fun writeRequirement(buffer: HandleByteBuf, requirement: RecipeUnlockingRequirement) {
        buffer.writeByte(requirement.context.ordinal)
        if (requirement.context == UnlockingContext.NONE) {
            buffer.writeUnsignedVarInt(requirement.ingredients.size)
            for (ing in requirement.ingredients) {
                buffer.writeRecipeIngredient(ing)
            }
        }
    }

    override fun pid(): Int {
        return ProtocolInfo.CRAFTING_DATA_PACKET
    }

    companion object {
        const val CRAFTING_TAG_CRAFTING_TABLE: String = "crafting_table"
        const val CRAFTING_TAG_CARTOGRAPHY_TABLE: String = "cartography_table"
        const val CRAFTING_TAG_STONECUTTER: String = "stonecutter"
        const val CRAFTING_TAG_FURNACE: String = "furnace"
        const val CRAFTING_TAG_CAMPFIRE: String = "campfire"
        const val CRAFTING_TAG_SOUL_CAMPFIRE: String = "soul_campfire"
        const val CRAFTING_TAG_BLAST_FURNACE: String = "blast_furnace"
        const val CRAFTING_TAG_SMOKER: String = "smoker"
        const val CRAFTING_TAG_SMITHING_TABLE: String = "smithing_table"
    }
}
