package org.chorus_oss.chorus.registry


import com.google.common.collect.Collections2
import com.google.gson.internal.LinkedTreeMap
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.util.ReferenceCountUtil
import io.netty.util.collection.CharObjectHashMap
import io.netty.util.internal.EmptyArrays
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf.Companion.of
import org.chorus_oss.chorus.network.protocol.CraftingDataPacket
import org.chorus_oss.chorus.network.protocol.types.RecipeUnlockingRequirement
import org.chorus_oss.chorus.recipe.*
import org.chorus_oss.chorus.recipe.descriptor.DefaultDescriptor
import org.chorus_oss.chorus.recipe.descriptor.ItemDescriptor
import org.chorus_oss.chorus.recipe.descriptor.ItemDescriptorType
import org.chorus_oss.chorus.recipe.descriptor.ItemTagDescriptor
import org.chorus_oss.chorus.utils.Config
import org.chorus_oss.chorus.utils.Identifier
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.Utils
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class RecipeRegistry : IRegistry<String, Recipe?, Recipe> {
    val vanillaRecipeParser: VanillaRecipeParser = VanillaRecipeParser(this)
    private val recipeMaps = EnumMap<RecipeType, MutableMap<Int, MutableSet<Recipe>>>(
        RecipeType::class.java
    )
    private val allRecipeMaps = HashMap<String, Recipe?>()
    val recipeXpMap: MutableMap<Recipe, Double> = HashMap()
    private val networkIdRecipeList: MutableList<Recipe> = mutableListOf()

    fun getNetworkIdRecipeList(): List<Recipe> {
        return networkIdRecipeList
    }

    fun getRecipeXp(recipe: Recipe): Double {
        return recipeXpMap.getOrDefault(recipe, 0.0)
    }

    fun setRecipeXp(recipe: Recipe, xp: Double) {
        recipeXpMap[recipe] = xp
    }

    val shapelessRecipeMap: Set<ShapelessRecipe>
        get() {
            val result = HashSet<ShapelessRecipe>()
            for (s in recipeMaps[RecipeType.SHAPELESS]!!.values) {
                result.addAll(
                    Collections2.transform(
                        s
                    ) { d: Recipe -> d as ShapelessRecipe })
            }
            return result
        }

    fun findShapelessRecipe(vararg items: Item): ShapelessRecipe? {
        val recipes = recipeMaps[RecipeType.SHAPELESS]!![items.size]
        if (recipes != null) {
            for (r in recipes) {
                if (r.fastCheck(*items)) return r as ShapelessRecipe
            }
        }
        return null
    }

    val shapedRecipeMap: Set<ShapedRecipe>
        get() {
            val result = HashSet<ShapedRecipe>()
            for (s in recipeMaps[RecipeType.SHAPED]!!.values) {
                result.addAll(
                    Collections2.transform(
                        s
                    ) { d: Recipe -> d as ShapedRecipe })
            }
            return result
        }

    fun findShapedRecipe(vararg items: Item): ShapedRecipe? {
        val recipes = recipeMaps[RecipeType.SHAPED]!![items.size]
        if (recipes != null) {
            for (r in recipes) {
                if (r.fastCheck(*items)) return r as ShapedRecipe
            }
        }
        return null
    }

    val furnaceRecipeMap: Set<FurnaceRecipe>
        get() {
            val result = HashSet<FurnaceRecipe>()
            val recipe = recipeMaps[RecipeType.FURNACE]
            if (recipe != null) {
                for (s in recipe.values) {
                    result.addAll(
                        Collections2.transform(
                            s
                        ) { d: Recipe -> d as FurnaceRecipe })
                }
            }
            return result
        }

    fun findFurnaceRecipe(vararg items: Item): FurnaceRecipe? {
        val map1 = recipeMaps[RecipeType.FURNACE]!!
        val recipes = map1[items.size]
        if (recipes != null) {
            for (r in recipes) {
                if (r.fastCheck(*items)) return r as FurnaceRecipe
            }
        }
        return null
    }

    val blastFurnaceRecipeMap: Set<BlastFurnaceRecipe>
        get() {
            val result = HashSet<BlastFurnaceRecipe>()
            val recipe = recipeMaps[RecipeType.BLAST_FURNACE]
            if (recipe != null) {
                for (s in recipe.values) {
                    result.addAll(
                        Collections2.transform(
                            s
                        ) { d: Recipe -> d as BlastFurnaceRecipe })
                }
            }
            return result
        }

    fun findBlastFurnaceRecipe(vararg items: Item): BlastFurnaceRecipe? {
        val map1 = recipeMaps[RecipeType.BLAST_FURNACE]!!
        val recipes = map1[items.size]
        if (recipes != null) {
            for (r in recipes) {
                if (r.fastCheck(*items)) return r as BlastFurnaceRecipe
            }
        }
        return null
    }

    val smokerRecipeMap: Set<SmokerRecipe>
        get() {
            val result = HashSet<SmokerRecipe>()
            val smoker = recipeMaps[RecipeType.SMOKER]
            if (smoker != null) {
                for (s in smoker.values) {
                    result.addAll(
                        Collections2.transform(
                            s
                        ) { d: Recipe -> d as SmokerRecipe })
                }
            }
            return result
        }

    fun findSmokerRecipe(vararg items: Item): SmokerRecipe? {
        val map1 = recipeMaps[RecipeType.SMOKER]
        if (map1 != null) {
            val recipes = map1[items.size]
            recipes ?: return null
            for (r in recipes) {
                if (r.fastCheck(*items)) return r as SmokerRecipe
            }
        }
        return null
    }

    val campfireRecipeMap: Set<CampfireRecipe>
        get() {
            val result = HashSet<CampfireRecipe>()
            val r1 = recipeMaps[RecipeType.CAMPFIRE]
            if (r1 != null) {
                for (s in r1.values) {
                    result.addAll(
                        Collections2.transform(
                            s
                        ) { d: Recipe -> d as CampfireRecipe })
                }
            }
            val r3 = recipeMaps[RecipeType.SOUL_CAMPFIRE]
            if (r3 != null) {
                for (s in r3.values) {
                    result.addAll(
                        Collections2.transform(
                            s
                        ) { d: Recipe -> d as CampfireRecipe })
                }
            }
            return result
        }

    fun findCampfireRecipe(vararg items: Item): CampfireRecipe? {
        val recipes = recipeMaps[RecipeType.CAMPFIRE]!![items.size]
        if (recipes != null) {
            for (r in recipes) {
                if (r.fastCheck(*items)) return r as CampfireRecipe
            }
        }
        return null
    }

    val multiRecipeMap: Set<MultiRecipe>
        get() {
            val result = HashSet<MultiRecipe>()
            for (s in recipeMaps[RecipeType.MULTI]!!.values) {
                result.addAll(
                    Collections2.transform(
                        s
                    ) { d: Recipe -> d as MultiRecipe })
            }
            return result
        }

    fun findMultiRecipe(vararg items: Item): MultiRecipe? {
        val recipes = recipeMaps[RecipeType.MULTI]!![items.size]
        if (recipes != null) {
            for (r in recipes) {
                if (r.fastCheck(*items)) return r as MultiRecipe
            }
        }
        return null
    }

    val stonecutterRecipeMap: Set<StonecutterRecipe>
        get() {
            val result = HashSet<StonecutterRecipe>()
            for (s in recipeMaps[RecipeType.STONECUTTER]!!.values) {
                result.addAll(
                    Collections2.transform(
                        s
                    ) { d: Recipe -> d as StonecutterRecipe })
            }
            return result
        }

    fun findStonecutterRecipe(vararg items: Item): StonecutterRecipe? {
        val recipes = recipeMaps[RecipeType.STONECUTTER]!![items.size]
        if (recipes != null) {
            for (r in recipes) {
                if (r.fastCheck(*items)) return r as StonecutterRecipe
            }
        }
        return null
    }

    val cartographyRecipeMap: Set<CartographyRecipe>
        get() {
            val result = HashSet<CartographyRecipe>()
            for (s in recipeMaps[RecipeType.CARTOGRAPHY]!!.values) {
                result.addAll(
                    Collections2.transform(
                        s
                    ) { d: Recipe -> d as CartographyRecipe })
            }
            return result
        }

    fun findCartographyRecipe(vararg items: Item): CartographyRecipe? {
        val recipes = recipeMaps[RecipeType.CARTOGRAPHY]
        if (recipes != null) {
            val map1 = recipes[items.size]
            if (map1 != null) {
                for (r in map1) {
                    if (r.fastCheck(*items)) return r as CartographyRecipe
                }
            }
        }
        return null
    }

    val smithingTransformRecipeMap: Set<SmithingTransformRecipe>
        get() {
            val result = HashSet<SmithingTransformRecipe>()
            for (s in recipeMaps[RecipeType.SMITHING_TRANSFORM]!!.values) {
                result.addAll(
                    Collections2.transform(
                        s
                    ) { d: Recipe -> d as SmithingTransformRecipe })
            }
            return result
        }

    fun findSmithingTransform(vararg items: Item): SmithingTransformRecipe? {
        val recipes = recipeMaps[RecipeType.SMITHING_TRANSFORM]!![items.size]
        if (recipes != null) {
            for (r in recipes) {
                if (r.fastCheck(*items)) return r as SmithingTransformRecipe
            }
        }
        return null
    }

    val brewingRecipeMap: Set<BrewingRecipe>
        get() {
            val result = HashSet<BrewingRecipe>()
            for (s in recipeMaps[RecipeType.BREWING]!!.values) {
                result.addAll(
                    Collections2.transform(
                        s
                    ) { d: Recipe -> d as BrewingRecipe })
            }
            return result
        }

    fun findBrewingRecipe(vararg items: Item): BrewingRecipe? {
        val recipes = recipeMaps[RecipeType.BREWING]!![items.size]
        if (recipes != null) {
            for (r in recipes) {
                if (r.fastCheck(*items)) return r as BrewingRecipe
            }
        }
        return null
    }

    val containerRecipeMap: Set<ContainerRecipe>
        get() {
            val result = HashSet<ContainerRecipe>()
            for (s in recipeMaps[RecipeType.CONTAINER]!!.values) {
                result.addAll(
                    Collections2.transform(
                        s
                    ) { d: Recipe -> d as ContainerRecipe })
            }
            return result
        }

    fun findContainerRecipe(vararg items: Item): ContainerRecipe? {
        val recipes = recipeMaps[RecipeType.CONTAINER]!![items.size]
        if (recipes != null) {
            for (r in recipes) {
                if (r.fastCheck(*items)) return r as ContainerRecipe
            }
        }
        return null
    }

    val craftingPacket: ByteBuf
        get() = buffer!!.copy()

    fun getRecipeByNetworkId(networkId: Int): Recipe {
        return networkIdRecipeList[networkId - 1]
    }

    override fun init() {
        if (isLoad.getAndSet(true)) return
        log.info("Loading recipes...")
        this.loadRecipes()
        this.rebuildPacket()
        log.info("Loaded {} recipes.", recipeCount)
    }

    override fun get(key: String): Recipe? {
        return allRecipeMaps[key]
    }

    override fun reload() {
        isLoad.set(false)
        recipeCount = 0
        if (buffer != null) {
            buffer!!.release()
            buffer = null
        }
        recipeMaps.clear()
        recipeXpMap.clear()
        allRecipeMaps.clear()
        networkIdRecipeList.clear()
        init()
    }

    @Throws(RegisterException::class)
    override fun register(key: String, recipe: Recipe) {
        if (recipe is CraftingRecipe) {
            val item: Item = recipe.results.first()
            val id = Utils.dataToUUID(
                recipeCount.toString(),
                item.id,
                item.damage.toString(),
                item.getCount().toString(),
                item.compoundTag.contentToString()
            )
            if (recipe.uuid == null) recipe.uuid = id
        }
        if (allRecipeMaps.putIfAbsent(recipe.recipeId, recipe) != null) {
            throw RegisterException("Duplicate recipe ${recipe.recipeId} type ${recipe.type}")
        }
        val recipeMap = recipeMaps.computeIfAbsent(recipe.type) { HashMap() }
        val r: MutableSet<Recipe> = recipeMap.computeIfAbsent(recipe.ingredients.size) { HashSet() }
        r.add(recipe)
        ++recipeCount
        when (recipe.type) {
            RecipeType.STONECUTTER,
            RecipeType.SHAPELESS,
            RecipeType.CARTOGRAPHY,
            RecipeType.USER_DATA_SHAPELESS_RECIPE,
            RecipeType.SMITHING_TRANSFORM,
            RecipeType.SMITHING_TRIM,
            RecipeType.SHAPED,
            RecipeType.MULTI -> networkIdRecipeList.add(
                recipe
            )

            else -> Unit
        }
    }

    fun register(recipe: Recipe) {
        try {
            this.register(recipe.recipeId, recipe)
        } catch (e: RegisterException) {
            throw RuntimeException(e)
        }
    }

    fun cleanAllRecipes() {
        recipeXpMap.clear()
        networkIdRecipeList.clear()
        recipeMaps.values.forEach { obj -> obj.clear() }
        allRecipeMaps.clear()
        recipeCount = 0
        ReferenceCountUtil.safeRelease(buffer)
        buffer = null
    }

    fun rebuildPacket() {
        val buf = ByteBufAllocator.DEFAULT.ioBuffer(64)
        val pk = CraftingDataPacket(
            listOf(),
            listOf(),
            listOf(),
            listOf()
        )
        pk.cleanRecipes = true

        pk.addNetworkIdRecipe(networkIdRecipeList)

        for (recipe in furnaceRecipeMap) {
            pk.addFurnaceRecipe(recipe)
        }
        for (recipe in smokerRecipeMap) {
            pk.addSmokerRecipe(recipe)
        }
        for (recipe in blastFurnaceRecipeMap) {
            pk.addBlastFurnaceRecipe(recipe)
        }
        for (recipe in campfireRecipeMap) {
            pk.addCampfireRecipeRecipe(recipe)
        }
        for (recipe in brewingRecipeMap) {
            pk.addBrewingRecipe(recipe)
        }
        for (recipe in containerRecipeMap) {
            pk.addContainerRecipe(recipe)
        }
        pk.encode(of(buf))
        buffer = buf
    }

    private fun loadRecipes() {
        //load xp config
        val furnaceXpConfig = Config(Config.JSON)
        try {
            Server::class.java.classLoader.getResourceAsStream("furnace_xp.json").use { r ->
                furnaceXpConfig.load(r)
            }
        } catch (e: IOException) {
            log.warn("Failed to load furnace xp config")
        }

        // INFORMATION:
        // This code is used to read the recipes.json provided by endstone, but since
        // we do not have that data at the moment (for 1.21.20), we use recipes.json provided by Kaooot:
        // https://github.com/Kaooot/bedrock-network-data
        // We can use the original reader again, once endstone generates data again
        val recipeConfig = Config(Config.JSON)
        try {
            Server::class.java.classLoader.getResourceAsStream("recipes.json").use { r ->
                recipeConfig.load(r)
                //load potionMixes
                val potionMixes: List<Map<String, Any>> = recipeConfig.getList("potionMixes") as List<Map<String, Any>>
                for (recipe in potionMixes) {
                    val inputId = recipe["inputId"] as String?
                    val inputMeta = (recipe["inputMeta"] as Double).toInt()

                    val reagentId = recipe["reagentId"] as String?
                    val reagentMeta = (recipe["reagentMeta"] as Double).toInt()

                    val outputId = recipe["outputId"] as String?
                    val outputMeta = (recipe["outputMeta"] as Double).toInt()

                    val inputItem = get(inputId!!, inputMeta)
                    val reagentItem = get(reagentId!!, reagentMeta)
                    val outputItem = get(outputId!!, outputMeta)

                    if (inputItem.isNothing || reagentItem.isNothing || outputItem.isNothing) {
                        continue
                    }
                    register(
                        BrewingRecipe(
                            inputItem,
                            reagentItem,
                            outputItem
                        )
                    )

                    // Endstone Reader
//                Map<String, Object> input = (Map<String, Object>) recipe.get("input");
//                String inputId = input.get("item").toString();
//                int inputMeta = Utils.toInt(input.get("data"));
//
//                Map<String, Object> output = (Map<String, Object>) recipe.get("output");
//                String outputId = output.get("item").toString();
//                int outputMeta = Utils.toInt(output.get("data"));
//
//                Map<String, Object> reagent = (Map<String, Object>) recipe.get("reagent");
//                String reagentId = reagent.get("item").toString();
//                int reagentMeta = Utils.toInt(reagent.get("data"));
//
//                Item inputItem = Item.get(inputId, inputMeta);
//                Item reagentItem = Item.get(reagentId, reagentMeta);
//                Item outputItem = Item.get(outputId, outputMeta);
//                if (inputItem.isNull() || reagentItem.isNull() || outputItem.isNull()) {
//                    continue;
//                }
//                register(new BrewingRecipe(
//                        inputItem,
//                        reagentItem,
//                        outputItem
//                ));
                }

                //load containerMixes
                val containerMixes: List<Map<String, Any>> =
                    recipeConfig.getList("containerMixes") as List<Map<String, Any>>
                for (containerMix in containerMixes) {
                    val inputId = containerMix["inputId"] as String?
                    val reagentId = containerMix["reagentId"] as String?
                    val outputId = containerMix["outputId"] as String?

                    this.register(
                        ContainerRecipe(
                            Item.get(inputId!!),
                            Item.get(reagentId!!),
                            Item.get(outputId!!)
                        )
                    )

                    // Endstone Reader
//                String fromItemId = containerMix.get("input").toString();
//                String ingredient = containerMix.get("reagent").toString();
//                String toItemId = containerMix.get("output").toString();
//                register(new ContainerRecipe(Item.get(fromItemId), Item.get(ingredient), Item.get(toItemId)));
                }

                //load all other recipes (Not Endstone Reader)
                val recipes: List<Map<String, Any>> = recipeConfig.getList("recipes") as List<Map<String, Any>>
                for (recipe in recipes) {
                    val block = recipe["block"] as String? ?: continue

                    when (block) {
                        "smithing_table" -> {
                            val recipeId = recipe["id"] as String?

                            val baseDescriptor = parseDescription(
                                (recipe["base"] as Map<String, Any>),
                                ParseType.SMITHING_TABLE
                            )
                            val additionDescriptor = parseDescription(
                                (recipe["addition"] as Map<String, Any>),
                                ParseType.SMITHING_TABLE
                            )
                            val templateDescriptor = parseDescription(
                                (recipe["template"] as Map<String, Any>),
                                ParseType.SMITHING_TABLE
                            )

                            if (recipe.containsKey("result")) { // is smithing transform recipe
                                val result = recipe["result"] as Map<String, Any>
                                val itemId = result["id"] as String?
                                val count = (result["count"] as Double).toInt()
                                this.register(
                                    SmithingTransformRecipe(
                                        recipeId!!,
                                        get(itemId!!, 0, count),
                                        baseDescriptor,
                                        additionDescriptor,
                                        templateDescriptor
                                    )
                                )
                            } else {    // is smithing trim recipe
                                this.register(
                                    SmithingTrimRecipe(
                                        recipeId!!,
                                        baseDescriptor,
                                        additionDescriptor,
                                        templateDescriptor,
                                        "smithing_table"
                                    )
                                )
                            }
                        }

                        "stonecutter" -> {
                            val inputParseType = ParseType.STONECUTTER_INPUT
                            val outputParseType = ParseType.STONECUTTER_OUTPUT

                            val recipeId = recipe["id"] as String?
                            val uuid = UUID.fromString(recipe["uuid"] as String?)
                            val priority = (recipe["priority"] as Double).toInt()

                            val outputs = recipe["output"] as MutableList<Map<String, Any>>
                            val primaryResultData = outputs.removeFirst()
                            val primaryResult = parseDescription(primaryResultData, outputParseType)

                            val ingredients: MutableList<ItemDescriptor> = ArrayList()
                            val inputs = recipe["input"] as List<Map<String, Any>>

                            for (input in inputs) {
                                ingredients.add(parseDescription(input, inputParseType))
                            }

                            this.register(
                                StonecutterRecipe(
                                    recipeId,
                                    uuid,
                                    priority,
                                    primaryResult.toItem(),
                                    ingredients.first().toItem(),
                                    RecipeUnlockingRequirement(
                                        RecipeUnlockingRequirement.UnlockingContext.ALWAYS_UNLOCKED
                                    )
                                )
                            )
                        }

                        "cartography_table" -> {
                            val inputParseType = ParseType.CARTOGRAPHY_TABLE_INPUT
                            val outputParseType = ParseType.CARTOGRAPHY_TABLE_OUTPUT

                            val recipeId = recipe["id"] as String?
                            val uuid = UUID.fromString(recipe["uuid"] as String?)
                            val priority = (recipe["priority"] as Double).toInt()

                            val outputs = recipe["output"] as MutableList<Map<String, Any>>
                            val primaryResultData = outputs.removeFirst()
                            val primaryResult = parseDescription(primaryResultData, outputParseType)

                            val ingredients: MutableList<ItemDescriptor> = ArrayList()
                            val inputs = recipe["input"] as List<Map<String, Any>>

                            for (input in inputs) {
                                ingredients.add(parseDescription(input, inputParseType))
                            }

                            this.register(
                                CartographyRecipe(
                                    recipeId,
                                    uuid,
                                    priority,
                                    primaryResult.toItem(),
                                    ingredients,
                                    RecipeUnlockingRequirement(
                                        RecipeUnlockingRequirement.UnlockingContext.ALWAYS_UNLOCKED
                                    )
                                )
                            )
                        }

                        "crafting_table" -> {
                            var inputParseType = ParseType.CRAFTING_TABLE_INPUT
                            var outputParseType = ParseType.CRAFTING_TABLE_OUTPUT

                            when (block) {
                                "cartography_table" -> {
                                    inputParseType = ParseType.CARTOGRAPHY_TABLE_INPUT
                                    outputParseType = ParseType.CARTOGRAPHY_TABLE_OUTPUT
                                }
                            }

                            val recipeId = recipe["id"] as String?
                            val uuid = UUID.fromString(recipe["uuid"] as String?)
                            val priority = (recipe["priority"] as Double).toInt()

                            val outputs = recipe["output"] as MutableList<Map<String, Any>>
                            val primaryResultData = outputs.removeFirst()
                            val primaryResult = parseDescription(primaryResultData, outputParseType)

                            if (recipe.containsKey("shape")) {
                                val extraResults: MutableList<Item> = ArrayList()
                                for (output in outputs) {
                                    extraResults.add(parseDescription(output, outputParseType).toItem())
                                }

                                val shape = (recipe["shape"] as List<String>).toTypedArray()

                                val ingredients: MutableMap<Char, ItemDescriptor> = CharObjectHashMap()

                                val inputs = recipe["input"] as LinkedTreeMap<String, Any>

                                for ((key, value) in inputs) {
                                    val patternKey = key[0]
                                    val ingredientData = value as Map<String, Any>

                                    ingredients[patternKey] = parseDescription(
                                        ingredientData,
                                        inputParseType
                                    )
                                }

                                this.register(
                                    ShapedRecipe(
                                        recipeId,
                                        uuid,
                                        priority,
                                        primaryResult.toItem(),
                                        shape,
                                        ingredients,
                                        extraResults,
                                        false,
                                        RecipeUnlockingRequirement(
                                            RecipeUnlockingRequirement.UnlockingContext.ALWAYS_UNLOCKED
                                        )
                                    )
                                )
                            } else {    // is shapeless recipe

                                val ingredients: MutableList<ItemDescriptor> = ArrayList()
                                val inputs = recipe["input"] as List<Map<String, Any>>

                                for (input in inputs) {
                                    ingredients.add(parseDescription(input, inputParseType))
                                }

                                this.register(
                                    ShapelessRecipe(
                                        recipeId,
                                        uuid,
                                        priority,
                                        primaryResult.toItem(),
                                        ingredients,
                                        RecipeUnlockingRequirement(
                                            RecipeUnlockingRequirement.UnlockingContext.ALWAYS_UNLOCKED
                                        )
                                    )
                                )
                            }
                        }

                        "furnace", "blast_furnace", "smoker", "campfire", "soul_campfire" -> {
                            val inputData = recipe["input"] as Map<String, Any>
                            val outputData = recipe["output"] as Map<String, Any>
                            val inputItem = parseDescription(inputData, ParseType.FURNACE_INPUT).toItem()
                            val outputItem = parseDescription(outputData, ParseType.FURNACE_OUTPUT).toItem()

                            val smeltingRecipe = when (block) {
                                "blast_furnace" -> BlastFurnaceRecipe(outputItem, inputItem)
                                "smoker" -> SmokerRecipe(outputItem, inputItem)
                                "campfire" -> CampfireRecipe(outputItem, inputItem)
                                "soul_campfire" -> SoulCampfireRecipe(outputItem, inputItem)
                                else -> FurnaceRecipe(outputItem, inputItem)
                            }

                            val xp = furnaceXpConfig.getDouble(inputItem.id + ":" + inputItem.damage)
                            if (xp != 0.0) {
                                this.setRecipeXp(smeltingRecipe, xp)
                            }

                            try {
                                this.register(smeltingRecipe)
                            } catch (e: Exception) {     //this can be removed once duplicate recipes no longer exist
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            log.warn("Failed to load recipes config")
        }

        // Allow to rename without crafting
        register(
            CartographyRecipe(
                get(ItemID.EMPTY_MAP, 0, 1, EmptyArrays.EMPTY_BYTES, false),
                listOf(get(ItemID.EMPTY_MAP, 0, 1, EmptyArrays.EMPTY_BYTES, false))
            )
        )
        register(
            CartographyRecipe(
                get(ItemID.EMPTY_MAP, 2, 1, EmptyArrays.EMPTY_BYTES, false),
                listOf(get(ItemID.EMPTY_MAP, 2, 1, EmptyArrays.EMPTY_BYTES, false))
            )
        )
        register(
            CartographyRecipe(
                get(ItemID.FILLED_MAP, 0, 1, EmptyArrays.EMPTY_BYTES, false),
                listOf(get(ItemID.FILLED_MAP, 0, 1, EmptyArrays.EMPTY_BYTES, false))
            )
        )
        register(
            CartographyRecipe(
                get(ItemID.FILLED_MAP, 3, 1, EmptyArrays.EMPTY_BYTES, false),
                listOf(get(ItemID.FILLED_MAP, 3, 1, EmptyArrays.EMPTY_BYTES, false))
            )
        )
        register(
            CartographyRecipe(
                get(ItemID.FILLED_MAP, 4, 1, EmptyArrays.EMPTY_BYTES, false),
                listOf(get(ItemID.FILLED_MAP, 4, 1, EmptyArrays.EMPTY_BYTES, false))
            )
        )
        register(
            CartographyRecipe(
                get(ItemID.FILLED_MAP, 5, 1, EmptyArrays.EMPTY_BYTES, false),
                listOf(get(ItemID.FILLED_MAP, 5, 1, EmptyArrays.EMPTY_BYTES, false))
            )
        )
    }

    /**
     * Temporary used for parsing information out of the new recipes.json format
     *
     * @return parsed ItemDescriptor
     */
    private fun parseDescription(data: Map<String, Any>, parseType: ParseType): ItemDescriptor {
        var descriptor: ItemDescriptor? = null

        when (parseType) {
            ParseType.SMITHING_TABLE, ParseType.CRAFTING_TABLE_INPUT, ParseType.CARTOGRAPHY_TABLE_INPUT, ParseType.STONECUTTER_INPUT -> {
                if (data["type"] == "item_tag") {
                    val itemTag = data["itemTag"] as String?
                    val count = if (data.containsKey("count")) Utils.toInt(data["count"]!!) else 1
                    descriptor = ItemTagDescriptor(itemTag!!, count)
                } else if (data["type"] == "complex_alias") {
                    val itemId = data["name"] as String?
                    val count = (data["count"] as Double).toInt()
                    val item = get(itemId!!, 0, count)
                    item.disableMeta()
                    descriptor = DefaultDescriptor(item)
                } else {    // only other possibility is the "default" type
                    val itemId = data["itemId"] as String
                    val count = if (data.containsKey("count")) Utils.toInt(data["count"]!!) else 1
                    val meta = (data["auxValue"] as Double).toInt().toShort()
                    val item: Item
                    if (meta == Short.MAX_VALUE || meta.toInt() == -1) {
                        item = get(itemId, 0, count)
                        item.disableMeta()
                    } else {
                        item = get(itemId, meta.toInt(), count)
                    }
                    descriptor = DefaultDescriptor(item)
                }
            }

            ParseType.CRAFTING_TABLE_OUTPUT, ParseType.CARTOGRAPHY_TABLE_OUTPUT, ParseType.STONECUTTER_OUTPUT, ParseType.FURNACE_INPUT, ParseType.FURNACE_OUTPUT -> {
                val id = data["id"] as String?
                var count = 1
                var meta: Short = 0

                if (data.containsKey("count")) {
                    count = (data["count"] as Double).toInt()
                }

                if (data.containsKey("damage")) {
                    meta = (data["damage"] as Double).toInt().toShort()
                }

                val item: Item

                if (meta == Short.MAX_VALUE || meta.toInt() == -1) {
                    item = get(id!!, 0, count)
                    item.disableMeta()
                } else {
                    item = get(id!!, meta.toInt(), count)
                }
                descriptor = DefaultDescriptor(item)
            }
        }

        return descriptor
    }

    internal enum class ParseType {
        SMITHING_TABLE,
        CRAFTING_TABLE_INPUT,
        CRAFTING_TABLE_OUTPUT,
        STONECUTTER_INPUT,
        STONECUTTER_OUTPUT,
        CARTOGRAPHY_TABLE_INPUT,
        CARTOGRAPHY_TABLE_OUTPUT,

        // we use this two parse types for all blocks similar to furnace:
        // blast_furnace, smoker, campfire and soul_campfire
        FURNACE_INPUT,
        FURNACE_OUTPUT
    }

    private fun parseShapelessRecipe(recipeObject: Map<String, Any>, craftingBlock: String): Recipe? {
        val id = recipeObject["id"].toString()
        if (craftingBlock == "smithing_table") {
            val base = recipeObject["base"] as Map<String, Any>
            val baseItem = parseRecipeItem(base)
            val addition = recipeObject["addition"] as Map<String, Any>
            val additionItem = parseRecipeItem(addition)
            val template = recipeObject["template"] as Map<String, Any>
            val templateItem = parseRecipeItem(template)
            val output = recipeObject["output"] as ArrayList<Map<String, Any>>
            val outputItem = parseRecipeItem(output[0])
            return SmithingTransformRecipe(id, outputItem.toItem(), baseItem, additionItem, templateItem)
        }
        val uuid = UUID.fromString(recipeObject["uuid"].toString())
        val itemDescriptors: MutableList<ItemDescriptor> = ArrayList()
        val inputs = (recipeObject["input"] as List<Map<String, Any>>)
        val outputs = (recipeObject["output"] as List<Map<String, Any>>)
        if (outputs.size > 1) {
            return null
        }
        val first: Map<String, Any> = outputs.first()

        val priority = if (recipeObject.containsKey("priority")) Utils.toInt(recipeObject["priority"]!!) else 0

        val result = parseRecipeItem(first)
        val resultItem = result.toItem()
        for (ingredient in inputs) {
            val recipeItem = parseRecipeItem(ingredient)
            itemDescriptors.add(recipeItem)
        }

        val recipeUnlockingRequirement =
            RecipeUnlockingRequirement(RecipeUnlockingRequirement.UnlockingContext.ALWAYS_UNLOCKED)

        return when (craftingBlock) {
            "crafting_table", "deprecated" -> ShapelessRecipe(
                id,
                uuid,
                priority,
                resultItem,
                itemDescriptors,
                recipeUnlockingRequirement
            )

            "shulker_box" -> UserDataShapelessRecipe(
                id,
                uuid,
                priority,
                resultItem,
                itemDescriptors,
                recipeUnlockingRequirement
            )

            "stonecutter" -> StonecutterRecipe(
                id,
                uuid,
                priority,
                resultItem,
                itemDescriptors[0].toItem(),
                recipeUnlockingRequirement
            )

            "cartography_table" -> CartographyRecipe(
                id,
                uuid,
                priority,
                resultItem,
                itemDescriptors,
                recipeUnlockingRequirement
            )

            else -> null
        }
    }

    private fun parseShapeRecipe(recipeObject: Map<String, Any>): Recipe {
        val id = recipeObject["id"].toString()
        val uuid = UUID.fromString(recipeObject["uuid"].toString())
        val outputs = recipeObject["output"] as MutableList<Map<String, Any>>

        val first = outputs.removeFirst()
        val shape = (recipeObject["pattern"] as List<String>).toTypedArray()
        val ingredients: MutableMap<Char, ItemDescriptor> = CharObjectHashMap()

        val priority = Utils.toInt(recipeObject["priority"]!!)
        val primaryResult = parseRecipeItem(first)

        val extraResults: MutableList<Item> = ArrayList()
        for (data in outputs) {
            val output = parseRecipeItem(data)
            extraResults.add(output.toItem())
        }

        val input = (recipeObject["input"] as MutableMap<String, Any>)
        var mirror = false
        if (input.containsKey("assumeSymetry")) {
            mirror = input.remove("assumeSymetry").toString().toBoolean()
        }
        val input2 = input as Map<String, Map<String, Any>>
        for ((key, ingredient) in input2) {
            val ingredientChar = key[0]
            val itemDescriptor = parseRecipeItem(ingredient)
            ingredients[ingredientChar] = itemDescriptor
        }

        val recipeUnlockingRequirement: RecipeUnlockingRequirement? = null
        return ShapedRecipe(
            id,
            uuid,
            priority,
            primaryResult.toItem(),
            shape,
            ingredients,
            extraResults,
            mirror,
            recipeUnlockingRequirement
        )
    }

    private fun parseRecipeItem(data: Map<String, Any>): ItemDescriptor {
        val itemDescriptorType =
            if (data.containsKey("tag")) ItemDescriptorType.ITEM_TAG else ItemDescriptorType.DEFAULT
        return when (itemDescriptorType) {
            ItemDescriptorType.DEFAULT -> {
                val item: Item
                val name = data["item"].toString()

                val count = if (data.containsKey("count")) Utils.toInt(data["count"]!!) else 1

                val nbt = data["nbt"] as String?
                val nbtBytes: ByteArray? = if (nbt != null) Base64.getDecoder()
                    .decode(nbt) else EmptyArrays.EMPTY_BYTES //TODO: idk how to fix nbt, cuz we don't use Cloudburst NBT

                var meta: Int? = null
                if (data.containsKey("data")) meta = Utils.toInt(data["data"]!!)

                //normal item
                if (meta != null) {
                    if (meta == Short.MAX_VALUE.toInt() || meta == -1) {
                        item = get(name, 0, count, nbtBytes, false)
                        item.disableMeta()
                    } else {
                        item = get(name, meta, count, nbtBytes, false)
                    }
                } else {
                    item = get(name, 0, count, nbtBytes, false)
                }
                DefaultDescriptor(item)
            }

            ItemDescriptorType.ITEM_TAG -> {
                val itemTag = data["tag"].toString()
                val count = if (data.containsKey("count")) Utils.toInt(data["count"]!!) else 1
                ItemTagDescriptor(itemTag, count)
            }

            else -> throw IllegalStateException("Unexpected value: $itemDescriptorType")
        }
    }

    companion object : Loggable {
        private val isLoad = AtomicBoolean(false)
        var recipeCount: Int = 0
            private set

        /**
         * 缓存着配方数据包
         */
        private var buffer: ByteBuf? = null
        fun computeRecipeIdWithItem(results: Collection<Item>, inputs: Collection<Item>, type: RecipeType): String {
            val inputs1: List<Item> = ArrayList(inputs)
            return computeRecipeId(results, inputs1.stream().map { item: Item? ->
                DefaultDescriptor(
                    item!!
                )
            }.toList(), type)
        }

        fun computeRecipeId(results: Collection<Item>, inputs: Collection<ItemDescriptor>, type: RecipeType): String {
            val builder = StringBuilder()
            val first = results.stream().findFirst()
            first.ifPresent { item: Item ->
                builder.append(Identifier(item.id).path)
                    .append('_')
                    .append(item.getCount())
                    .append('_')
                    .append(if (item.isBlock()) item.getSafeBlockState().specialValue().toInt() else item.damage)
                    .append("_from_")
            }
            var limit = 5
            for (des in inputs) {
                if ((limit--) == 0) {
                    break
                }
                if (des is ItemTagDescriptor) {
                    builder.append("tag_").append(des.itemTag).append("_and_")
                } else if (des is DefaultDescriptor) {
                    val item = des.item
                    builder.append(Identifier(item.id).path)
                        .append('_')
                        .append(item.getCount())
                        .append('_')
                        .append(
                            if (item.damage != 0) item.damage else if (item.isBlock()) item.getSafeBlockState()
                                .blockStateHash() else 0
                        )
                        .append("_and_")
                }
            }
            val r = builder.toString()
            return r.substring(0, r.lastIndexOf("_and_")) + "_" + type.name.lowercase()
        }

        fun setCraftingPacket(craftingPacket: ByteBuf) {
            ReferenceCountUtil.safeRelease(buffer)
            buffer = craftingPacket.retain()
        }
    }
}
