package org.chorus_oss.chorus.experimental.block.state

import org.chorus_oss.chorus.block.property.enums.*
import org.chorus_oss.chorus.math.BlockFace

@Suppress("unused")
object CommonStates {
    val active: BlockState<Boolean> = BlockState.from("active")

    val age16: BlockState<Int> = BlockState.from("age", 15)

    val age3: BlockState<Int> = BlockState.from("age", 2)

    val age4: BlockState<Int> = BlockState.from("age", 3)

    val age6: BlockState<Int> = BlockState.from("age", 5)

    val ageBit: BlockState<Boolean> = BlockState.from("age_bit")

    val allowUnderwaterBit: BlockState<Boolean> = BlockState.from("allow_underwater_bit")

    val attachedBit: BlockState<Boolean> = BlockState.from("attached_bit")

    val attachment: BlockState<String> = BlockState.from(
        "attachment", Attachment::class
    )

    val bambooLeafSize: BlockState<String> = BlockState.from(
        "bamboo_leaf_size", BambooLeafSize::class
    )

    val bambooStalkThickness: BlockState<String> = BlockState.from(
        "bamboo_stalk_thickness", BambooStalkThickness::class
    )

    val bigDripleafHead: BlockState<Boolean> = BlockState.from("big_dripleaf_head")

    val bigDripleafTilt: BlockState<String> = BlockState.from(
        "big_dripleaf_tilt", BigDripleafTilt::class
    )

    val biteCounter: BlockState<Int> = BlockState.from("bite_counter", 6)

    val blockLightLevel: BlockState<Int> = BlockState.from("block_light_level", 15)

    val bloom: BlockState<Boolean> = BlockState.from("bloom")

    val booksStored: BlockState<Int> = BlockState.from("books_stored", 63)

    val brewingStandSlotABit: BlockState<Boolean> = BlockState.from("brewing_stand_slot_a_bit")

    val brewingStandSlotBBit: BlockState<Boolean> = BlockState.from("brewing_stand_slot_b_bit")

    val brewingStandSlotCBit: BlockState<Boolean> = BlockState.from("brewing_stand_slot_c_bit")

    val brushedProgress: BlockState<Int> = BlockState.from("brushed_progress", 3)

    val buttonPressedBit: BlockState<Boolean> = BlockState.from("button_pressed_bit")

    val canSummon: BlockState<Boolean> = BlockState.from("can_summon")

    val candles: BlockState<Int> = BlockState.from("candles", 3)

    val cauldronLiquid: BlockState<String> = BlockState.from(
        "cauldron_liquid", CauldronLiquid::class
    )

    val chemistryTableType: BlockState<String> = BlockState.from(
        "chemistry_table_type", ChemistryTableType::class
    )

    val chiselType: BlockState<String> = BlockState.from(
        "chisel_type", ChiselType::class
    )

    val clusterCount: BlockState<Int> = BlockState.from("cluster_count", 3)

    val color: BlockState<String> = BlockState.from(
        "color", Color::class
    )

    val colorBit: BlockState<Boolean> = BlockState.from("color_bit")

    val composterFillLevel: BlockState<Int> = BlockState.from("composter_fill_level", 8)

    val conditionalBit: BlockState<Boolean> = BlockState.from("conditional_bit")

    val coralDirection: BlockState<Int> = BlockState.from("coral_direction", 3)

    val coralFanDirection: BlockState<Int> = BlockState.from("coral_fan_direction", 1)

    val coralHangTypeBit: BlockState<Boolean> = BlockState.from("coral_hang_type_bit")

    val coveredBit: BlockState<Boolean> = BlockState.from("covered_bit")

    val crackedState: BlockState<String> = BlockState.from(
        "cracked_state", CrackedState::class
    )

    val crafting: BlockState<Boolean> = BlockState.from("crafting")

    val creakingHeartState: BlockState<String> = BlockState.from(
        "creaking_heart_state", CreakingHeartState::class
    )

    val damage: BlockState<String> = BlockState.from(
        "damage", Damage::class
    )

    val deadBit: BlockState<Boolean> = BlockState.from("dead_bit")

    val deprecated: BlockState<Int> = BlockState.from("deprecated", 3)

    // Horizontal-index based
    val direction: BlockState<Int> = BlockState.from("direction", 3)

    val dirtType: BlockState<String> = BlockState.from(
        "dirt_type", DirtType::class
    )

    val disarmedBit: BlockState<Boolean> = BlockState.from("disarmed_bit")

    val doorHingeBit: BlockState<Boolean> = BlockState.from("door_hinge_bit")

    val doublePlantType: BlockState<String> = BlockState.from(
        "double_plant_type", DoublePlantType::class
    )

    val dragDown: BlockState<Boolean> = BlockState.from("drag_down")

    val dripstoneThickness: BlockState<String> = BlockState.from(
        "dripstone_thickness", DripstoneThickness::class
    )

    val endPortalEyeBit: BlockState<Boolean> = BlockState.from("end_portal_eye_bit")

    val explodeBit: BlockState<Boolean> = BlockState.from("explode_bit")

    val extinguished: BlockState<Boolean> = BlockState.from("extinguished")

    val coralColor: BlockState<String> = BlockState.from(
        "coral_color", CoralColor::class
    )

    // Base index for BlockFace
    val facingDirection: BlockState<Int> = BlockState.from("facing_direction", 5)

    val fillLevel: BlockState<Int> = BlockState.from("fill_level", 6)

    // Base index for CompassRoseDirection
    val groundSignDirection: BlockState<Int> = BlockState.from("ground_sign_direction", 15)

    val growingPlantAge: BlockState<Int> = BlockState.from("growing_plant_age", 25)

    val growth: BlockState<Int> = BlockState.from("growth", 7)

    val hanging: BlockState<Boolean> = BlockState.from("hanging")

    val headPieceBit: BlockState<Boolean> = BlockState.from("head_piece_bit")

    val height: BlockState<Int> = BlockState.from("height", 7)

    val honeyLevel: BlockState<Int> = BlockState.from("honey_level", 5)

    val hugeMushroomBits: BlockState<Int> = BlockState.from("huge_mushroom_bits", 15)

    val inWallBit: BlockState<Boolean> = BlockState.from("in_wall_bit")

    val infiniburnBit: BlockState<Boolean> = BlockState.from("infiniburn_bit")

    val itemFrameMapBit: BlockState<Boolean> = BlockState.from("item_frame_map_bit")

    val itemFramePhotoBit: BlockState<Boolean> = BlockState.from("item_frame_photo_bit")

    val kelpAge: BlockState<Int> = BlockState.from("kelp_age", 25)

    val leverDirection: BlockState<String> = BlockState.from(
        "lever_direction", LeverDirection::class
    )

    val liquidDepth: BlockState<Int> = BlockState.from("liquid_depth", 15)

    val lit: BlockState<Boolean> = BlockState.from("lit")

    val minecraftBlockFace: BlockState<String> = BlockState.from(
        "minecraft:block_face", BlockFace::class
    )

    // CommonPropertyMap#CARDINAL_BLOCKFACE
    val minecraftCardinalDirection: BlockState<String> = BlockState.from(
        "minecraft:cardinal_direction", MinecraftCardinalDirection::class
    )

    val minecraftFacingDirection: BlockState<String> = BlockState.from(
        "minecraft:facing_direction", BlockFace::class
    )

    val minecraftVerticalHalf: BlockState<String> = BlockState.from(
        "minecraft:vertical_half", MinecraftVerticalHalf::class
    )

    val moisturizedAmount: BlockState<Int> = BlockState.from("moisturized_amount", 7)

    val monsterEggStoneType: BlockState<String> = BlockState.from(
        "monster_egg_stone_type", MonsterEggStoneType::class
    )

    val multiFaceDirectionBits: BlockState<Int> = BlockState.from("multi_face_direction_bits", 63)

    val newLeafType: BlockState<String> = BlockState.from(
        "new_leaf_type", NewLeafType::class
    )

    val occupiedBit: BlockState<Boolean> = BlockState.from("occupied_bit")

    val oldLeafType: BlockState<String> = BlockState.from(
        "old_leaf_type", OldLeafType::class
    )

    val openBit: BlockState<Boolean> = BlockState.from("open_bit")

    val orientation: BlockState<String> = BlockState.from(
        "orientation", Orientation::class
    )

    val outputLitBit: BlockState<Boolean> = BlockState.from("output_lit_bit")

    val outputSubtractBit: BlockState<Boolean> = BlockState.from("output_subtract_bit")

    val persistentBit: BlockState<Boolean> = BlockState.from("persistent_bit")

    val pillarAxis: BlockState<String> = BlockState.from(
        "pillar_axis", BlockFace.Axis::class
    )

    val portalAxis: BlockState<String> = BlockState.from(
        "portal_axis", PortalAxis::class
    )

    val poweredBit: BlockState<Boolean> = BlockState.from("powered_bit")

    val prismarineBlockType: BlockState<String> = BlockState.from(
        "prismarine_block_type", PrismarineBlockType::class
    )

    val propaguleStage: BlockState<Int> = BlockState.from("propagule_stage", 4)

    val railDataBit: BlockState<Boolean> = BlockState.from("rail_data_bit")

    val railDirection10: BlockState<Int> = BlockState.from("rail_direction", 9)

    val railDirection6: BlockState<Int> = BlockState.from("rail_direction", 5)

    val redstoneSignal: BlockState<Int> = BlockState.from("redstone_signal", 15)

    val repeaterDelay: BlockState<Int> = BlockState.from("repeater_delay", 3)

    val respawnAnchorCharge: BlockState<Int> = BlockState.from("respawn_anchor_charge", 4)

    val rotation: BlockState<Int> = BlockState.from("rotation", 3)

    val sandType: BlockState<String> = BlockState.from(
        "sand_type", SandType::class
    )

    val sculkSensorPhase: BlockState<Int> = BlockState.from("sculk_sensor_phase", 2)

    val seaGrassType: BlockState<String> = BlockState.from(
        "sea_grass_type", SeaGrassType::class
    )

    val spongeType: BlockState<String> = BlockState.from(
        "sponge_type", SpongeType::class
    )

    val stability: BlockState<Int> = BlockState.from("stability", 7)

    val stabilityCheck: BlockState<Boolean> = BlockState.from("stability_check")

    val stoneBrickType: BlockState<String> = BlockState.from(
        "stone_brick_type", StoneBrickType::class
    )

    val stoneSlabType: BlockState<String> = BlockState.from(
        "stone_slab_type", StoneSlabType::class
    )

    val stoneSlabType2: BlockState<String> = BlockState.from(
        "stone_slab_type_2", StoneSlabType2::class
    )

    val stoneSlabType3: BlockState<String> = BlockState.from(
        "stone_slab_type_3", StoneSlabType3::class
    )

    val stoneSlabType4: BlockState<String> = BlockState.from(
        "stone_slab_type_4", StoneSlabType4::class
    )

    val strippedBit: BlockState<Boolean> = BlockState.from("stripped_bit")

    val structureBlockType: BlockState<String> = BlockState.from(
        "structure_block_type", StructureBlockType::class
    )

    val structureVoidType: BlockState<String> = BlockState.from(
        "structure_void_type", StructureVoidType::class
    )

    val suspendedBit: BlockState<Boolean> = BlockState.from("suspended_bit")

    val tallGrassType: BlockState<String> = BlockState.from(
        "tall_grass_type", TallGrassType::class
    )

    val toggleBit: BlockState<Boolean> = BlockState.from("toggle_bit")

    val torchFacingDirection: BlockState<String> = BlockState.from(
        "torch_facing_direction", TorchFacingDirection::class
    )

    val triggeredBit: BlockState<Boolean> = BlockState.from("triggered_bit")

    val turtleEggCount: BlockState<String> = BlockState.from(
        "turtle_egg_count", TurtleEggCount::class
    )

    val twistingVinesAge: BlockState<Int> = BlockState.from("twisting_vines_age", 25)

    val updateBit: BlockState<Boolean> = BlockState.from("update_bit")

    val upperBlockBit: BlockState<Boolean> = BlockState.from("upper_block_bit")

    val upsideDownBit: BlockState<Boolean> = BlockState.from("upside_down_bit")

    val vineDirectionBits: BlockState<Int> = BlockState.from("vine_direction_bits", 15)

    val wallBlockType: BlockState<String> = BlockState.from(
        "wall_block_type", WallBlockType::class
    )

    val wallConnectionTypeEast: BlockState<String> = BlockState.from(
        "wall_connection_type_east", WallConnectionType::class
    )

    val wallConnectionTypeNorth: BlockState<String> = BlockState.from(
        "wall_connection_type_north", WallConnectionType::class
    )

    val wallConnectionTypeSouth: BlockState<String> = BlockState.from(
        "wall_connection_type_south", WallConnectionType::class
    )

    val wallConnectionTypeWest: BlockState<String> = BlockState.from(
        "wall_connection_type_west", WallConnectionType::class
    )

    val paleMossCarpetSideEast: BlockState<String> = BlockState.from(
        "pale_moss_carpet_side_east", PaleMossCarpetSide::class
    )

    val paleMossCarpetSideNorth: BlockState<String> = BlockState.from(
        "pale_moss_carpet_side_north", PaleMossCarpetSide::class
    )

    val paleMossCarpetSideSouth: BlockState<String> = BlockState.from(
        "pale_moss_carpet_side_south", PaleMossCarpetSide::class
    )

    val paleMossCarpetSideWest: BlockState<String> = BlockState.from(
        "pale_moss_carpet_side_west", PaleMossCarpetSide::class
    )

    val tip: BlockState<Boolean> = BlockState.from("tip")

    val natural: BlockState<Boolean> = BlockState.from("natural")

    val wallPostBit: BlockState<Boolean> = BlockState.from("wall_post_bit")

    val weepingVinesAge: BlockState<Int> = BlockState.from("weeping_vines_age", 25)

    val weirdoDirection: BlockState<Int> = BlockState.from("weirdo_direction", 3)

    val woodType: BlockState<String> = BlockState.from(
        "wood_type", WoodType::class
    )

    val trialSpawnerState: BlockState<Int> = BlockState.from("trial_spawner_state", 5)

    val vaultState: BlockState<String> = BlockState.from(
        "vault_state", VaultState::class
    )

    val ominous: BlockState<Boolean> = BlockState.from("ominous")
}