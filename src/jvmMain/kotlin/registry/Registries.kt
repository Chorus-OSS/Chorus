package org.chorus_oss.chorus.registry

object Registries {
    @JvmField
    val POTION: PotionRegistry = PotionRegistry()

    @JvmField
    val ENTITY: EntityRegistry = EntityRegistry()

    @JvmField
    val BLOCKENTITY: BlockEntityRegistry = BlockEntityRegistry()

    @JvmField
    val BLOCKSTATE_ITEMMETA: BlockState2ItemMetaRegistry = BlockState2ItemMetaRegistry()

    @JvmField
    val BLOCKSTATE: BlockStateRegistry = BlockStateRegistry()

    @JvmField
    val ITEM_RUNTIMEID: ItemRuntimeIdRegistry = ItemRuntimeIdRegistry()

    @JvmField
    val BLOCK: BlockRegistry = BlockRegistry()

    @JvmField
    val ITEM: ItemRegistry = ItemRegistry()

    @JvmField
    val CREATIVE: CreativeItemRegistry = CreativeItemRegistry()

    @JvmField
    val BIOME: BiomeRegistry = BiomeRegistry()

    @JvmField
    val FUEL: FuelRegistry = FuelRegistry()

    @JvmField
    val GENERATOR: GeneratorRegistry = GeneratorRegistry()

    @JvmField
    val GENERATE_STAGE: GenerateStageRegistry = GenerateStageRegistry()

    @JvmField
    val EFFECT: EffectRegistry = EffectRegistry()

    @JvmField
    val RECIPE: RecipeRegistry = RecipeRegistry()
}
