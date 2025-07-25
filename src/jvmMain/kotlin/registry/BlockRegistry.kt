package org.chorus_oss.chorus.registry

import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.block.customblock.CustomBlockDefinition
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.plugin.Plugin
import org.chorus_oss.chorus.utils.Loggable
import org.jetbrains.annotations.UnmodifiableView
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

class BlockRegistry : IRegistry<String, Block?, KClass<out Block>>, Loggable {
    override fun init() {
        if (isLoad.getAndSet(true)) return
        try {
            register(BlockID.ACACIA_BUTTON, BlockAcaciaButton::class)
            register(BlockID.ACACIA_DOOR, BlockAcaciaDoor::class)
            register(BlockID.ACACIA_DOUBLE_SLAB, BlockAcaciaDoubleSlab::class)
            register(BlockID.ACACIA_FENCE, BlockAcaciaFence::class)
            register(BlockID.ACACIA_FENCE_GATE, BlockAcaciaFenceGate::class)
            register(BlockID.ACACIA_HANGING_SIGN, BlockAcaciaHangingSign::class)
            register(BlockID.ACACIA_LEAVES, BlockAcaciaLeaves::class)
            register(BlockID.ACACIA_LOG, BlockAcaciaLog::class)
            register(BlockID.ACACIA_PLANKS, BlockAcaciaPlanks::class)
            register(BlockID.ACACIA_PRESSURE_PLATE, BlockAcaciaPressurePlate::class)
            register(BlockID.ACACIA_SAPLING, BlockAcaciaSapling::class)
            register(BlockID.ACACIA_SLAB, BlockAcaciaSlab::class)
            register(BlockID.ACACIA_STAIRS, BlockAcaciaStairs::class)
            register(BlockID.ACACIA_STANDING_SIGN, BlockAcaciaStandingSign::class)
            register(BlockID.ACACIA_TRAPDOOR, BlockAcaciaTrapdoor::class)
            register(BlockID.ACACIA_WALL_SIGN, BlockAcaciaWallSign::class)
            register(BlockID.ACACIA_WOOD, BlockAcaciaWood::class)
            register(BlockID.ACTIVATOR_RAIL, BlockActivatorRail::class)
            register(BlockID.AIR, BlockAir::class)
            register(BlockID.ALLIUM, BlockAllium::class)
            register(BlockID.ALLOW, BlockAllow::class)
            register(BlockID.AMETHYST_BLOCK, BlockAmethystBlock::class)
            register(BlockID.AMETHYST_CLUSTER, BlockAmethystCluster::class)
            register(BlockID.ANCIENT_DEBRIS, BlockAncientDebris::class)
            register(BlockID.ANDESITE, BlockAndesite::class)
            register(BlockID.ANDESITE_DOUBLE_SLAB, BlockAndesiteDoubleSlab::class)
            register(BlockID.ANDESITE_SLAB, BlockAndesiteSlab::class)
            register(BlockID.ANDESITE_STAIRS, BlockAndesiteStairs::class)
            register(BlockID.ANDESITE_WALL, BlockAndesiteWall::class)
            register(BlockID.ANVIL, BlockAnvil::class)
            register(BlockID.AZALEA, BlockAzalea::class)
            register(BlockID.AZALEA_LEAVES, BlockAzaleaLeaves::class)
            register(BlockID.AZALEA_LEAVES_FLOWERED, BlockAzaleaLeavesFlowered::class)
            register(BlockID.AZURE_BLUET, BlockAzureBluet::class)
            register(BlockID.BAMBOO, BlockBamboo::class)
            register(BlockID.BAMBOO_BLOCK, BlockBambooBlock::class)
            register(BlockID.BAMBOO_BUTTON, BlockBambooButton::class)
            register(BlockID.BAMBOO_DOOR, BlockBambooDoor::class)
            register(BlockID.BAMBOO_DOUBLE_SLAB, BlockBambooDoubleSlab::class)
            register(BlockID.BAMBOO_FENCE, BlockBambooFence::class)
            register(BlockID.BAMBOO_FENCE_GATE, BlockBambooFenceGate::class)
            register(BlockID.BAMBOO_HANGING_SIGN, BlockBambooHangingSign::class)
            register(BlockID.BAMBOO_MOSAIC, BlockBambooMosaic::class)
            register(BlockID.BAMBOO_MOSAIC_DOUBLE_SLAB, BlockBambooMosaicDoubleSlab::class)
            register(BlockID.BAMBOO_MOSAIC_SLAB, BlockBambooMosaicSlab::class)
            register(BlockID.BAMBOO_MOSAIC_STAIRS, BlockBambooMosaicStairs::class)
            register(BlockID.BAMBOO_PLANKS, BlockBambooPlanks::class)
            register(BlockID.BAMBOO_PRESSURE_PLATE, BlockBambooPressurePlate::class)
            register(BlockID.BAMBOO_SAPLING, BlockBambooSapling::class)
            register(BlockID.BAMBOO_SLAB, BlockBambooSlab::class)
            register(BlockID.BAMBOO_STAIRS, BlockBambooStairs::class)
            register(BlockID.BAMBOO_STANDING_SIGN, BlockBambooStandingSign::class)
            register(BlockID.BAMBOO_TRAPDOOR, BlockBambooTrapdoor::class)
            register(BlockID.BAMBOO_WALL_SIGN, BlockBambooWallSign::class)
            register(BlockID.BARREL, BlockBarrel::class)
            register(BlockID.BARRIER, BlockBarrier::class)
            register(BlockID.BASALT, BlockBasalt::class)
            register(BlockID.BEACON, BlockBeacon::class)
            register(BlockID.BED, BlockBed::class)
            register(BlockID.BEDROCK, BlockBedrock::class)
            register(BlockID.BEE_NEST, BlockBeeNest::class)
            register(BlockID.BEEHIVE, BlockBeehive::class)
            register(BlockID.BEETROOT, BlockBeetroot::class)
            register(BlockID.BELL, BlockBell::class)
            register(BlockID.BIG_DRIPLEAF, BlockBigDripleaf::class)
            register(BlockID.BIRCH_BUTTON, BlockBirchButton::class)
            register(BlockID.BIRCH_DOOR, BlockBirchDoor::class)
            register(BlockID.BIRCH_DOUBLE_SLAB, BlockBirchDoubleSlab::class)
            register(BlockID.BIRCH_FENCE, BlockBirchFence::class)
            register(BlockID.BIRCH_FENCE_GATE, BlockBirchFenceGate::class)
            register(BlockID.BIRCH_HANGING_SIGN, BlockBirchHangingSign::class)
            register(BlockID.BIRCH_LEAVES, BlockBirchLeaves::class)
            register(BlockID.BIRCH_LOG, BlockBirchLog::class)
            register(BlockID.BIRCH_PLANKS, BlockBirchPlanks::class)
            register(BlockID.BIRCH_PRESSURE_PLATE, BlockBirchPressurePlate::class)
            register(BlockID.BIRCH_SAPLING, BlockBirchSapling::class)
            register(BlockID.BIRCH_SLAB, BlockBirchSlab::class)
            register(BlockID.BIRCH_STAIRS, BlockBirchStairs::class)
            register(BlockID.BIRCH_STANDING_SIGN, BlockBirchStandingSign::class)
            register(BlockID.BIRCH_TRAPDOOR, BlockBirchTrapdoor::class)
            register(BlockID.BIRCH_WALL_SIGN, BlockBirchWallSign::class)
            register(BlockID.BIRCH_WOOD, BlockBirchWood::class)
            register(BlockID.BLACK_CANDLE, BlockBlackCandle::class)
            register(BlockID.BLACK_CANDLE_CAKE, BlockBlackCandleCake::class)
            register(BlockID.BLACK_CARPET, BlockBlackCarpet::class)
            register(BlockID.BLACK_CONCRETE, BlockBlackConcrete::class)
            register(BlockID.BLACK_CONCRETE_POWDER, BlockBlackConcretePowder::class)
            register(BlockID.BLACK_GLAZED_TERRACOTTA, BlockBlackGlazedTerracotta::class)
            register(BlockID.BLACK_SHULKER_BOX, BlockBlackShulkerBox::class)
            register(BlockID.BLACK_STAINED_GLASS, BlockBlackStainedGlass::class)
            register(BlockID.BLACK_STAINED_GLASS_PANE, BlockBlackStainedGlassPane::class)
            register(BlockID.BLACK_TERRACOTTA, BlockBlackTerracotta::class)
            register(BlockID.BLACK_WOOL, BlockBlackWool::class)
            register(BlockID.BLACKSTONE, BlockBlackstone::class)
            register(BlockID.BLACKSTONE_DOUBLE_SLAB, BlockBlackstoneDoubleSlab::class)
            register(BlockID.BLACKSTONE_SLAB, BlockBlackstoneSlab::class)
            register(BlockID.BLACKSTONE_STAIRS, BlockBlackstoneStairs::class)
            register(BlockID.BLACKSTONE_WALL, BlockBlackstoneWall::class)
            register(BlockID.BLAST_FURNACE, BlockBlastFurnace::class)
            register(BlockID.BLUE_CANDLE, BlockBlueCandle::class)
            register(BlockID.BLUE_CANDLE_CAKE, BlockBlueCandleCake::class)
            register(BlockID.BLUE_CARPET, BlockBlueCarpet::class)
            register(BlockID.BLUE_CONCRETE, BlockBlueConcrete::class)
            register(BlockID.BLUE_CONCRETE_POWDER, BlockBlueConcretePowder::class)
            register(BlockID.BLUE_GLAZED_TERRACOTTA, BlockBlueGlazedTerracotta::class)
            register(BlockID.BLUE_ICE, BlockBlueIce::class)
            register(BlockID.BLUE_ORCHID, BlockBlueOrchid::class)
            register(BlockID.BLUE_SHULKER_BOX, BlockBlueShulkerBox::class)
            register(BlockID.BLUE_STAINED_GLASS, BlockBlueStainedGlass::class)
            register(BlockID.BLUE_STAINED_GLASS_PANE, BlockBlueStainedGlassPane::class)
            register(BlockID.BLUE_TERRACOTTA, BlockBlueTerracotta::class)
            register(BlockID.BLUE_WOOL, BlockBlueWool::class)
            register(BlockID.BONE_BLOCK, BlockBoneBlock::class)
            register(BlockID.BOOKSHELF, BlockBookshelf::class)
            register(BlockID.BORDER_BLOCK, BlockBorderBlock::class)
            register(BlockID.BRAIN_CORAL, BlockBrainCoral::class)
            register(BlockID.BRAIN_CORAL_BLOCK, BlockBrainCoralBlock::class)
            register(BlockID.BRAIN_CORAL_FAN, BlockBrainCoralFan::class)
            register(BlockID.BRAIN_CORAL_WALL_FAN, BlockBrainCoralWallFan::class)
            register(BlockID.BREWING_STAND, BlockBrewingStand::class)
            register(BlockID.BRICK_BLOCK, BlockBrickBlock::class)
            register(BlockID.BRICK_DOUBLE_SLAB, BlockBrickDoubleSlab::class)
            register(BlockID.BRICK_SLAB, BlockBrickSlab::class)
            register(BlockID.BRICK_STAIRS, BlockBrickStairs::class)
            register(BlockID.BRICK_WALL, BlockBrickWall::class)
            register(BlockID.BROWN_CANDLE, BlockBrownCandle::class)
            register(BlockID.BROWN_CANDLE_CAKE, BlockBrownCandleCake::class)
            register(BlockID.BROWN_CARPET, BlockBrownCarpet::class)
            register(BlockID.BROWN_CONCRETE, BlockBrownConcrete::class)
            register(BlockID.BROWN_CONCRETE_POWDER, BlockBrownConcretePowder::class)
            register(BlockID.BROWN_GLAZED_TERRACOTTA, BlockBrownGlazedTerracotta::class)
            register(BlockID.BROWN_MUSHROOM, BlockBrownMushroom::class)
            register(BlockID.BROWN_MUSHROOM_BLOCK, BlockBrownMushroomBlock::class)
            register(BlockID.BROWN_SHULKER_BOX, BlockBrownShulkerBox::class)
            register(BlockID.BROWN_STAINED_GLASS, BlockBrownStainedGlass::class)
            register(BlockID.BROWN_STAINED_GLASS_PANE, BlockBrownStainedGlassPane::class)
            register(BlockID.BROWN_TERRACOTTA, BlockBrownTerracotta::class)
            register(BlockID.BROWN_WOOL, BlockBrownWool::class)
            register(BlockID.BUBBLE_COLUMN, BlockBubbleColumn::class)
            register(BlockID.BUBBLE_CORAL, BlockBubbleCoral::class)
            register(BlockID.BUBBLE_CORAL_BLOCK, BlockBubbleCoralBlock::class)
            register(BlockID.BUBBLE_CORAL_FAN, BlockBubbleCoralFan::class)
            register(BlockID.BUBBLE_CORAL_WALL_FAN, BlockBubbleCoralWallFan::class)
            register(BlockID.BUDDING_AMETHYST, BlockBuddingAmethyst::class)
            register(BlockID.BUSH, BlockBush::class)
            register(BlockID.CACTUS, BlockCactus::class)
            register(BlockID.CACTUS_FLOWER, BlockCactusFlower::class)
            register(BlockID.CAKE, BlockCake::class)
            register(BlockID.CALCITE, BlockCalcite::class)
            register(BlockID.CALIBRATED_SCULK_SENSOR, BlockCalibratedSculkSensor::class)
            register(BlockID.CAMPFIRE, BlockCampfire::class)
            register(BlockID.CANDLE, BlockCandle::class)
            register(BlockID.CANDLE_CAKE, BlockCandleCake::class)
            register(BlockID.CARROTS, BlockCarrots::class)
            register(BlockID.CARTOGRAPHY_TABLE, BlockCartographyTable::class)
            register(BlockID.CARVED_PUMPKIN, BlockCarvedPumpkin::class)
            register(BlockID.CAULDRON, BlockCauldron::class)
            register(BlockID.CAVE_VINES, BlockCaveVines::class)
            register(BlockID.CAVE_VINES_BODY_WITH_BERRIES, BlockCaveVinesBodyWithBerries::class)
            register(BlockID.CAVE_VINES_HEAD_WITH_BERRIES, BlockCaveVinesHeadWithBerries::class)
            register(BlockID.CHAIN, BlockChain::class)
            register(BlockID.CHAIN_COMMAND_BLOCK, BlockChainCommandBlock::class)
            register(BlockID.CHERRY_BUTTON, BlockCherryButton::class)
            register(BlockID.CHERRY_DOOR, BlockCherryDoor::class)
            register(BlockID.CHERRY_DOUBLE_SLAB, BlockCherryDoubleSlab::class)
            register(BlockID.CHERRY_FENCE, BlockCherryFence::class)
            register(BlockID.CHERRY_FENCE_GATE, BlockCherryFenceGate::class)
            register(BlockID.CHERRY_HANGING_SIGN, BlockCherryHangingSign::class)
            register(BlockID.CHERRY_LEAVES, BlockCherryLeaves::class)
            register(BlockID.CHERRY_LOG, BlockCherryLog::class)
            register(BlockID.CHERRY_PLANKS, BlockCherryPlanks::class)
            register(BlockID.CHERRY_PRESSURE_PLATE, BlockCherryPressurePlate::class)
            register(BlockID.CHERRY_SAPLING, BlockCherrySapling::class)
            register(BlockID.CHERRY_SLAB, BlockCherrySlab::class)
            register(BlockID.CHERRY_STAIRS, BlockCherryStairs::class)
            register(BlockID.CHERRY_STANDING_SIGN, BlockCherryStandingSign::class)
            register(BlockID.CHERRY_TRAPDOOR, BlockCherryTrapdoor::class)
            register(BlockID.CHERRY_WALL_SIGN, BlockCherryWallSign::class)
            register(BlockID.CHERRY_WOOD, BlockCherryWood::class)
            register(BlockID.CHEST, BlockChest::class)
            register(BlockID.CHIPPED_ANVIL, BlockChippedAnvil::class)
            register(BlockID.CHISELED_BOOKSHELF, BlockChiseledBookshelf::class)
            register(BlockID.CHISELED_COPPER, BlockChiseledCopper::class)
            register(BlockID.CHISELED_DEEPSLATE, BlockChiseledDeepslate::class)
            register(BlockID.CHISELED_NETHER_BRICKS, BlockChiseledNetherBricks::class)
            register(BlockID.CHISELED_POLISHED_BLACKSTONE, BlockChiseledPolishedBlackstone::class)
            register(BlockID.CHISELED_QUARTZ_BLOCK, BlockChiseledQuartzBlock::class)
            register(BlockID.CHISELED_RED_SANDSTONE, BlockChiseledRedSandstone::class)
            register(BlockID.CHISELED_RESIN_BRICKS, BlockChiseledResinBricks::class)
            register(BlockID.CHISELED_SANDSTONE, BlockChiseledSandstone::class)
            register(BlockID.CHISELED_STONE_BRICKS, BlockChiseledStoneBricks::class)
            register(BlockID.CHISELED_TUFF, BlockChiseledTuff::class)
            register(BlockID.CHISELED_TUFF_BRICKS, BlockChiseledTuffBricks::class)
            register(BlockID.CHORUS_FLOWER, BlockChorusFlower::class)
            register(BlockID.CHORUS_PLANT, BlockChorusPlant::class)
            register(BlockID.CLAY, BlockClay::class)
            register(BlockID.CLIENT_REQUEST_PLACEHOLDER_BLOCK, BlockClientRequestPlaceholderBlock::class)
            register(BlockID.CLOSED_EYEBLOSSOM, BlockClosedEyeblossom::class)
            register(BlockID.COAL_BLOCK, BlockCoalBlock::class)
            register(BlockID.COAL_ORE, BlockCoalOre::class)
            register(BlockID.COARSE_DIRT, BlockCoarseDirt::class)
            register(BlockID.COBBLED_DEEPSLATE, BlockCobbledDeepslate::class)
            register(BlockID.COBBLED_DEEPSLATE_DOUBLE_SLAB, BlockCobbledDeepslateDoubleSlab::class)
            register(BlockID.COBBLED_DEEPSLATE_SLAB, BlockCobbledDeepslateSlab::class)
            register(BlockID.COBBLED_DEEPSLATE_STAIRS, BlockCobbledDeepslateStairs::class)
            register(BlockID.COBBLED_DEEPSLATE_WALL, BlockCobbledDeepslateWall::class)
            register(BlockID.COBBLESTONE, BlockCobblestone::class)
            register(BlockID.COBBLESTONE_DOUBLE_SLAB, BlockCobblestoneDoubleSlab::class)
            register(BlockID.COBBLESTONE_SLAB, BlockCobblestoneSlab::class)
            register(BlockID.COBBLESTONE_WALL, BlockCobblestoneWall::class)
            register(BlockID.COCOA, BlockCocoa::class)
            register(BlockID.COMMAND_BLOCK, BlockCommandBlock::class)
            register(BlockID.COMPOSTER, BlockComposter::class)
            register(BlockID.CONDUIT, BlockConduit::class)
            register(BlockID.COPPER_BLOCK, BlockCopperBlock::class)
            register(BlockID.COPPER_BULB, BlockCopperBulb::class)
            register(BlockID.COPPER_DOOR, BlockCopperDoor::class)
            register(BlockID.COPPER_GRATE, BlockCopperGrate::class)
            register(BlockID.COPPER_ORE, BlockCopperOre::class)
            register(BlockID.COPPER_TRAPDOOR, BlockCopperTrapdoor::class)
            register(BlockID.CORNFLOWER, BlockCornflower::class)
            register(BlockID.CRACKED_DEEPSLATE_BRICKS, BlockCrackedDeepslateBricks::class)
            register(BlockID.CRACKED_DEEPSLATE_TILES, BlockCrackedDeepslateTiles::class)
            register(BlockID.CRACKED_NETHER_BRICKS, BlockCrackedNetherBricks::class)
            register(BlockID.CRACKED_POLISHED_BLACKSTONE_BRICKS, BlockCrackedPolishedBlackstoneBricks::class)
            register(BlockID.CRACKED_STONE_BRICKS, BlockCrackedStoneBricks::class)
            register(BlockID.CRAFTER, BlockCrafter::class)
            register(BlockID.CRAFTING_TABLE, BlockCraftingTable::class)
            register(BlockID.CREAKING_HEART, BlockCreakingHeart::class)
            register(BlockID.CREEPER_HEAD, BlockCreeperHead::class)
            register(BlockID.CRIMSON_BUTTON, BlockCrimsonButton::class)
            register(BlockID.CRIMSON_DOOR, BlockCrimsonDoor::class)
            register(BlockID.CRIMSON_DOUBLE_SLAB, BlockCrimsonDoubleSlab::class)
            register(BlockID.CRIMSON_FENCE, BlockCrimsonFence::class)
            register(BlockID.CRIMSON_FENCE_GATE, BlockCrimsonFenceGate::class)
            register(BlockID.CRIMSON_FUNGUS, BlockCrimsonFungus::class)
            register(BlockID.CRIMSON_HANGING_SIGN, BlockCrimsonHangingSign::class)
            register(BlockID.CRIMSON_HYPHAE, BlockCrimsonHyphae::class)
            register(BlockID.CRIMSON_NYLIUM, BlockCrimsonNylium::class)
            register(BlockID.CRIMSON_PLANKS, BlockCrimsonPlanks::class)
            register(BlockID.CRIMSON_PRESSURE_PLATE, BlockCrimsonPressurePlate::class)
            register(BlockID.CRIMSON_ROOTS, BlockCrimsonRoots::class)
            register(BlockID.CRIMSON_SLAB, BlockCrimsonSlab::class)
            register(BlockID.CRIMSON_STAIRS, BlockCrimsonStairs::class)
            register(BlockID.CRIMSON_STANDING_SIGN, BlockCrimsonStandingSign::class)
            register(BlockID.CRIMSON_STEM, BlockCrimsonStem::class)
            register(BlockID.CRIMSON_TRAPDOOR, BlockCrimsonTrapdoor::class)
            register(BlockID.CRIMSON_WALL_SIGN, BlockCrimsonWallSign::class)
            register(BlockID.CRYING_OBSIDIAN, BlockCryingObsidian::class)
            register(BlockID.CUT_COPPER, BlockCutCopper::class)
            register(BlockID.CUT_COPPER_SLAB, BlockCutCopperSlab::class)
            register(BlockID.CUT_COPPER_STAIRS, BlockCutCopperStairs::class)
            register(BlockID.CUT_RED_SANDSTONE, BlockCutRedSandstone::class)
            register(BlockID.CUT_RED_SANDSTONE_DOUBLE_SLAB, BlockCutRedSandstoneDoubleSlab::class)
            register(BlockID.CUT_RED_SANDSTONE_SLAB, BlockCutRedSandstoneSlab::class)
            register(BlockID.CUT_SANDSTONE, BlockCutSandstone::class)
            register(BlockID.CUT_SANDSTONE_DOUBLE_SLAB, BlockCutSandstoneDoubleSlab::class)
            register(BlockID.CUT_SANDSTONE_SLAB, BlockCutSandstoneSlab::class)
            register(BlockID.CYAN_CANDLE, BlockCyanCandle::class)
            register(BlockID.CYAN_CANDLE_CAKE, BlockCyanCandleCake::class)
            register(BlockID.CYAN_CARPET, BlockCyanCarpet::class)
            register(BlockID.CYAN_CONCRETE, BlockCyanConcrete::class)
            register(BlockID.CYAN_CONCRETE_POWDER, BlockCyanConcretePowder::class)
            register(BlockID.CYAN_GLAZED_TERRACOTTA, BlockCyanGlazedTerracotta::class)
            register(BlockID.CYAN_SHULKER_BOX, BlockCyanShulkerBox::class)
            register(BlockID.CYAN_STAINED_GLASS, BlockCyanStainedGlass::class)
            register(BlockID.CYAN_STAINED_GLASS_PANE, BlockCyanStainedGlassPane::class)
            register(BlockID.CYAN_TERRACOTTA, BlockCyanTerracotta::class)
            register(BlockID.CYAN_WOOL, BlockCyanWool::class)
            register(BlockID.DAMAGED_ANVIL, BlockDamagedAnvil::class)
            register(BlockID.DARK_OAK_BUTTON, BlockDarkOakButton::class)
            register(BlockID.DARK_OAK_DOOR, BlockDarkOakDoor::class)
            register(BlockID.DARK_OAK_DOUBLE_SLAB, BlockDarkOakDoubleSlab::class)
            register(BlockID.DARK_OAK_FENCE, BlockDarkOakFence::class)
            register(BlockID.DARK_OAK_FENCE_GATE, BlockDarkOakFenceGate::class)
            register(BlockID.DARK_OAK_HANGING_SIGN, BlockDarkOakHangingSign::class)
            register(BlockID.DARK_OAK_LEAVES, BlockDarkOakLeaves::class)
            register(BlockID.DARK_OAK_LOG, BlockDarkOakLog::class)
            register(BlockID.DARK_OAK_PLANKS, BlockDarkOakPlanks::class)
            register(BlockID.DARK_OAK_PRESSURE_PLATE, BlockDarkOakPressurePlate::class)
            register(BlockID.DARK_OAK_SAPLING, BlockDarkOakSapling::class)
            register(BlockID.DARK_OAK_SLAB, BlockDarkOakSlab::class)
            register(BlockID.DARK_OAK_STAIRS, BlockDarkOakStairs::class)
            register(BlockID.DARK_OAK_TRAPDOOR, BlockDarkOakTrapdoor::class)
            register(BlockID.DARK_OAK_WOOD, BlockDarkOakWood::class)
            register(BlockID.DARK_PRISMARINE, BlockDarkPrismarine::class)
            register(BlockID.DARK_PRISMARINE_DOUBLE_SLAB, BlockDarkPrismarineDoubleSlab::class)
            register(BlockID.DARK_PRISMARINE_SLAB, BlockDarkPrismarineSlab::class)
            register(BlockID.DARK_PRISMARINE_STAIRS, BlockDarkPrismarineStairs::class)
            register(BlockID.DARKOAK_STANDING_SIGN, BlockDarkoakStandingSign::class)
            register(BlockID.DARKOAK_WALL_SIGN, BlockDarkoakWallSign::class)
            register(BlockID.DAYLIGHT_DETECTOR, BlockDaylightDetector::class)
            register(BlockID.DAYLIGHT_DETECTOR_INVERTED, BlockDaylightDetectorInverted::class)
            register(BlockID.DEAD_BRAIN_CORAL, BlockDeadBrainCoral::class)
            register(BlockID.DEAD_BRAIN_CORAL_BLOCK, BlockDeadBrainCoralBlock::class)
            register(BlockID.DEAD_BRAIN_CORAL_FAN, BlockDeadBrainCoralFan::class)
            register(BlockID.DEAD_BRAIN_CORAL_WALL_FAN, BlockDeadBrainCoralWallFan::class)
            register(BlockID.DEAD_BUBBLE_CORAL, BlockDeadBubbleCoral::class)
            register(BlockID.DEAD_BUBBLE_CORAL_BLOCK, BlockDeadBubbleCoralBlock::class)
            register(BlockID.DEAD_BUBBLE_CORAL_FAN, BlockDeadBubbleCoralFan::class)
            register(BlockID.DEAD_BUBBLE_CORAL_WALL_FAN, BlockDeadBubbleCoralWallFan::class)
            register(BlockID.DEAD_FIRE_CORAL, BlockDeadFireCoral::class)
            register(BlockID.DEAD_FIRE_CORAL_BLOCK, BlockDeadFireCoralBlock::class)
            register(BlockID.DEAD_FIRE_CORAL_FAN, BlockDeadFireCoralFan::class)
            register(BlockID.DEAD_FIRE_CORAL_WALL_FAN, BlockDeadFireCoralWallFan::class)
            register(BlockID.DEAD_HORN_CORAL, BlockDeadHornCoral::class)
            register(BlockID.DEAD_HORN_CORAL_BLOCK, BlockDeadHornCoralBlock::class)
            register(BlockID.DEAD_HORN_CORAL_FAN, BlockDeadHornCoralFan::class)
            register(BlockID.DEAD_HORN_CORAL_WALL_FAN, BlockDeadHornCoralWallFan::class)
            register(BlockID.DEAD_TUBE_CORAL, BlockDeadTubeCoral::class)
            register(BlockID.DEAD_TUBE_CORAL_BLOCK, BlockDeadTubeCoralBlock::class)
            register(BlockID.DEAD_TUBE_CORAL_FAN, BlockDeadTubeCoralFan::class)
            register(BlockID.DEAD_TUBE_CORAL_WALL_FAN, BlockDeadTubeCoralWallFan::class)
            register(BlockID.DEADBUSH, BlockDeadbush::class)
            register(BlockID.DECORATED_POT, BlockDecoratedPot::class)
            register(BlockID.DEEPSLATE, BlockDeepslate::class)
            register(BlockID.DEEPSLATE_BRICK_DOUBLE_SLAB, BlockDeepslateBrickDoubleSlab::class)
            register(BlockID.DEEPSLATE_BRICK_SLAB, BlockDeepslateBrickSlab::class)
            register(BlockID.DEEPSLATE_BRICK_STAIRS, BlockDeepslateBrickStairs::class)
            register(BlockID.DEEPSLATE_BRICK_WALL, BlockDeepslateBrickWall::class)
            register(BlockID.DEEPSLATE_BRICKS, BlockDeepslateBricks::class)
            register(BlockID.DEEPSLATE_COAL_ORE, BlockDeepslateCoalOre::class)
            register(BlockID.DEEPSLATE_COPPER_ORE, BlockDeepslateCopperOre::class)
            register(BlockID.DEEPSLATE_DIAMOND_ORE, BlockDeepslateDiamondOre::class)
            register(BlockID.DEEPSLATE_EMERALD_ORE, BlockDeepslateEmeraldOre::class)
            register(BlockID.DEEPSLATE_GOLD_ORE, BlockDeepslateGoldOre::class)
            register(BlockID.DEEPSLATE_IRON_ORE, BlockDeepslateIronOre::class)
            register(BlockID.DEEPSLATE_LAPIS_ORE, BlockDeepslateLapisOre::class)
            register(BlockID.DEEPSLATE_REDSTONE_ORE, BlockDeepslateRedstoneOre::class)
            register(BlockID.DEEPSLATE_TILE_DOUBLE_SLAB, BlockDeepslateTileDoubleSlab::class)
            register(BlockID.DEEPSLATE_TILE_SLAB, BlockDeepslateTileSlab::class)
            register(BlockID.DEEPSLATE_TILE_STAIRS, BlockDeepslateTileStairs::class)
            register(BlockID.DEEPSLATE_TILE_WALL, BlockDeepslateTileWall::class)
            register(BlockID.DEEPSLATE_TILES, BlockDeepslateTiles::class)
            register(BlockID.DENY, BlockDeny::class)
            register(BlockID.DETECTOR_RAIL, BlockDetectorRail::class)
            register(BlockID.DIAMOND_BLOCK, BlockDiamondBlock::class)
            register(BlockID.DIAMOND_ORE, BlockDiamondOre::class)
            register(BlockID.DIORITE, BlockDiorite::class)
            register(BlockID.DIORITE_DOUBLE_SLAB, BlockDioriteDoubleSlab::class)
            register(BlockID.DIORITE_SLAB, BlockDioriteSlab::class)
            register(BlockID.DIORITE_STAIRS, BlockDioriteStairs::class)
            register(BlockID.DIORITE_WALL, BlockDioriteWall::class)
            register(BlockID.DIRT, BlockDirt::class)
            register(BlockID.DIRT_WITH_ROOTS, BlockDirtWithRoots::class)
            register(BlockID.DISPENSER, BlockDispenser::class)
            register(BlockID.DOUBLE_CUT_COPPER_SLAB, BlockDoubleCutCopperSlab::class)
            register(BlockID.DRAGON_EGG, BlockDragonEgg::class)
            register(BlockID.DRAGON_HEAD, BlockDragonHead::class)
            register(BlockID.DRIED_GHAST, BlockDriedGhast::class)
            register(BlockID.DRIED_KELP_BLOCK, BlockDriedKelpBlock::class)
            register(BlockID.DRIPSTONE_BLOCK, BlockDripstoneBlock::class)
            register(BlockID.DROPPER, BlockDropper::class)
            register(BlockID.EMERALD_BLOCK, BlockEmeraldBlock::class)
            register(BlockID.EMERALD_ORE, BlockEmeraldOre::class)
            register(BlockID.ENCHANTING_TABLE, BlockEnchantingTable::class)
            register(BlockID.END_BRICK_STAIRS, BlockEndBrickStairs::class)
            register(BlockID.END_BRICKS, BlockEndBricks::class)
            register(BlockID.END_GATEWAY, BlockEndGateway::class)
            register(BlockID.END_PORTAL, BlockEndPortal::class)
            register(BlockID.END_PORTAL_FRAME, BlockEndPortalFrame::class)
            register(BlockID.END_ROD, BlockEndRod::class)
            register(BlockID.END_STONE, BlockEndStone::class)
            register(BlockID.END_STONE_BRICK_DOUBLE_SLAB, BlockEndStoneBrickDoubleSlab::class)
            register(BlockID.END_STONE_BRICK_SLAB, BlockEndStoneBrickSlab::class)
            register(BlockID.END_STONE_BRICK_WALL, BlockEndStoneBrickWall::class)
            register(BlockID.ENDER_CHEST, BlockEnderChest::class)
            register(BlockID.EXPOSED_CHISELED_COPPER, BlockExposedChiseledCopper::class)
            register(BlockID.EXPOSED_COPPER, BlockExposedCopper::class)
            register(BlockID.EXPOSED_COPPER_BULB, BlockExposedCopperBulb::class)
            register(BlockID.EXPOSED_COPPER_DOOR, BlockExposedCopperDoor::class)
            register(BlockID.EXPOSED_COPPER_GRATE, BlockExposedCopperGrate::class)
            register(BlockID.EXPOSED_COPPER_TRAPDOOR, BlockExposedCopperTrapdoor::class)
            register(BlockID.EXPOSED_CUT_COPPER, BlockExposedCutCopper::class)
            register(BlockID.EXPOSED_CUT_COPPER_SLAB, BlockExposedCutCopperSlab::class)
            register(BlockID.EXPOSED_CUT_COPPER_STAIRS, BlockExposedCutCopperStairs::class)
            register(BlockID.EXPOSED_DOUBLE_CUT_COPPER_SLAB, BlockExposedDoubleCutCopperSlab::class)
            register(BlockID.FARMLAND, BlockFarmland::class)
            register(BlockID.FENCE_GATE, BlockFenceGate::class)
            register(BlockID.FERN, BlockFern::class)
            register(BlockID.FIRE, BlockFire::class)
            register(BlockID.FIREFLY_BUSH, BlockFireflyBush::class)
            register(BlockID.FIRE_CORAL, BlockFireCoral::class)
            register(BlockID.FIRE_CORAL_BLOCK, BlockFireCoralBlock::class)
            register(BlockID.FIRE_CORAL_FAN, BlockFireCoralFan::class)
            register(BlockID.FIRE_CORAL_WALL_FAN, BlockFireCoralWallFan::class)
            register(BlockID.FLETCHING_TABLE, BlockFletchingTable::class)
            register(BlockID.FLOWER_POT, BlockFlowerPot::class)
            register(BlockID.FLOWERING_AZALEA, BlockFloweringAzalea::class)
            register(BlockID.FLOWING_LAVA, BlockFlowingLava::class)
            register(BlockID.FLOWING_WATER, BlockFlowingWater::class)
            register(BlockID.FRAME, BlockFrame::class)
            register(BlockID.FROG_SPAWN, BlockFrogSpawn::class)
            register(BlockID.FROSTED_ICE, BlockFrostedIce::class)
            register(BlockID.FURNACE, BlockFurnace::class)
            register(BlockID.GILDED_BLACKSTONE, BlockGildedBlackstone::class)
            register(BlockID.GLASS, BlockGlass::class)
            register(BlockID.GLASS_PANE, BlockGlassPane::class)
            register(BlockID.GLOW_FRAME, BlockGlowFrame::class)
            register(BlockID.GLOW_LICHEN, BlockGlowLichen::class)
            register(BlockID.GLOWINGOBSIDIAN, BlockGlowingobsidian::class)
            register(BlockID.GLOWSTONE, BlockGlowstone::class)
            register(BlockID.GOLD_BLOCK, BlockGoldBlock::class)
            register(BlockID.GOLD_ORE, BlockGoldOre::class)
            register(BlockID.GOLDEN_RAIL, BlockGoldenRail::class)
            register(BlockID.GRANITE, BlockGranite::class)
            register(BlockID.GRANITE_DOUBLE_SLAB, BlockGraniteDoubleSlab::class)
            register(BlockID.GRANITE_SLAB, BlockGraniteSlab::class)
            register(BlockID.GRANITE_STAIRS, BlockGraniteStairs::class)
            register(BlockID.GRANITE_WALL, BlockGraniteWall::class)
            register(BlockID.GRASS_BLOCK, BlockGrassBlock::class)
            register(BlockID.GRASS_PATH, BlockGrassPath::class)
            register(BlockID.GRAVEL, BlockGravel::class)
            register(BlockID.GRAY_CANDLE, BlockGrayCandle::class)
            register(BlockID.GRAY_CANDLE_CAKE, BlockGrayCandleCake::class)
            register(BlockID.GRAY_CARPET, BlockGrayCarpet::class)
            register(BlockID.GRAY_CONCRETE, BlockGrayConcrete::class)
            register(BlockID.GRAY_CONCRETE_POWDER, BlockGrayConcretePowder::class)
            register(BlockID.GRAY_GLAZED_TERRACOTTA, BlockGrayGlazedTerracotta::class)
            register(BlockID.GRAY_SHULKER_BOX, BlockGrayShulkerBox::class)
            register(BlockID.GRAY_STAINED_GLASS, BlockGrayStainedGlass::class)
            register(BlockID.GRAY_STAINED_GLASS_PANE, BlockGrayStainedGlassPane::class)
            register(BlockID.GRAY_TERRACOTTA, BlockGrayTerracotta::class)
            register(BlockID.GRAY_WOOL, BlockGrayWool::class)
            register(BlockID.GREEN_CANDLE, BlockGreenCandle::class)
            register(BlockID.GREEN_CANDLE_CAKE, BlockGreenCandleCake::class)
            register(BlockID.GREEN_CARPET, BlockGreenCarpet::class)
            register(BlockID.GREEN_CONCRETE, BlockGreenConcrete::class)
            register(BlockID.GREEN_CONCRETE_POWDER, BlockGreenConcretePowder::class)
            register(BlockID.GREEN_GLAZED_TERRACOTTA, BlockGreenGlazedTerracotta::class)
            register(BlockID.GREEN_SHULKER_BOX, BlockGreenShulkerBox::class)
            register(BlockID.GREEN_STAINED_GLASS, BlockGreenStainedGlass::class)
            register(BlockID.GREEN_STAINED_GLASS_PANE, BlockGreenStainedGlassPane::class)
            register(BlockID.GREEN_TERRACOTTA, BlockGreenTerracotta::class)
            register(BlockID.GREEN_WOOL, BlockGreenWool::class)
            register(BlockID.GRINDSTONE, BlockGrindstone::class)
            register(BlockID.HANGING_ROOTS, BlockHangingRoots::class)
            register(BlockID.HARDENED_CLAY, BlockHardenedClay::class)
            register(BlockID.HAY_BLOCK, BlockHayBlock::class)
            register(BlockID.HEAVY_CORE, BlockHeavyCore::class)
            register(BlockID.HEAVY_WEIGHTED_PRESSURE_PLATE, BlockHeavyWeightedPressurePlate::class)
            register(BlockID.HONEY_BLOCK, BlockHoneyBlock::class)
            register(BlockID.HONEYCOMB_BLOCK, BlockHoneycombBlock::class)
            register(BlockID.HOPPER, BlockHopper::class)
            register(BlockID.HORN_CORAL, BlockHornCoral::class)
            register(BlockID.HORN_CORAL_BLOCK, BlockHornCoralBlock::class)
            register(BlockID.HORN_CORAL_FAN, BlockHornCoralFan::class)
            register(BlockID.HORN_CORAL_WALL_FAN, BlockHornCoralWallFan::class)
            register(BlockID.ICE, BlockIce::class)
            register(BlockID.INFESTED_CHISELED_STONE_BRICKS, BlockInfestedChiseledStoneBricks::class)
            register(BlockID.INFESTED_COBBLESTONE, BlockInfestedCobblestone::class)
            register(BlockID.INFESTED_CRACKED_STONE_BRICKS, BlockInfestedCrackedStoneBricks::class)
            register(BlockID.INFESTED_DEEPSLATE, BlockInfestedDeepslate::class)
            register(BlockID.INFESTED_MOSSY_STONE_BRICKS, BlockInfestedMossyStoneBricks::class)
            register(BlockID.INFESTED_STONE, BlockInfestedStone::class)
            register(BlockID.INFESTED_STONE_BRICKS, BlockInfestedStoneBricks::class)
            register(BlockID.INFO_UPDATE, BlockInfoUpdate::class)
            register(BlockID.INFO_UPDATE2, BlockInfoUpdate2::class)
            register(BlockID.INVISIBLE_BEDROCK, BlockInvisibleBedrock::class)
            register(BlockID.IRON_BARS, BlockIronBars::class)
            register(BlockID.IRON_BLOCK, BlockIronBlock::class)
            register(BlockID.IRON_DOOR, BlockIronDoor::class)
            register(BlockID.IRON_ORE, BlockIronOre::class)
            register(BlockID.IRON_TRAPDOOR, BlockIronTrapdoor::class)
            register(BlockID.JIGSAW, BlockJigsaw::class)
            register(BlockID.JUKEBOX, BlockJukebox::class)
            register(BlockID.JUNGLE_BUTTON, BlockJungleButton::class)
            register(BlockID.JUNGLE_DOOR, BlockJungleDoor::class)
            register(BlockID.JUNGLE_DOUBLE_SLAB, BlockJungleDoubleSlab::class)
            register(BlockID.JUNGLE_FENCE, BlockJungleFence::class)
            register(BlockID.JUNGLE_FENCE_GATE, BlockJungleFenceGate::class)
            register(BlockID.JUNGLE_HANGING_SIGN, BlockJungleHangingSign::class)
            register(BlockID.JUNGLE_LEAVES, BlockJungleLeaves::class)
            register(BlockID.JUNGLE_LOG, BlockJungleLog::class)
            register(BlockID.JUNGLE_PLANKS, BlockJunglePlanks::class)
            register(BlockID.JUNGLE_PRESSURE_PLATE, BlockJunglePressurePlate::class)
            register(BlockID.JUNGLE_SAPLING, BlockJungleSapling::class)
            register(BlockID.JUNGLE_SLAB, BlockJungleSlab::class)
            register(BlockID.JUNGLE_STAIRS, BlockJungleStairs::class)
            register(BlockID.JUNGLE_STANDING_SIGN, BlockJungleStandingSign::class)
            register(BlockID.JUNGLE_TRAPDOOR, BlockJungleTrapdoor::class)
            register(BlockID.JUNGLE_WALL_SIGN, BlockJungleWallSign::class)
            register(BlockID.JUNGLE_WOOD, BlockJungleWood::class)
            register(BlockID.KELP, BlockKelp::class)
            register(BlockID.LADDER, BlockLadder::class)
            register(BlockID.LANTERN, BlockLantern::class)
            register(BlockID.LAPIS_BLOCK, BlockLapisBlock::class)
            register(BlockID.LAPIS_ORE, BlockLapisOre::class)
            register(BlockID.LARGE_AMETHYST_BUD, BlockLargeAmethystBud::class)
            register(BlockID.LARGE_FERN, BlockLargeFern::class)
            register(BlockID.LAVA, BlockLava::class)
            register(BlockID.LEAF_LITTER, BlockLeafLitter::class)
            register(BlockID.LECTERN, BlockLectern::class)
            register(BlockID.LEVER, BlockLever::class)
            register(BlockID.LIGHT_BLOCK_0, BlockLightBlock0::class)
            register(BlockID.LIGHT_BLOCK_1, BlockLightBlock1::class)
            register(BlockID.LIGHT_BLOCK_2, BlockLightBlock2::class)
            register(BlockID.LIGHT_BLOCK_3, BlockLightBlock3::class)
            register(BlockID.LIGHT_BLOCK_4, BlockLightBlock4::class)
            register(BlockID.LIGHT_BLOCK_5, BlockLightBlock5::class)
            register(BlockID.LIGHT_BLOCK_6, BlockLightBlock6::class)
            register(BlockID.LIGHT_BLOCK_7, BlockLightBlock7::class)
            register(BlockID.LIGHT_BLOCK_8, BlockLightBlock8::class)
            register(BlockID.LIGHT_BLOCK_9, BlockLightBlock9::class)
            register(BlockID.LIGHT_BLOCK_10, BlockLightBlock10::class)
            register(BlockID.LIGHT_BLOCK_11, BlockLightBlock11::class)
            register(BlockID.LIGHT_BLOCK_12, BlockLightBlock12::class)
            register(BlockID.LIGHT_BLOCK_13, BlockLightBlock13::class)
            register(BlockID.LIGHT_BLOCK_14, BlockLightBlock14::class)
            register(BlockID.LIGHT_BLOCK_15, BlockLightBlock15::class)
            register(BlockID.LIGHT_BLUE_CANDLE, BlockLightBlueCandle::class)
            register(BlockID.LIGHT_BLUE_CANDLE_CAKE, BlockLightBlueCandleCake::class)
            register(BlockID.LIGHT_BLUE_CARPET, BlockLightBlueCarpet::class)
            register(BlockID.LIGHT_BLUE_CONCRETE, BlockLightBlueConcrete::class)
            register(BlockID.LIGHT_BLUE_CONCRETE_POWDER, BlockLightBlueConcretePowder::class)
            register(BlockID.LIGHT_BLUE_GLAZED_TERRACOTTA, BlockLightBlueGlazedTerracotta::class)
            register(BlockID.LIGHT_BLUE_SHULKER_BOX, BlockLightBlueShulkerBox::class)
            register(BlockID.LIGHT_BLUE_STAINED_GLASS, BlockLightBlueStainedGlass::class)
            register(BlockID.LIGHT_BLUE_STAINED_GLASS_PANE, BlockLightBlueStainedGlassPane::class)
            register(BlockID.LIGHT_BLUE_TERRACOTTA, BlockLightBlueTerracotta::class)
            register(BlockID.LIGHT_BLUE_WOOL, BlockLightBlueWool::class)
            register(BlockID.LIGHT_GRAY_CANDLE, BlockLightGrayCandle::class)
            register(BlockID.LIGHT_GRAY_CANDLE_CAKE, BlockLightGrayCandleCake::class)
            register(BlockID.LIGHT_GRAY_CARPET, BlockLightGrayCarpet::class)
            register(BlockID.LIGHT_GRAY_CONCRETE, BlockLightGrayConcrete::class)
            register(BlockID.LIGHT_GRAY_CONCRETE_POWDER, BlockLightGrayConcretePowder::class)
            register(BlockID.LIGHT_GRAY_SHULKER_BOX, BlockLightGrayShulkerBox::class)
            register(BlockID.LIGHT_GRAY_STAINED_GLASS, BlockLightGrayStainedGlass::class)
            register(BlockID.LIGHT_GRAY_STAINED_GLASS_PANE, BlockLightGrayStainedGlassPane::class)
            register(BlockID.LIGHT_GRAY_TERRACOTTA, BlockLightGrayTerracotta::class)
            register(BlockID.LIGHT_GRAY_WOOL, BlockLightGrayWool::class)
            register(BlockID.LIGHT_WEIGHTED_PRESSURE_PLATE, BlockLightWeightedPressurePlate::class)
            register(BlockID.LIGHTNING_ROD, BlockLightningRod::class)
            register(BlockID.LILAC, BlockLilac::class)
            register(BlockID.LILY_OF_THE_VALLEY, BlockLilyOfTheValley::class)
            register(BlockID.LIME_CANDLE, BlockLimeCandle::class)
            register(BlockID.LIME_CANDLE_CAKE, BlockLimeCandleCake::class)
            register(BlockID.LIME_CARPET, BlockLimeCarpet::class)
            register(BlockID.LIME_CONCRETE, BlockLimeConcrete::class)
            register(BlockID.LIME_CONCRETE_POWDER, BlockLimeConcretePowder::class)
            register(BlockID.LIME_GLAZED_TERRACOTTA, BlockLimeGlazedTerracotta::class)
            register(BlockID.LIME_SHULKER_BOX, BlockLimeShulkerBox::class)
            register(BlockID.LIME_STAINED_GLASS, BlockLimeStainedGlass::class)
            register(BlockID.LIME_STAINED_GLASS_PANE, BlockLimeStainedGlassPane::class)
            register(BlockID.LIME_TERRACOTTA, BlockLimeTerracotta::class)
            register(BlockID.LIME_WOOL, BlockLimeWool::class)
            register(BlockID.LIT_BLAST_FURNACE, BlockLitBlastFurnace::class)
            register(BlockID.LIT_DEEPSLATE_REDSTONE_ORE, BlockLitDeepslateRedstoneOre::class)
            register(BlockID.LIT_FURNACE, BlockLitFurnace::class)
            register(BlockID.LIT_PUMPKIN, BlockLitPumpkin::class)
            register(BlockID.LIT_REDSTONE_LAMP, BlockLitRedstoneLamp::class)
            register(BlockID.LIT_REDSTONE_ORE, BlockLitRedstoneOre::class)
            register(BlockID.LIT_SMOKER, BlockLitSmoker::class)
            register(BlockID.LODESTONE, BlockLodestone::class)
            register(BlockID.LOOM, BlockLoom::class)
            register(BlockID.MAGENTA_CANDLE, BlockMagentaCandle::class)
            register(BlockID.MAGENTA_CANDLE_CAKE, BlockMagentaCandleCake::class)
            register(BlockID.MAGENTA_CARPET, BlockMagentaCarpet::class)
            register(BlockID.MAGENTA_CONCRETE, BlockMagentaConcrete::class)
            register(BlockID.MAGENTA_CONCRETE_POWDER, BlockMagentaConcretePowder::class)
            register(BlockID.MAGENTA_GLAZED_TERRACOTTA, BlockMagentaGlazedTerracotta::class)
            register(BlockID.MAGENTA_SHULKER_BOX, BlockMagentaShulkerBox::class)
            register(BlockID.MAGENTA_STAINED_GLASS, BlockMagentaStainedGlass::class)
            register(BlockID.MAGENTA_STAINED_GLASS_PANE, BlockMagentaStainedGlassPane::class)
            register(BlockID.MAGENTA_TERRACOTTA, BlockMagentaTerracotta::class)
            register(BlockID.MAGENTA_WOOL, BlockMagentaWool::class)
            register(BlockID.MAGMA, BlockMagma::class)
            register(BlockID.MANGROVE_BUTTON, BlockMangroveButton::class)
            register(BlockID.MANGROVE_DOOR, BlockMangroveDoor::class)
            register(BlockID.MANGROVE_DOUBLE_SLAB, BlockMangroveDoubleSlab::class)
            register(BlockID.MANGROVE_FENCE, BlockMangroveFence::class)
            register(BlockID.MANGROVE_FENCE_GATE, BlockMangroveFenceGate::class)
            register(BlockID.MANGROVE_HANGING_SIGN, BlockMangroveHangingSign::class)
            register(BlockID.MANGROVE_LEAVES, BlockMangroveLeaves::class)
            register(BlockID.MANGROVE_LOG, BlockMangroveLog::class)
            register(BlockID.MANGROVE_PLANKS, BlockMangrovePlanks::class)
            register(BlockID.MANGROVE_PRESSURE_PLATE, BlockMangrovePressurePlate::class)
            register(BlockID.MANGROVE_PROPAGULE, BlockMangrovePropagule::class)
            register(BlockID.MANGROVE_ROOTS, BlockMangroveRoots::class)
            register(BlockID.MANGROVE_SLAB, BlockMangroveSlab::class)
            register(BlockID.MANGROVE_STAIRS, BlockMangroveStairs::class)
            register(BlockID.MANGROVE_STANDING_SIGN, BlockMangroveStandingSign::class)
            register(BlockID.MANGROVE_TRAPDOOR, BlockMangroveTrapdoor::class)
            register(BlockID.MANGROVE_WALL_SIGN, BlockMangroveWallSign::class)
            register(BlockID.MANGROVE_WOOD, BlockMangroveWood::class)
            register(BlockID.MEDIUM_AMETHYST_BUD, BlockMediumAmethystBud::class)
            register(BlockID.MELON_BLOCK, BlockMelonBlock::class)
            register(BlockID.MELON_STEM, BlockMelonStem::class)
            register(BlockID.MOB_SPAWNER, BlockMobSpawner::class)
            //register(MONSTER_EGG, BlockMonsterEgg.class);
            register(BlockID.MOSS_BLOCK, BlockMossBlock::class)
            register(BlockID.MOSS_CARPET, BlockMossCarpet::class)
            register(BlockID.MOSSY_COBBLESTONE, BlockMossyCobblestone::class)
            register(BlockID.MOSSY_COBBLESTONE_DOUBLE_SLAB, BlockMossyCobblestoneDoubleSlab::class)
            register(BlockID.MOSSY_COBBLESTONE_SLAB, BlockMossyCobblestoneSlab::class)
            register(BlockID.MOSSY_COBBLESTONE_STAIRS, BlockMossyCobblestoneStairs::class)
            register(BlockID.MOSSY_COBBLESTONE_WALL, BlockMossyCobblestoneWall::class)
            register(BlockID.MOSSY_STONE_BRICK_DOUBLE_SLAB, BlockMossyStoneBrickDoubleSlab::class)
            register(BlockID.MOSSY_STONE_BRICK_SLAB, BlockMossyStoneBrickSlab::class)
            register(BlockID.MOSSY_STONE_BRICKS, BlockMossyStoneBricks::class)
            register(BlockID.MOSSY_STONE_BRICK_STAIRS, BlockMossyStoneBrickStairs::class)
            register(BlockID.MOSSY_STONE_BRICK_WALL, BlockMossyStoneBrickWall::class)
            register(BlockID.MOVING_BLOCK, BlockMovingBlock::class)
            register(BlockID.MUD, BlockMud::class)
            register(BlockID.MUD_BRICK_DOUBLE_SLAB, BlockMudBrickDoubleSlab::class)
            register(BlockID.MUD_BRICK_SLAB, BlockMudBrickSlab::class)
            register(BlockID.MUD_BRICK_STAIRS, BlockMudBrickStairs::class)
            register(BlockID.MUD_BRICK_WALL, BlockMudBrickWall::class)
            register(BlockID.MUD_BRICKS, BlockMudBricks::class)
            register(BlockID.MUDDY_MANGROVE_ROOTS, BlockMuddyMangroveRoots::class)
            register(BlockID.MUSHROOM_STEM, BlockMushroomStem::class)
            register(BlockID.MYCELIUM, BlockMycelium::class)
            register(BlockID.NETHER_BRICK, BlockNetherBrick::class)
            register(BlockID.NETHER_BRICK_DOUBLE_SLAB, BlockNetherBrickDoubleSlab::class)
            register(BlockID.NETHER_BRICK_FENCE, BlockNetherBrickFence::class)
            register(BlockID.NETHER_BRICK_SLAB, BlockNetherBrickSlab::class)
            register(BlockID.NETHER_BRICK_STAIRS, BlockNetherBrickStairs::class)
            register(BlockID.NETHER_BRICK_WALL, BlockNetherBrickWall::class)
            register(BlockID.NETHER_GOLD_ORE, BlockNetherGoldOre::class)
            register(BlockID.NETHER_SPROUTS, BlockNetherSprouts::class)
            register(BlockID.NETHER_WART, BlockNetherWart::class)
            register(BlockID.NETHER_WART_BLOCK, BlockNetherWartBlock::class)
            register(BlockID.NETHERITE_BLOCK, BlockNetheriteBlock::class)
            register(BlockID.NETHERRACK, BlockNetherrack::class)
            register(BlockID.NETHERREACTOR, BlockNetherreactor::class)
            register(BlockID.NORMAL_STONE_SLAB, BlockNormalStoneSlab::class)
            register(BlockID.NORMAL_STONE_DOUBLE_SLAB, BlockNormalStoneDoubleSlab::class)
            register(BlockID.NORMAL_STONE_STAIRS, BlockNormalStoneStairs::class)
            register(BlockID.NOTEBLOCK, BlockNoteblock::class)
            register(BlockID.OAK_DOUBLE_SLAB, BlockOakDoubleSlab::class)
            register(BlockID.OAK_FENCE, BlockOakFence::class)
            register(BlockID.OAK_HANGING_SIGN, BlockOakHangingSign::class)
            register(BlockID.OAK_LEAVES, BlockOakLeaves::class)
            register(BlockID.OAK_LOG, BlockOakLog::class)
            register(BlockID.OAK_PLANKS, BlockOakPlanks::class)
            register(BlockID.OAK_SAPLING, BlockOakSapling::class)
            register(BlockID.OAK_SLAB, BlockOakSlab::class)
            register(BlockID.OAK_STAIRS, BlockOakStairs::class)
            register(BlockID.OAK_WOOD, BlockOakWood::class)
            register(BlockID.OBSERVER, BlockObserver::class)
            register(BlockID.OBSIDIAN, BlockObsidian::class)
            register(BlockID.OCHRE_FROGLIGHT, BlockOchreFroglight::class)
            register(BlockID.OPEN_EYEBLOSSOM, BlockOpenEyeblossom::class)
            register(BlockID.ORANGE_CANDLE, BlockOrangeCandle::class)
            register(BlockID.ORANGE_CANDLE_CAKE, BlockOrangeCandleCake::class)
            register(BlockID.ORANGE_CARPET, BlockOrangeCarpet::class)
            register(BlockID.ORANGE_CONCRETE, BlockOrangeConcrete::class)
            register(BlockID.ORANGE_CONCRETE_POWDER, BlockOrangeConcretePowder::class)
            register(BlockID.ORANGE_GLAZED_TERRACOTTA, BlockOrangeGlazedTerracotta::class)
            register(BlockID.ORANGE_SHULKER_BOX, BlockOrangeShulkerBox::class)
            register(BlockID.ORANGE_STAINED_GLASS, BlockOrangeStainedGlass::class)
            register(BlockID.ORANGE_STAINED_GLASS_PANE, BlockOrangeStainedGlassPane::class)
            register(BlockID.ORANGE_TERRACOTTA, BlockOrangeTerracotta::class)
            register(BlockID.ORANGE_TULIP, BlockOrangeTulip::class)
            register(BlockID.ORANGE_WOOL, BlockOrangeWool::class)
            register(BlockID.OXEYE_DAISY, BlockOxeyeDaisy::class)
            register(BlockID.OXIDIZED_CHISELED_COPPER, BlockOxidizedChiseledCopper::class)
            register(BlockID.OXIDIZED_COPPER, BlockOxidizedCopper::class)
            register(BlockID.OXIDIZED_COPPER_BULB, BlockOxidizedCopperBulb::class)
            register(BlockID.OXIDIZED_COPPER_DOOR, BlockOxidizedCopperDoor::class)
            register(BlockID.OXIDIZED_COPPER_GRATE, BlockOxidizedCopperGrate::class)
            register(BlockID.OXIDIZED_COPPER_TRAPDOOR, BlockOxidizedCopperTrapdoor::class)
            register(BlockID.OXIDIZED_CUT_COPPER, BlockOxidizedCutCopper::class)
            register(BlockID.OXIDIZED_CUT_COPPER_SLAB, BlockOxidizedCutCopperSlab::class)
            register(BlockID.OXIDIZED_CUT_COPPER_STAIRS, BlockOxidizedCutCopperStairs::class)
            register(BlockID.OXIDIZED_DOUBLE_CUT_COPPER_SLAB, BlockOxidizedDoubleCutCopperSlab::class)
            register(BlockID.PACKED_ICE, BlockPackedIce::class)
            register(BlockID.PACKED_MUD, BlockPackedMud::class)
            register(BlockID.PALE_HANGING_MOSS, BlockPaleHangingMoss::class)
            register(BlockID.PALE_MOSS_BLOCK, BlockPaleMossBlock::class)
            register(BlockID.PALE_MOSS_CARPET, BlockPaleMossCarpet::class)
            register(BlockID.PALE_OAK_BUTTON, BlockPaleOakButton::class)
            register(BlockID.PALE_OAK_DOOR, BlockPaleOakDoor::class)
            register(BlockID.PALE_OAK_FENCE, BlockPaleOakFence::class)
            register(BlockID.PALE_OAK_FENCE_GATE, BlockPaleOakFenceGate::class)
            register(BlockID.PALE_OAK_HANGING_SIGN, BlockPaleOakHangingSign::class)
            register(BlockID.PALE_OAK_LEAVES, BlockPaleOakLeaves::class)
            register(BlockID.PALE_OAK_LOG, BlockPaleOakLog::class)
            register(BlockID.PALE_OAK_DOUBLE_SLAB, BlockPaleOakDoubleSlab::class)
            register(BlockID.PALE_OAK_PLANKS, BlockPaleOakPlanks::class)
            register(BlockID.PALE_OAK_PRESSURE_PLATE, BlockPaleOakPressurePlate::class)
            register(BlockID.PALE_OAK_SAPLING, BlockPaleOakSapling::class)
            register(BlockID.PALE_OAK_SLAB, BlockPaleOakSlab::class)
            register(BlockID.PALE_OAK_STAIRS, BlockPaleOakStairs::class)
            register(BlockID.PALE_OAK_STANDING_SIGN, BlockPaleOakStandingSign::class)
            register(BlockID.PALE_OAK_TRAPDOOR, BlockPaleOakTrapdoor::class)
            register(BlockID.PALE_OAK_WALL_SIGN, BlockPaleOakWallSign::class)
            register(BlockID.PALE_OAK_WOOD, BlockPaleOakWood::class)
            register(BlockID.PEARLESCENT_FROGLIGHT, BlockPearlescentFroglight::class)
            register(BlockID.PEONY, BlockPeony::class)
            register(BlockID.PETRIFIED_OAK_DOUBLE_SLAB, BlockPetrifiedOakDoubleSlab::class)
            register(BlockID.PETRIFIED_OAK_SLAB, BlockPetrifiedOakSlab::class)
            register(BlockID.PIGLIN_HEAD, BlockPiglinHead::class)
            register(BlockID.PINK_CANDLE, BlockPinkCandle::class)
            register(BlockID.PINK_CANDLE_CAKE, BlockPinkCandleCake::class)
            register(BlockID.PINK_CARPET, BlockPinkCarpet::class)
            register(BlockID.PINK_CONCRETE, BlockPinkConcrete::class)
            register(BlockID.PINK_CONCRETE_POWDER, BlockPinkConcretePowder::class)
            register(BlockID.PINK_GLAZED_TERRACOTTA, BlockPinkGlazedTerracotta::class)
            register(BlockID.PINK_PETALS, BlockPinkPetals::class)
            register(BlockID.PINK_SHULKER_BOX, BlockPinkShulkerBox::class)
            register(BlockID.PINK_STAINED_GLASS, BlockPinkStainedGlass::class)
            register(BlockID.PINK_STAINED_GLASS_PANE, BlockPinkStainedGlassPane::class)
            register(BlockID.PINK_TERRACOTTA, BlockPinkTerracotta::class)
            register(BlockID.PINK_TULIP, BlockPinkTulip::class)
            register(BlockID.PINK_WOOL, BlockPinkWool::class)
            register(BlockID.PISTON, BlockPiston::class)
            register(BlockID.PISTON_ARM_COLLISION, BlockPistonArmCollision::class)
            register(BlockID.PITCHER_CROP, BlockPitcherCrop::class)
            register(BlockID.PITCHER_PLANT, BlockPitcherPlant::class)
            register(BlockID.PLAYER_HEAD, BlockPlayerHead::class)
            register(BlockID.PODZOL, BlockPodzol::class)
            register(BlockID.POINTED_DRIPSTONE, BlockPointedDripstone::class)
            register(BlockID.POLISHED_ANDESITE, BlockPolishedAndesite::class)
            register(BlockID.POLISHED_ANDESITE_DOUBLE_SLAB, BlockPolishedAndesiteDoubleSlab::class)
            register(BlockID.POLISHED_ANDESITE_SLAB, BlockPolishedAndesiteSlab::class)
            register(BlockID.POLISHED_ANDESITE_STAIRS, BlockPolishedAndesiteStairs::class)
            register(BlockID.POLISHED_BASALT, BlockPolishedBasalt::class)
            register(BlockID.POLISHED_BLACKSTONE, BlockPolishedBlackstone::class)
            register(BlockID.POLISHED_BLACKSTONE_BRICK_DOUBLE_SLAB, BlockPolishedBlackstoneBrickDoubleSlab::class)
            register(BlockID.POLISHED_BLACKSTONE_BRICK_SLAB, BlockPolishedBlackstoneBrickSlab::class)
            register(BlockID.POLISHED_BLACKSTONE_BRICK_STAIRS, BlockPolishedBlackstoneBrickStairs::class)
            register(BlockID.POLISHED_BLACKSTONE_BRICK_WALL, BlockPolishedBlackstoneBrickWall::class)
            register(BlockID.POLISHED_BLACKSTONE_BRICKS, BlockPolishedBlackstoneBricks::class)
            register(BlockID.POLISHED_BLACKSTONE_BUTTON, BlockPolishedBlackstoneButton::class)
            register(BlockID.POLISHED_BLACKSTONE_DOUBLE_SLAB, BlockPolishedBlackstoneDoubleSlab::class)
            register(BlockID.POLISHED_BLACKSTONE_PRESSURE_PLATE, BlockPolishedBlackstonePressurePlate::class)
            register(BlockID.POLISHED_BLACKSTONE_SLAB, BlockPolishedBlackstoneSlab::class)
            register(BlockID.POLISHED_BLACKSTONE_STAIRS, BlockPolishedBlackstoneStairs::class)
            register(BlockID.POLISHED_BLACKSTONE_WALL, BlockPolishedBlackstoneWall::class)
            register(BlockID.POLISHED_DEEPSLATE, BlockPolishedDeepslate::class)
            register(BlockID.POLISHED_DEEPSLATE_DOUBLE_SLAB, BlockPolishedDeepslateDoubleSlab::class)
            register(BlockID.POLISHED_DEEPSLATE_SLAB, BlockPolishedDeepslateSlab::class)
            register(BlockID.POLISHED_DEEPSLATE_STAIRS, BlockPolishedDeepslateStairs::class)
            register(BlockID.POLISHED_DEEPSLATE_WALL, BlockPolishedDeepslateWall::class)
            register(BlockID.POLISHED_DIORITE, BlockPolishedDiorite::class)
            register(BlockID.POLISHED_DIORITE_DOUBLE_SLAB, BlockPolishedDioriteDoubleSlab::class)
            register(BlockID.POLISHED_DIORITE_SLAB, BlockPolishedDioriteSlab::class)
            register(BlockID.POLISHED_DIORITE_STAIRS, BlockPolishedDioriteStairs::class)
            register(BlockID.POLISHED_GRANITE, BlockPolishedGranite::class)
            register(BlockID.POLISHED_GRANITE_DOUBLE_SLAB, BlockPolishedGraniteDoubleSlab::class)
            register(BlockID.POLISHED_GRANITE_SLAB, BlockPolishedGraniteSlab::class)
            register(BlockID.POLISHED_GRANITE_STAIRS, BlockPolishedGraniteStairs::class)
            register(BlockID.POLISHED_TUFF, BlockPolishedTuff::class)
            register(BlockID.POLISHED_TUFF_DOUBLE_SLAB, BlockPolishedTuffDoubleSlab::class)
            register(BlockID.POLISHED_TUFF_SLAB, BlockPolishedTuffSlab::class)
            register(BlockID.POLISHED_TUFF_STAIRS, BlockPolishedTuffStairs::class)
            register(BlockID.POLISHED_TUFF_WALL, BlockPolishedTuffWall::class)
            register(BlockID.POPPY, BlockPoppy::class)
            register(BlockID.PORTAL, BlockPortal::class)
            register(BlockID.POTATOES, BlockPotatoes::class)
            register(BlockID.POWDER_SNOW, BlockPowderSnow::class)
            register(BlockID.POWERED_COMPARATOR, BlockPoweredComparator::class)
            register(BlockID.POWERED_REPEATER, BlockPoweredRepeater::class)
            register(BlockID.PRISMARINE, BlockPrismarine::class)
            register(BlockID.PRISMARINE_BRICK_DOUBLE_SLAB, BlockPrismarineBrickDoubleSlab::class)
            register(BlockID.PRISMARINE_BRICK_SLAB, BlockPrismarineBrickSlab::class)
            register(BlockID.PRISMARINE_BRICKS, BlockPrismarineBricks::class)
            register(BlockID.PRISMARINE_BRICKS_STAIRS, BlockPrismarineBricksStairs::class)
            register(BlockID.PRISMARINE_DOUBLE_SLAB, BlockPrismarineDoubleSlab::class)
            register(BlockID.PRISMARINE_SLAB, BlockPrismarineSlab::class)
            register(BlockID.PRISMARINE_STAIRS, BlockPrismarineStairs::class)
            register(BlockID.PRISMARINE_WALL, BlockPrismarineWall::class)
            register(BlockID.PUMPKIN, BlockPumpkin::class)
            register(BlockID.PUMPKIN_STEM, BlockPumpkinStem::class)
            register(BlockID.PURPLE_CANDLE, BlockPurpleCandle::class)
            register(BlockID.PURPLE_CANDLE_CAKE, BlockPurpleCandleCake::class)
            register(BlockID.PURPLE_CARPET, BlockPurpleCarpet::class)
            register(BlockID.PURPLE_CONCRETE, BlockPurpleConcrete::class)
            register(BlockID.PURPLE_CONCRETE_POWDER, BlockPurpleConcretePowder::class)
            register(BlockID.PURPLE_GLAZED_TERRACOTTA, BlockPurpleGlazedTerracotta::class)
            register(BlockID.PURPLE_SHULKER_BOX, BlockPurpleShulkerBox::class)
            register(BlockID.PURPLE_STAINED_GLASS, BlockPurpleStainedGlass::class)
            register(BlockID.PURPLE_STAINED_GLASS_PANE, BlockPurpleStainedGlassPane::class)
            register(BlockID.PURPLE_TERRACOTTA, BlockPurpleTerracotta::class)
            register(BlockID.PURPLE_WOOL, BlockPurpleWool::class)
            register(BlockID.PURPUR_BLOCK, BlockPurpurBlock::class)
            register(BlockID.PURPUR_DOUBLE_SLAB, BlockPurpurDoubleSlab::class)
            register(BlockID.PURPUR_PILLAR, BlockPurpurPillar::class)
            register(BlockID.PURPUR_SLAB, BlockPurpurSlab::class)
            register(BlockID.PURPUR_STAIRS, BlockPurpurStairs::class)
            register(BlockID.QUARTZ_BLOCK, BlockQuartzBlock::class)
            register(BlockID.QUARTZ_BRICKS, BlockQuartzBricks::class)
            register(BlockID.QUARTZ_DOUBLE_SLAB, BlockQuartzDoubleSlab::class)
            register(BlockID.QUARTZ_ORE, BlockQuartzOre::class)
            register(BlockID.QUARTZ_PILLAR, BlockQuartzPillar::class)
            register(BlockID.QUARTZ_SLAB, BlockQuartzSlab::class)
            register(BlockID.QUARTZ_STAIRS, BlockQuartzStairs::class)
            register(BlockID.RAIL, BlockRail::class)
            register(BlockID.RAW_COPPER_BLOCK, BlockRawCopperBlock::class)
            register(BlockID.RAW_GOLD_BLOCK, BlockRawGoldBlock::class)
            register(BlockID.RAW_IRON_BLOCK, BlockRawIronBlock::class)
            register(BlockID.RED_CANDLE, BlockRedCandle::class)
            register(BlockID.RED_CANDLE_CAKE, BlockRedCandleCake::class)
            register(BlockID.RED_CARPET, BlockRedCarpet::class)
            register(BlockID.RED_CONCRETE, BlockRedConcrete::class)
            register(BlockID.RED_CONCRETE_POWDER, BlockRedConcretePowder::class)
            register(BlockID.RED_GLAZED_TERRACOTTA, BlockRedGlazedTerracotta::class)
            register(BlockID.RED_MUSHROOM, BlockRedMushroom::class)
            register(BlockID.RED_MUSHROOM_BLOCK, BlockRedMushroomBlock::class)
            register(BlockID.RED_NETHER_BRICK, BlockRedNetherBrick::class)
            register(BlockID.RED_NETHER_BRICK_DOUBLE_SLAB, BlockRedNetherBrickDoubleSlab::class)
            register(BlockID.RED_NETHER_BRICK_SLAB, BlockRedNetherBrickSlab::class)
            register(BlockID.RED_NETHER_BRICK_STAIRS, BlockRedNetherBrickStairs::class)
            register(BlockID.RED_NETHER_BRICK_WALL, BlockRedNetherBrickWall::class)
            register(BlockID.RED_SAND, BlockRedSand::class)
            register(BlockID.RED_SANDSTONE, BlockRedSandstone::class)
            register(BlockID.RED_SANDSTONE_DOUBLE_SLAB, BlockRedSandstoneDoubleSlab::class)
            register(BlockID.RED_SANDSTONE_SLAB, BlockRedSandstoneSlab::class)
            register(BlockID.RED_SANDSTONE_STAIRS, BlockRedSandstoneStairs::class)
            register(BlockID.RED_SANDSTONE_WALL, BlockRedSandstoneWall::class)
            register(BlockID.RED_SHULKER_BOX, BlockRedShulkerBox::class)
            register(BlockID.RED_STAINED_GLASS, BlockRedStainedGlass::class)
            register(BlockID.RED_STAINED_GLASS_PANE, BlockRedStainedGlassPane::class)
            register(BlockID.RED_TERRACOTTA, BlockRedTerracotta::class)
            register(BlockID.RED_TULIP, BlockRedTulip::class)
            register(BlockID.RED_WOOL, BlockRedWool::class)
            register(BlockID.REDSTONE_BLOCK, BlockRedstoneBlock::class)
            register(BlockID.REDSTONE_LAMP, BlockRedstoneLamp::class)
            register(BlockID.REDSTONE_ORE, BlockRedstoneOre::class)
            register(BlockID.REDSTONE_TORCH, BlockRedstoneTorch::class)
            register(BlockID.REDSTONE_WIRE, BlockRedstoneWire::class)
            register(BlockID.REEDS, BlockReeds::class)
            register(BlockID.REINFORCED_DEEPSLATE, BlockReinforcedDeepslate::class)
            register(BlockID.REPEATING_COMMAND_BLOCK, BlockRepeatingCommandBlock::class)
            register(BlockID.RESERVED6, BlockReserved6::class)
            register(BlockID.RESIN_BLOCK, BlockResin::class)
            register(BlockID.RESIN_BRICKS, BlockResinBricks::class)
            register(BlockID.RESIN_BRICK_DOUBLE_SLAB, BlockResinBrickDoubleSlab::class)
            register(BlockID.RESIN_BRICK_SLAB, BlockResinBrickSlab::class)
            register(BlockID.RESIN_BRICK_STAIRS, BlockResinBrickStairs::class)
            register(BlockID.RESIN_BRICK_WALL, BlockResinBrickWall::class)
            register(BlockID.RESIN_CLUMP, BlockResinClump::class)
            register(BlockID.RESPAWN_ANCHOR, BlockRespawnAnchor::class)
            register(BlockID.ROSE_BUSH, BlockRoseBush::class)
            register(BlockID.SAND, BlockSand::class)
            register(BlockID.SANDSTONE, BlockSandstone::class)
            register(BlockID.SANDSTONE_DOUBLE_SLAB, BlockSandstoneDoubleSlab::class)
            register(BlockID.SANDSTONE_SLAB, BlockSandstoneSlab::class)
            register(BlockID.SANDSTONE_STAIRS, BlockSandstoneStairs::class)
            register(BlockID.SANDSTONE_WALL, BlockSandstoneWall::class)
            register(BlockID.SCAFFOLDING, BlockScaffolding::class)
            register(BlockID.SCULK, BlockSculk::class)
            register(BlockID.SCULK_CATALYST, BlockSculkCatalyst::class)
            register(BlockID.SCULK_SENSOR, BlockSculkSensor::class)
            register(BlockID.SCULK_SHRIEKER, BlockSculkShrieker::class)
            register(BlockID.SCULK_VEIN, BlockSculkVein::class)
            register(BlockID.SEA_LANTERN, BlockSeaLantern::class)
            register(BlockID.SEA_PICKLE, BlockSeaPickle::class)
            register(BlockID.SEAGRASS, BlockSeagrass::class)
            register(BlockID.SHORT_DRY_GRASS, BlockShortDryGrass::class)
            register(BlockID.SHORT_GRASS, BlockShortGrass::class)
            register(BlockID.SHROOMLIGHT, BlockShroomlight::class)
            register(BlockID.SILVER_GLAZED_TERRACOTTA, BlockSilverGlazedTerracotta::class)
            register(BlockID.SKELETON_SKULL, BlockSkeletonSkull::class)
            register(BlockID.SLIME, BlockSlime::class)
            register(BlockID.SMALL_AMETHYST_BUD, BlockSmallAmethystBud::class)
            register(BlockID.SMALL_DRIPLEAF_BLOCK, BlockSmallDripleafBlock::class)
            register(BlockID.SMITHING_TABLE, BlockSmithingTable::class)
            register(BlockID.SMOKER, BlockSmoker::class)
            register(BlockID.SMOOTH_BASALT, BlockSmoothBasalt::class)
            register(BlockID.SMOOTH_QUARTZ, BlockSmoothQuartz::class)
            register(BlockID.SMOOTH_QUARTZ_DOUBLE_SLAB, BlockSmoothQuartzDoubleSlab::class)
            register(BlockID.SMOOTH_QUARTZ_SLAB, BlockSmoothQuartzSlab::class)
            register(BlockID.SMOOTH_QUARTZ_STAIRS, BlockSmoothQuartzStairs::class)
            register(BlockID.SMOOTH_RED_SANDSTONE, BlockSmoothRedSandstone::class)
            register(BlockID.SMOOTH_RED_SANDSTONE_DOUBLE_SLAB, BlockSmoothRedSandstoneDoubleSlab::class)
            register(BlockID.SMOOTH_RED_SANDSTONE_SLAB, BlockSmoothRedSandstoneSlab::class)
            register(BlockID.SMOOTH_RED_SANDSTONE_STAIRS, BlockSmoothRedSandstoneStairs::class)
            register(BlockID.SMOOTH_SANDSTONE, BlockSmoothSandstone::class)
            register(BlockID.SMOOTH_SANDSTONE_DOUBLE_SLAB, BlockSmoothSandstoneDoubleSlab::class)
            register(BlockID.SMOOTH_SANDSTONE_SLAB, BlockSmoothSandstoneSlab::class)
            register(BlockID.SMOOTH_SANDSTONE_STAIRS, BlockSmoothSandstoneStairs::class)
            register(BlockID.SMOOTH_STONE, BlockSmoothStone::class)
            register(BlockID.SMOOTH_STONE_DOUBLE_SLAB, BlockSmoothStoneDoubleSlab::class)
            register(BlockID.SMOOTH_STONE_SLAB, BlockSmoothStoneSlab::class)
            register(BlockID.SNIFFER_EGG, BlockSnifferEgg::class)
            register(BlockID.SNOW, BlockSnow::class)
            register(BlockID.SNOW_LAYER, BlockSnowLayer::class)
            register(BlockID.SOUL_CAMPFIRE, BlockSoulCampfire::class)
            register(BlockID.SOUL_FIRE, BlockSoulFire::class)
            register(BlockID.SOUL_LANTERN, BlockSoulLantern::class)
            register(BlockID.SOUL_SAND, BlockSoulSand::class)
            register(BlockID.SOUL_SOIL, BlockSoulSoil::class)
            register(BlockID.SOUL_TORCH, BlockSoulTorch::class)
            register(BlockID.SPONGE, BlockSponge::class)
            register(BlockID.SPORE_BLOSSOM, BlockSporeBlossom::class)
            register(BlockID.SPRUCE_BUTTON, BlockSpruceButton::class)
            register(BlockID.SPRUCE_DOOR, BlockSpruceDoor::class)
            register(BlockID.SPRUCE_DOUBLE_SLAB, BlockSpruceDoubleSlab::class)
            register(BlockID.SPRUCE_FENCE, BlockSpruceFence::class)
            register(BlockID.SPRUCE_FENCE_GATE, BlockSpruceFenceGate::class)
            register(BlockID.SPRUCE_HANGING_SIGN, BlockSpruceHangingSign::class)
            register(BlockID.SPRUCE_LEAVES, BlockSpruceLeaves::class)
            register(BlockID.SPRUCE_LOG, BlockSpruceLog::class)
            register(BlockID.SPRUCE_PLANKS, BlockSprucePlanks::class)
            register(BlockID.SPRUCE_PRESSURE_PLATE, BlockSprucePressurePlate::class)
            register(BlockID.SPRUCE_SAPLING, BlockSpruceSapling::class)
            register(BlockID.SPRUCE_SLAB, BlockSpruceSlab::class)
            register(BlockID.SPRUCE_STAIRS, BlockSpruceStairs::class)
            register(BlockID.SPRUCE_STANDING_SIGN, BlockSpruceStandingSign::class)
            register(BlockID.SPRUCE_TRAPDOOR, BlockSpruceTrapdoor::class)
            register(BlockID.SPRUCE_WALL_SIGN, BlockSpruceWallSign::class)
            register(BlockID.SPRUCE_WOOD, BlockSpruceWood::class)
            register(BlockID.STANDING_BANNER, BlockStandingBanner::class)
            register(BlockID.STANDING_SIGN, BlockStandingSign::class)
            register(BlockID.STICKY_PISTON, BlockStickyPiston::class)
            register(BlockID.STICKY_PISTON_ARM_COLLISION, BlockStickyPistonArmCollision::class)
            register(BlockID.STONE, BlockStone::class)
            register(BlockID.STONE_BRICK_DOUBLE_SLAB, BlockStoneBrickDoubleSlab::class)
            register(BlockID.STONE_BRICK_SLAB, BlockStoneBrickSlab::class)
            register(BlockID.STONE_BRICK_STAIRS, BlockStoneBrickStairs::class)
            register(BlockID.STONE_BRICK_WALL, BlockStoneBrickWall::class)
            register(BlockID.STONE_BUTTON, BlockStoneButton::class)
            register(BlockID.STONE_PRESSURE_PLATE, BlockStonePressurePlate::class)
            register(BlockID.STONE_STAIRS, BlockStoneStairs::class)
            register(BlockID.STONE_BRICKS, BlockStoneBricks::class)
            register(BlockID.STONECUTTER, BlockStonecutter::class)
            register(BlockID.STONECUTTER_BLOCK, BlockStonecutterBlock::class)
            register(BlockID.STRIPPED_ACACIA_LOG, BlockStrippedAcaciaLog::class)
            register(BlockID.STRIPPED_ACACIA_WOOD, BlockStrippedAcaciaWood::class)
            register(BlockID.STRIPPED_BAMBOO_BLOCK, BlockStrippedBambooBlock::class)
            register(BlockID.STRIPPED_BIRCH_LOG, BlockStrippedBirchLog::class)
            register(BlockID.STRIPPED_BIRCH_WOOD, BlockStrippedBirchWood::class)
            register(BlockID.STRIPPED_CHERRY_LOG, BlockStrippedCherryLog::class)
            register(BlockID.STRIPPED_CHERRY_WOOD, BlockStrippedCherryWood::class)
            register(BlockID.STRIPPED_CRIMSON_HYPHAE, BlockStrippedCrimsonHyphae::class)
            register(BlockID.STRIPPED_CRIMSON_STEM, BlockStrippedCrimsonStem::class)
            register(BlockID.STRIPPED_DARK_OAK_LOG, BlockStrippedDarkOakLog::class)
            register(BlockID.STRIPPED_DARK_OAK_WOOD, BlockStrippedDarkOakWood::class)
            register(BlockID.STRIPPED_JUNGLE_LOG, BlockStrippedJungleLog::class)
            register(BlockID.STRIPPED_JUNGLE_WOOD, BlockStrippedJungleWood::class)
            register(BlockID.STRIPPED_MANGROVE_LOG, BlockStrippedMangroveLog::class)
            register(BlockID.STRIPPED_MANGROVE_WOOD, BlockStrippedMangroveWood::class)
            register(BlockID.STRIPPED_OAK_LOG, BlockStrippedOakLog::class)
            register(BlockID.STRIPPED_OAK_WOOD, BlockStrippedOakWood::class)
            register(BlockID.STRIPPED_PALE_OAK_LOG, BlockStrippedPaleOakLog::class)
            register(BlockID.STRIPPED_PALE_OAK_WOOD, BlockStrippedPaleOakWood::class)
            register(BlockID.STRIPPED_SPRUCE_LOG, BlockStrippedSpruceLog::class)
            register(BlockID.STRIPPED_SPRUCE_WOOD, BlockStrippedSpruceWood::class)
            register(BlockID.STRIPPED_WARPED_HYPHAE, BlockStrippedWarpedHyphae::class)
            register(BlockID.STRIPPED_WARPED_STEM, BlockStrippedWarpedStem::class)
            register(BlockID.STRUCTURE_BLOCK, BlockStructureBlock::class)
            register(BlockID.STRUCTURE_VOID, BlockStructureVoid::class)
            register(BlockID.SUNFLOWER, BlockSunflower::class)
            register(BlockID.SUSPICIOUS_GRAVEL, BlockSuspiciousGravel::class)
            register(BlockID.SUSPICIOUS_SAND, BlockSuspiciousSand::class)
            register(BlockID.SWEET_BERRY_BUSH, BlockSweetBerryBush::class)
            register(BlockID.TALL_GRASS, BlockTallGrass::class)
            register(BlockID.TALL_DRY_GRASS, BlockTallDryGrass::class)
            register(BlockID.TARGET, BlockTarget::class)
            register(BlockID.TINTED_GLASS, BlockTintedGlass::class)
            register(BlockID.TNT, BlockTNT::class)
            register(BlockID.TORCH, BlockTorch::class)
            register(BlockID.TORCHFLOWER, BlockTorchflower::class)
            register(BlockID.TORCHFLOWER_CROP, BlockTorchflowerCrop::class)
            register(BlockID.TRAPDOOR, BlockTrapdoor::class)
            register(BlockID.TRAPPED_CHEST, BlockTrappedChest::class)
            register(BlockID.TRIAL_SPAWNER, BlockTrialSpawner::class)
            register(BlockID.TRIP_WIRE, BlockTripWire::class)
            register(BlockID.TRIPWIRE_HOOK, BlockTripwireHook::class)
            register(BlockID.TUBE_CORAL, BlockTubeCoral::class)
            register(BlockID.TUBE_CORAL_BLOCK, BlockTubeCoralBlock::class)
            register(BlockID.TUBE_CORAL_FAN, BlockTubeCoralFan::class)
            register(BlockID.TUBE_CORAL_WALL_FAN, BlockTubeCoralWallFan::class)
            register(BlockID.TUFF, BlockTuff::class)
            register(BlockID.TUFF_BRICK_DOUBLE_SLAB, BlockTuffBrickDoubleSlab::class)
            register(BlockID.TUFF_BRICK_SLAB, BlockTuffBrickSlab::class)
            register(BlockID.TUFF_BRICK_STAIRS, BlockTuffBrickStairs::class)
            register(BlockID.TUFF_BRICK_WALL, BlockTuffBrickWall::class)
            register(BlockID.TUFF_BRICKS, BlockTuffBricks::class)
            register(BlockID.TUFF_DOUBLE_SLAB, BlockTuffDoubleSlab::class)
            register(BlockID.TUFF_SLAB, BlockTuffSlab::class)
            register(BlockID.TUFF_STAIRS, BlockTuffStairs::class)
            register(BlockID.TUFF_WALL, BlockTuffWall::class)
            register(BlockID.TURTLE_EGG, BlockTurtleEgg::class)
            register(BlockID.TWISTING_VINES, BlockTwistingVines::class)
            register(BlockID.UNDYED_SHULKER_BOX, BlockUndyedShulkerBox::class)
            register(BlockID.UNKNOWN, BlockUnknown::class)
            register(BlockID.UNLIT_REDSTONE_TORCH, BlockUnlitRedstoneTorch::class)
            register(BlockID.UNPOWERED_COMPARATOR, BlockUnpoweredComparator::class)
            register(BlockID.UNPOWERED_REPEATER, BlockUnpoweredRepeater::class)
            register(BlockID.VAULT, BlockVault::class)
            register(BlockID.VERDANT_FROGLIGHT, BlockVerdantFroglight::class)
            register(BlockID.VINE, BlockVine::class)
            register(BlockID.WALL_BANNER, BlockWallBanner::class)
            register(BlockID.WALL_SIGN, BlockWallSign::class)
            register(BlockID.WARPED_BUTTON, BlockWarpedButton::class)
            register(BlockID.WARPED_DOOR, BlockWarpedDoor::class)
            register(BlockID.WARPED_DOUBLE_SLAB, BlockWarpedDoubleSlab::class)
            register(BlockID.WARPED_FENCE, BlockWarpedFence::class)
            register(BlockID.WARPED_FENCE_GATE, BlockWarpedFenceGate::class)
            register(BlockID.WARPED_FUNGUS, BlockWarpedFungus::class)
            register(BlockID.WARPED_HANGING_SIGN, BlockWarpedHangingSign::class)
            register(BlockID.WARPED_HYPHAE, BlockWarpedHyphae::class)
            register(BlockID.WARPED_NYLIUM, BlockWarpedNylium::class)
            register(BlockID.WARPED_PLANKS, BlockWarpedPlanks::class)
            register(BlockID.WARPED_PRESSURE_PLATE, BlockWarpedPressurePlate::class)
            register(BlockID.WARPED_ROOTS, BlockWarpedRoots::class)
            register(BlockID.WARPED_SLAB, BlockWarpedSlab::class)
            register(BlockID.WARPED_STAIRS, BlockWarpedStairs::class)
            register(BlockID.WARPED_STANDING_SIGN, BlockWarpedStandingSign::class)
            register(BlockID.WARPED_STEM, BlockWarpedStem::class)
            register(BlockID.WARPED_TRAPDOOR, BlockWarpedTrapdoor::class)
            register(BlockID.WARPED_WALL_SIGN, BlockWarpedWallSign::class)
            register(BlockID.WARPED_WART_BLOCK, BlockWarpedWartBlock::class)
            register(BlockID.WATER, BlockWater::class)
            register(BlockID.WATERLILY, BlockWaterlily::class)
            register(BlockID.WAXED_CHISELED_COPPER, BlockWaxedChiseledCopper::class)
            register(BlockID.WAXED_COPPER, BlockWaxedCopper::class)
            register(BlockID.WAXED_COPPER_BULB, BlockWaxedCopperBulb::class)
            register(BlockID.WAXED_COPPER_DOOR, BlockWaxedCopperDoor::class)
            register(BlockID.WAXED_COPPER_GRATE, BlockWaxedCopperGrate::class)
            register(BlockID.WAXED_COPPER_TRAPDOOR, BlockWaxedCopperTrapdoor::class)
            register(BlockID.WAXED_CUT_COPPER, BlockWaxedCutCopper::class)
            register(BlockID.WAXED_CUT_COPPER_SLAB, BlockWaxedCutCopperSlab::class)
            register(BlockID.WAXED_CUT_COPPER_STAIRS, BlockWaxedCutCopperStairs::class)
            register(BlockID.WAXED_DOUBLE_CUT_COPPER_SLAB, BlockWaxedDoubleCutCopperSlab::class)
            register(BlockID.WAXED_EXPOSED_CHISELED_COPPER, BlockWaxedExposedChiseledCopper::class)
            register(BlockID.WAXED_EXPOSED_COPPER, BlockWaxedExposedCopper::class)
            register(BlockID.WAXED_EXPOSED_COPPER_BULB, BlockWaxedExposedCopperBulb::class)
            register(BlockID.WAXED_EXPOSED_COPPER_DOOR, BlockWaxedExposedCopperDoor::class)
            register(BlockID.WAXED_EXPOSED_COPPER_GRATE, BlockWaxedExposedCopperGrate::class)
            register(BlockID.WAXED_EXPOSED_COPPER_TRAPDOOR, BlockWaxedExposedCopperTrapdoor::class)
            register(BlockID.WAXED_EXPOSED_CUT_COPPER, BlockWaxedExposedCutCopper::class)
            register(BlockID.WAXED_EXPOSED_CUT_COPPER_SLAB, BlockWaxedExposedCutCopperSlab::class)
            register(BlockID.WAXED_EXPOSED_CUT_COPPER_STAIRS, BlockWaxedExposedCutCopperStairs::class)
            register(BlockID.WAXED_EXPOSED_DOUBLE_CUT_COPPER_SLAB, BlockWaxedExposedDoubleCutCopperSlab::class)
            register(BlockID.WAXED_OXIDIZED_CHISELED_COPPER, BlockWaxedOxidizedChiseledCopper::class)
            register(BlockID.WAXED_OXIDIZED_COPPER, BlockWaxedOxidizedCopper::class)
            register(BlockID.WAXED_OXIDIZED_COPPER_BULB, BlockWaxedOxidizedCopperBulb::class)
            register(BlockID.WAXED_OXIDIZED_COPPER_DOOR, BlockWaxedOxidizedCopperDoor::class)
            register(BlockID.WAXED_OXIDIZED_COPPER_GRATE, BlockWaxedOxidizedCopperGrate::class)
            register(BlockID.WAXED_OXIDIZED_COPPER_TRAPDOOR, BlockWaxedOxidizedCopperTrapdoor::class)
            register(BlockID.WAXED_OXIDIZED_CUT_COPPER, BlockWaxedOxidizedCutCopper::class)
            register(BlockID.WAXED_OXIDIZED_CUT_COPPER_SLAB, BlockWaxedOxidizedCutCopperSlab::class)
            register(BlockID.WAXED_OXIDIZED_CUT_COPPER_STAIRS, BlockWaxedOxidizedCutCopperStairs::class)
            register(BlockID.WAXED_OXIDIZED_DOUBLE_CUT_COPPER_SLAB, BlockWaxedOxidizedDoubleCutCopperSlab::class)
            register(BlockID.WAXED_WEATHERED_CHISELED_COPPER, BlockWaxedWeatheredChiseledCopper::class)
            register(BlockID.WAXED_WEATHERED_COPPER, BlockWaxedWeatheredCopper::class)
            register(BlockID.WAXED_WEATHERED_COPPER_BULB, BlockWaxedWeatheredCopperBulb::class)
            register(BlockID.WAXED_WEATHERED_COPPER_DOOR, BlockWaxedWeatheredCopperDoor::class)
            register(BlockID.WAXED_WEATHERED_COPPER_GRATE, BlockWaxedWeatheredCopperGrate::class)
            register(BlockID.WAXED_WEATHERED_COPPER_TRAPDOOR, BlockWaxedWeatheredCopperTrapdoor::class)
            register(BlockID.WAXED_WEATHERED_CUT_COPPER, BlockWaxedWeatheredCutCopper::class)
            register(BlockID.WAXED_WEATHERED_CUT_COPPER_SLAB, BlockWaxedWeatheredCutCopperSlab::class)
            register(BlockID.WAXED_WEATHERED_CUT_COPPER_STAIRS, BlockWaxedWeatheredCutCopperStairs::class)
            register(BlockID.WAXED_WEATHERED_DOUBLE_CUT_COPPER_SLAB, BlockWaxedWeatheredDoubleCutCopperSlab::class)
            register(BlockID.WEATHERED_CHISELED_COPPER, BlockWeatheredChiseledCopper::class)
            register(BlockID.WEATHERED_COPPER, BlockWeatheredCopper::class)
            register(BlockID.WEATHERED_COPPER_BULB, BlockWeatheredCopperBulb::class)
            register(BlockID.WEATHERED_COPPER_DOOR, BlockWeatheredCopperDoor::class)
            register(BlockID.WEATHERED_COPPER_GRATE, BlockWeatheredCopperGrate::class)
            register(BlockID.WEATHERED_COPPER_TRAPDOOR, BlockWeatheredCopperTrapdoor::class)
            register(BlockID.WEATHERED_CUT_COPPER, BlockWeatheredCutCopper::class)
            register(BlockID.WEATHERED_CUT_COPPER_SLAB, BlockWeatheredCutCopperSlab::class)
            register(BlockID.WEATHERED_CUT_COPPER_STAIRS, BlockWeatheredCutCopperStairs::class)
            register(BlockID.WEATHERED_DOUBLE_CUT_COPPER_SLAB, BlockWeatheredDoubleCutCopperSlab::class)
            register(BlockID.WEB, BlockWeb::class)
            register(BlockID.WEEPING_VINES, BlockWeepingVines::class)
            register(BlockID.WET_SPONGE, BlockWetSponge::class)
            register(BlockID.WHEAT, BlockWheat::class)
            register(BlockID.WHITE_CANDLE, BlockWhiteCandle::class)
            register(BlockID.WHITE_CANDLE_CAKE, BlockWhiteCandleCake::class)
            register(BlockID.WHITE_CARPET, BlockWhiteCarpet::class)
            register(BlockID.WHITE_CONCRETE, BlockWhiteConcrete::class)
            register(BlockID.WHITE_CONCRETE_POWDER, BlockWhiteConcretePowder::class)
            register(BlockID.WHITE_GLAZED_TERRACOTTA, BlockWhiteGlazedTerracotta::class)
            register(BlockID.WHITE_SHULKER_BOX, BlockWhiteShulkerBox::class)
            register(BlockID.WHITE_STAINED_GLASS, BlockWhiteStainedGlass::class)
            register(BlockID.WHITE_STAINED_GLASS_PANE, BlockWhiteStainedGlassPane::class)
            register(BlockID.WHITE_TERRACOTTA, BlockWhiteTerracotta::class)
            register(BlockID.WHITE_TULIP, BlockWhiteTulip::class)
            register(BlockID.WHITE_WOOL, BlockWhiteWool::class)
            register(BlockID.WILDFLOWERS, BlockWildflowers::class)
            register(BlockID.WITHER_ROSE, BlockWitherRose::class)
            register(BlockID.WITHER_SKELETON_SKULL, BlockWitherSkeletonSkull::class)
            register(BlockID.WOODEN_BUTTON, BlockWoodenButton::class)
            register(BlockID.WOODEN_DOOR, BlockWoodenDoor::class)
            register(BlockID.WOODEN_PRESSURE_PLATE, BlockWoodenPressurePlate::class)
            register(BlockID.YELLOW_CANDLE, BlockYellowCandle::class)
            register(BlockID.YELLOW_CANDLE_CAKE, BlockYellowCandleCake::class)
            register(BlockID.YELLOW_CARPET, BlockYellowCarpet::class)
            register(BlockID.YELLOW_CONCRETE, BlockYellowConcrete::class)
            register(BlockID.YELLOW_CONCRETE_POWDER, BlockYellowConcretePowder::class)
            register(BlockID.DANDELION, BlockDandelion::class)
            register(BlockID.YELLOW_GLAZED_TERRACOTTA, BlockYellowGlazedTerracotta::class)
            register(BlockID.YELLOW_SHULKER_BOX, BlockYellowShulkerBox::class)
            register(BlockID.YELLOW_STAINED_GLASS, BlockYellowStainedGlass::class)
            register(BlockID.YELLOW_STAINED_GLASS_PANE, BlockYellowStainedGlassPane::class)
            register(BlockID.YELLOW_TERRACOTTA, BlockYellowTerracotta::class)
            register(BlockID.YELLOW_WOOL, BlockYellowWool::class)
            register(BlockID.ZOMBIE_HEAD, BlockZombieHead::class)
        } catch (_: RegisterException) {
        }
    }

    val keySet: @UnmodifiableView MutableSet<String>
        get() = Collections.unmodifiableSet(KEYSET)

    @Throws(RegisterException::class)
    override fun register(key: String, value: KClass<out Block>) {
        if (skipBlockSet.contains(key)) return  // skip for experimental or educational blocks

        if (value.isAbstract) {
            throw RegisterException("You can't register a abstract block class!")
        }
        try {
            val companion = value.companionObject ?: throw RuntimeException("Block must have a companion object!")
            val companionInstance =
                companion.objectInstance ?: throw RuntimeException("Block must have a companion object!")

            val properties = companion.memberProperties.find { it.name == "properties" }
                ?: throw RuntimeException("Block companion must have properties member")
            properties.isAccessible = true

            if (properties.returnType == typeOf<BlockProperties>()) {
                val blockProperties = properties.call(companionInstance) as BlockProperties

                val constructor = value.constructors.find { it ->
                    val param = it.parameters.firstOrNull() ?: return@find false

                    if (it.parameters.size > 1) {
                        if (it.parameters.subList(1, it.parameters.size).any { !it.isOptional }) return@find false
                    }

                    param.type == typeOf<BlockState>()
                } ?: throw RuntimeException("Block: $value must have a constructor with a BlockState param!")

                val fn = fun(state: BlockState?): Block {
                    return constructor.call(state ?: blockProperties.defaultState)
                }

                if (CACHE_CONSTRUCTORS.putIfAbsent(blockProperties.identifier, fn) != null) {
                    throw RegisterException("This block has already been registered with the identifier: " + blockProperties.identifier)
                } else {
                    KEYSET.add(key)
                    PROPERTIES[key] = blockProperties
                    CLASSES[key] = value

                    blockProperties.specialValueMap.values.forEach { v: BlockState ->
                        Registries.BLOCKSTATE.registerInternal(v)
                    }
                }
            } else {
                throw RegisterException(
                    "Block: $key must define a field `val properties: BlockProperties`!"
                )
            }
        } catch (e: NoSuchFieldException) {
            throw RegisterException(e)
        } catch (e: IllegalAccessException) {
            throw RegisterException(e)
        } catch (e: NoSuchMethodException) {
            throw RegisterException(e)
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

//    /**
//     * register custom item
//     */
//    @SafeVarargs
//    @Throws(RegisterException::class)
//    fun registerCustomBlock(plugin: Plugin, vararg values: Class<out Block?>) {
//        for (c in values) {
//            registerCustomBlock(plugin, c)
//        }
//    }
//
//    /**
//     * register custom block
//     */
//    @Throws(RegisterException::class)
//    fun registerCustomBlock(plugin: Plugin, value: Class<out Block?>) {
//        if (Modifier.isAbstract(value.modifiers)) {
//            throw RegisterException("You can't register a abstract block class!")
//        }
//        try {
//            val properties = value.getDeclaredField("PROPERTIES")
//            properties.isAccessible = true
//            val modifiers = properties.modifiers
//            val blockProperties: BlockProperties
//            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && properties.type == BlockProperties::class) {
//                blockProperties = properties[value] as BlockProperties
//            } else {
//                throw RegisterException(
//                    "Block: ${value.simpleName} must define a field `public static final BlockProperties PROPERTIES`!"
//                )
//            }
//            try {
//                require(value.getMethod("getProperties").declaringClass == value)
//            } catch (noSuchMethodException: Exception) {
//                throw RegisterException(
//                    """
//                    Custom block class: ${value.simpleName} must override method:
//
//                    @Override
//                    public @NotNull BlockProperties getProperties() {
//                        return PROPERTIES;
//                    }
//                    """.trimIndent()
//                )
//            }
//            val key = blockProperties.identifier
//            val memberLoader: FastMemberLoader =
//                IRegistry.fastMemberLoaderCache.computeIfAbsent(plugin.name) {
//                    FastMemberLoader(plugin.pluginClassLoader)
//                }
//            val c = FastConstructor.create(value.getConstructor(BlockState::class), memberLoader, false)
//            if (CACHE_CONSTRUCTORS.putIfAbsent(key, c) == null) {
//                if (CustomBlock::class.isAssignableFrom(value)) {
//                    val customBlock = c.invoke(null as Any?) as CustomBlock
//                    val customBlockDefinitions =
//                        CUSTOM_BLOCK_DEFINITIONS.computeIfAbsent(plugin) { ArrayList() }
//                    customBlockDefinitions.add(customBlock.definition)
//                    val rid = 255 - CustomBlockDefinition.getRuntimeId(customBlock.id)
//                    Registries.ITEM_RUNTIMEID.registerCustomRuntimeItem(RuntimeEntry(customBlock.id, rid, false))
//                    if (customBlock.shouldBeRegisteredInCreative()) {
//                        val itemBlock = ItemBlock(customBlock.toBlock())
//                        itemBlock.netId = (null)
//                        Registries.CREATIVE.addCreativeItem(itemBlock)
//                    }
//                    KEYSET.add(key)
//                    PROPERTIES[key] = blockProperties
//                    blockProperties.specialValueMap.values.forEach(Consumer { v: BlockState ->
//                        Registries.BLOCKSTATE.registerInternal(v)
//                    })
//                } else {
//                    throw RegisterException("Register Error,must implement the CustomBlock interface!")
//                }
//            } else {
//                throw RegisterException("There custom block has already been registered with the identifier: $key")
//            }
//        } catch (e: NoSuchFieldException) {
//            throw RegisterException(e)
//        } catch (e: IllegalAccessException) {
//            throw RegisterException(e)
//        } catch (e: NoSuchMethodException) {
//            throw RegisterException(e)
//        } catch (e: Throwable) {
//            throw RuntimeException(e)
//        }
//    }

    private fun register0(key: String, value: KClass<out Block>) {
        try {
            register(key, value)
        } catch (e: Exception) {
            log.error("", e)
        }
    }

    val customBlockDefinitionList: @UnmodifiableView MutableList<CustomBlockDefinition>
        get() = CUSTOM_BLOCK_DEFINITIONS.values.stream().flatMap { it.stream() }.toList()

    override fun reload() {
        isLoad.set(false)
        KEYSET.clear()
        CACHE_CONSTRUCTORS.clear()
        PROPERTIES.clear()
        CUSTOM_BLOCK_DEFINITIONS.clear()
        init()
    }

    fun getBlockProperties(identifier: String): BlockProperties {
        return PROPERTIES[identifier] ?: BlockAir.properties
    }

    override operator fun get(key: String): Block? {
        val constructor = CACHE_CONSTRUCTORS[key] ?: return null
        return constructor.invoke(null)
    }

    fun get(identifier: String, x: Int, y: Int, z: Int): Block? {
        val constructor = CACHE_CONSTRUCTORS[identifier] ?: return null
        try {
            val b = constructor.invoke(null)
            b.position.x = x.toDouble()
            b.position.y = y.toDouble()
            b.position.z = z.toDouble()
            return b
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

    fun get(identifier: String, x: Int, y: Int, z: Int, level: Level): Block? {
        val constructor = CACHE_CONSTRUCTORS[identifier] ?: return null
        try {
            val b = constructor.invoke(null)
            b.position.x = x.toDouble()
            b.position.y = y.toDouble()
            b.position.z = z.toDouble()
            b.level = level
            return b
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

    fun get(identifier: String, x: Int, y: Int, z: Int, layer: Int, level: Level): Block? {
        val constructor = CACHE_CONSTRUCTORS[identifier] ?: return null
        try {
            val b = constructor.invoke(null)
            b.position.x = x.toDouble()
            b.position.y = y.toDouble()
            b.position.z = z.toDouble()
            b.level = level
            b.layer = layer
            return b
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

    operator fun get(blockState: BlockState?): Block? {
        if (blockState == null) return null

        val constructor = CACHE_CONSTRUCTORS[blockState.identifier] ?: return null

        try {
            return constructor.invoke(blockState)
        } catch (e: Throwable) {
            return null
        }
    }

    fun get(blockState: BlockState, x: Int, y: Int, z: Int): Block? {
        val constructor = CACHE_CONSTRUCTORS[blockState.identifier] ?: return null
        try {
            val b = constructor.invoke(blockState)
            b.position.x = x.toDouble()
            b.position.y = y.toDouble()
            b.position.z = z.toDouble()
            return b
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

    operator fun get(blockState: BlockState, x: Int, y: Int, z: Int, level: Level): Block? {
        val constructor = CACHE_CONSTRUCTORS[blockState.identifier] ?: return null
        try {
            val b = constructor.invoke(blockState)
            b.position.x = x.toDouble()
            b.position.y = y.toDouble()
            b.position.z = z.toDouble()
            b.level = level
            return b
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

    operator fun get(blockState: BlockState, x: Int, y: Int, z: Int, layer: Int, level: Level): Block? {
        val constructor = CACHE_CONSTRUCTORS[blockState.identifier] ?: return null
        try {
            val b = constructor.invoke(blockState)
            b.position.x = x.toDouble()
            b.position.y = y.toDouble()
            b.position.z = z.toDouble()
            b.level = level
            b.layer = layer
            return b
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private val isLoad = AtomicBoolean(false)
        private val KEYSET: MutableSet<String> = HashSet()
        val CACHE_CONSTRUCTORS = mutableMapOf<String, (BlockState?) -> Block>()
        val PROPERTIES = mutableMapOf<String, BlockProperties>()
        private val CUSTOM_BLOCK_DEFINITIONS: MutableMap<Plugin, MutableList<CustomBlockDefinition>> = LinkedHashMap()

        val CLASSES = mutableMapOf<String, KClass<out Block>>()

        val skipBlockSet: Set<String> = setOf(
            "minecraft:camera",
            "minecraft:chalkboard",
            "minecraft:chemical_heat",
            "minecraft:compound_creator",
            "minecraft:element_constructor",
            "minecraft:lab_table",
            "minecraft:material_reducer",
            "minecraft:colored_torch_purple",
            "minecraft:colored_torch_blue",
            "minecraft:colored_torch_red",
            "minecraft:colored_torch_green",
            "minecraft:deprecated_anvil",
            "minecraft:deprecated_purpur_block_1",
            "minecraft:deprecated_purpur_block_2",
            "minecraft:element_0",
            "minecraft:element_1",
            "minecraft:element_10",
            "minecraft:element_100",
            "minecraft:element_101",
            "minecraft:element_102",
            "minecraft:element_103",
            "minecraft:element_104",
            "minecraft:element_105",
            "minecraft:element_106",
            "minecraft:element_107",
            "minecraft:element_108",
            "minecraft:element_109",
            "minecraft:element_11",
            "minecraft:element_110",
            "minecraft:element_111",
            "minecraft:element_112",
            "minecraft:element_113",
            "minecraft:element_114",
            "minecraft:element_115",
            "minecraft:element_116",
            "minecraft:element_117",
            "minecraft:element_118",
            "minecraft:element_12",
            "minecraft:element_13",
            "minecraft:element_14",
            "minecraft:element_15",
            "minecraft:element_16",
            "minecraft:element_17",
            "minecraft:element_18",
            "minecraft:element_19",
            "minecraft:element_2",
            "minecraft:element_20",
            "minecraft:element_21",
            "minecraft:element_22",
            "minecraft:element_23",
            "minecraft:element_24",
            "minecraft:element_25",
            "minecraft:element_26",
            "minecraft:element_27",
            "minecraft:element_28",
            "minecraft:element_29",
            "minecraft:element_3",
            "minecraft:element_30",
            "minecraft:element_31",
            "minecraft:element_32",
            "minecraft:element_33",
            "minecraft:element_34",
            "minecraft:element_35",
            "minecraft:element_36",
            "minecraft:element_37",
            "minecraft:element_38",
            "minecraft:element_39",
            "minecraft:element_4",
            "minecraft:element_40",
            "minecraft:element_41",
            "minecraft:element_42",
            "minecraft:element_43",
            "minecraft:element_44",
            "minecraft:element_45",
            "minecraft:element_46",
            "minecraft:element_47",
            "minecraft:element_48",
            "minecraft:element_49",
            "minecraft:element_5",
            "minecraft:element_50",
            "minecraft:element_51",
            "minecraft:element_52",
            "minecraft:element_53",
            "minecraft:element_54",
            "minecraft:element_55",
            "minecraft:element_56",
            "minecraft:element_57",
            "minecraft:element_58",
            "minecraft:element_59",
            "minecraft:element_6",
            "minecraft:element_60",
            "minecraft:element_61",
            "minecraft:element_62",
            "minecraft:element_63",
            "minecraft:element_64",
            "minecraft:element_65",
            "minecraft:element_66",
            "minecraft:element_67",
            "minecraft:element_68",
            "minecraft:element_69",
            "minecraft:element_7",
            "minecraft:element_70",
            "minecraft:element_71",
            "minecraft:element_72",
            "minecraft:element_73",
            "minecraft:element_74",
            "minecraft:element_75",
            "minecraft:element_76",
            "minecraft:element_77",
            "minecraft:element_78",
            "minecraft:element_79",
            "minecraft:element_8",
            "minecraft:element_80",
            "minecraft:element_81",
            "minecraft:element_82",
            "minecraft:element_83",
            "minecraft:element_84",
            "minecraft:element_85",
            "minecraft:element_86",
            "minecraft:element_87",
            "minecraft:element_88",
            "minecraft:element_89",
            "minecraft:element_9",
            "minecraft:element_90",
            "minecraft:element_91",
            "minecraft:element_92",
            "minecraft:element_93",
            "minecraft:element_94",
            "minecraft:element_95",
            "minecraft:element_96",
            "minecraft:element_97",
            "minecraft:element_98",
            "minecraft:element_99",
            "minecraft:hard_black_stained_glass",
            "minecraft:hard_black_stained_glass_pane",
            "minecraft:hard_blue_stained_glass",
            "minecraft:hard_blue_stained_glass_pane",
            "minecraft:hard_brown_stained_glass",
            "minecraft:hard_brown_stained_glass_pane",
            "minecraft:hard_cyan_stained_glass",
            "minecraft:hard_cyan_stained_glass_pane",
            "minecraft:hard_glass",
            "minecraft:hard_glass_pane",
            "minecraft:hard_gray_stained_glass",
            "minecraft:hard_gray_stained_glass_pane",
            "minecraft:hard_green_stained_glass",
            "minecraft:hard_green_stained_glass_pane",
            "minecraft:hard_light_blue_stained_glass",
            "minecraft:hard_light_blue_stained_glass_pane",
            "minecraft:hard_light_gray_stained_glass",
            "minecraft:hard_light_gray_stained_glass_pane",
            "minecraft:hard_lime_stained_glass",
            "minecraft:hard_lime_stained_glass_pane",
            "minecraft:hard_magenta_stained_glass",
            "minecraft:hard_magenta_stained_glass_pane",
            "minecraft:hard_orange_stained_glass",
            "minecraft:hard_orange_stained_glass_pane",
            "minecraft:hard_pink_stained_glass",
            "minecraft:hard_pink_stained_glass_pane",
            "minecraft:hard_purple_stained_glass",
            "minecraft:hard_purple_stained_glass_pane",
            "minecraft:hard_red_stained_glass",
            "minecraft:hard_red_stained_glass_pane",
            "minecraft:hard_white_stained_glass",
            "minecraft:hard_white_stained_glass_pane",
            "minecraft:hard_yellow_stained_glass",
            "minecraft:hard_yellow_stained_glass_pane",
            "minecraft:underwater_torch",
            "minecraft:underwater_tnt"
        )
    }
}
