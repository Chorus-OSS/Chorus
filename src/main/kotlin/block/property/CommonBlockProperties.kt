package org.chorus_oss.chorus.block.property

import org.chorus_oss.chorus.block.property.enums.*
import org.chorus_oss.chorus.block.property.type.BlockPropertyType
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.block.property.type.EnumPropertyType
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.protocol.types.structure.StructureBlockType

interface CommonBlockProperties {
    companion object {
        fun values(): List<BlockPropertyType<*>> {
            return VALUES
        }


        val ACTIVE: BooleanPropertyType = BooleanPropertyType.of("active", false)


        val AGE_16: IntPropertyType = IntPropertyType.of("age", 0, 15, 0)


        val AGE_3: IntPropertyType = IntPropertyType.of("age", 0, 2, 0)


        val AGE_4: IntPropertyType = IntPropertyType.of("age", 0, 3, 0)


        val AGE_6: IntPropertyType = IntPropertyType.of("age", 0, 5, 0)


        val AGE_BIT: BooleanPropertyType = BooleanPropertyType.of("age_bit", false)

        val ALLOW_UNDERWATER_BIT: BooleanPropertyType = BooleanPropertyType.of("allow_underwater_bit", false)


        val ATTACHED_BIT: BooleanPropertyType = BooleanPropertyType.of("attached_bit", false)


        val ATTACHMENT: EnumPropertyType<Attachment> = EnumPropertyType.of(
            "attachment",
            Attachment::class.java, Attachment.entries[0]
        )


        val BAMBOO_LEAF_SIZE: EnumPropertyType<BambooLeafSize> = EnumPropertyType.of(
            "bamboo_leaf_size",
            BambooLeafSize::class.java, BambooLeafSize.entries[0]
        )


        val BAMBOO_STALK_THICKNESS: EnumPropertyType<BambooStalkThickness> = EnumPropertyType.of(
            "bamboo_stalk_thickness",
            BambooStalkThickness::class.java, BambooStalkThickness.entries[0]
        )


        val BIG_DRIPLEAF_HEAD: BooleanPropertyType = BooleanPropertyType.of("big_dripleaf_head", false)


        val BIG_DRIPLEAF_TILT: EnumPropertyType<BigDripleafTilt> = EnumPropertyType.of(
            "big_dripleaf_tilt",
            BigDripleafTilt::class.java, BigDripleafTilt.entries[0]
        )


        val BITE_COUNTER: IntPropertyType = IntPropertyType.of("bite_counter", 0, 6, 0)


        val BLOCK_LIGHT_LEVEL: IntPropertyType = IntPropertyType.of("block_light_level", 0, 15, 0)


        val BLOOM: BooleanPropertyType = BooleanPropertyType.of("bloom", false)


        val BOOKS_STORED: IntPropertyType = IntPropertyType.of("books_stored", 0, 63, 0)


        val BREWING_STAND_SLOT_A_BIT: BooleanPropertyType = BooleanPropertyType.of("brewing_stand_slot_a_bit", false)


        val BREWING_STAND_SLOT_B_BIT: BooleanPropertyType = BooleanPropertyType.of("brewing_stand_slot_b_bit", false)


        val BREWING_STAND_SLOT_C_BIT: BooleanPropertyType = BooleanPropertyType.of("brewing_stand_slot_c_bit", false)


        val BRUSHED_PROGRESS: IntPropertyType = IntPropertyType.of("brushed_progress", 0, 3, 0)


        val BUTTON_PRESSED_BIT: BooleanPropertyType = BooleanPropertyType.of("button_pressed_bit", false)


        val CAN_SUMMON: BooleanPropertyType = BooleanPropertyType.of("can_summon", false)


        val CANDLES: IntPropertyType = IntPropertyType.of("candles", 0, 3, 0)


        val CAULDRON_LIQUID: EnumPropertyType<CauldronLiquid> = EnumPropertyType.of(
            "cauldron_liquid",
            CauldronLiquid::class.java, CauldronLiquid.entries[0]
        )


        val CHEMISTRY_TABLE_TYPE: EnumPropertyType<ChemistryTableType> = EnumPropertyType.of(
            "chemistry_table_type",
            ChemistryTableType::class.java, ChemistryTableType.entries[0]
        )

        val CHISEL_TYPE: EnumPropertyType<ChiselType> = EnumPropertyType.of(
            "chisel_type",
            ChiselType::class.java, ChiselType.entries[0]
        )


        val CLUSTER_COUNT: IntPropertyType = IntPropertyType.of("cluster_count", 0, 3, 0)

        val COLOR: EnumPropertyType<Color> = EnumPropertyType.of(
            "color",
            Color::class.java, Color.entries[0]
        )


        val COLOR_BIT: BooleanPropertyType = BooleanPropertyType.of("color_bit", false)


        val COMPOSTER_FILL_LEVEL: IntPropertyType = IntPropertyType.of("composter_fill_level", 0, 8, 0)


        val CONDITIONAL_BIT: BooleanPropertyType = BooleanPropertyType.of("conditional_bit", false)


        val CORAL_DIRECTION: IntPropertyType = IntPropertyType.of("coral_direction", 0, 3, 0)


        val CORAL_FAN_DIRECTION: IntPropertyType = IntPropertyType.of("coral_fan_direction", 0, 1, 0)


        val CORAL_HANG_TYPE_BIT: BooleanPropertyType = BooleanPropertyType.of("coral_hang_type_bit", false)


        val COVERED_BIT: BooleanPropertyType = BooleanPropertyType.of("covered_bit", false)


        val CRACKED_STATE: EnumPropertyType<CrackedState> = EnumPropertyType.of(
            "cracked_state",
            CrackedState::class.java, CrackedState.entries[0]
        )


        val CRAFTING: BooleanPropertyType = BooleanPropertyType.of("crafting", false)


        val CREAKING_HEART_STATE: EnumPropertyType<CreakingHeartState> = EnumPropertyType.of(
            "creaking_heart_state",
            CreakingHeartState::class.java, CreakingHeartState.entries[0]
        )

        val DAMAGE: EnumPropertyType<Damage> = EnumPropertyType.of(
            "damage", Damage::class.java,
            Damage.entries[0]
        )


        val DEAD_BIT: BooleanPropertyType = BooleanPropertyType.of("dead_bit", false)


        val DEPRECATED: IntPropertyType = IntPropertyType.of("deprecated", 0, 3, 0)

        // Horizontal-index based

        val DIRECTION: IntPropertyType = IntPropertyType.of("direction", 0, 3, 0)


        val DIRT_TYPE: EnumPropertyType<DirtType> = EnumPropertyType.of(
            "dirt_type",
            DirtType::class.java, DirtType.entries[0]
        )


        val DISARMED_BIT: BooleanPropertyType = BooleanPropertyType.of("disarmed_bit", false)


        val DOOR_HINGE_BIT: BooleanPropertyType = BooleanPropertyType.of("door_hinge_bit", false)


        val DOUBLE_PLANT_TYPE: EnumPropertyType<DoublePlantType> = EnumPropertyType.of(
            "double_plant_type",
            DoublePlantType::class.java, DoublePlantType.entries[0]
        )


        val DRAG_DOWN: BooleanPropertyType = BooleanPropertyType.of("drag_down", false)


        val DRIPSTONE_THICKNESS: EnumPropertyType<DripstoneThickness> = EnumPropertyType.of(
            "dripstone_thickness",
            DripstoneThickness::class.java, DripstoneThickness.entries[0]
        )


        val END_PORTAL_EYE_BIT: BooleanPropertyType = BooleanPropertyType.of("end_portal_eye_bit", false)


        val EXPLODE_BIT: BooleanPropertyType = BooleanPropertyType.of("explode_bit", false)


        val EXTINGUISHED: BooleanPropertyType = BooleanPropertyType.of("extinguished", false)


        val CORAL_COLOR: EnumPropertyType<CoralColor> = EnumPropertyType.of(
            "coral_color",
            CoralColor::class.java, CoralColor.entries[0]
        )

        //Base index for BlockFace

        val FACING_DIRECTION: IntPropertyType = IntPropertyType.of("facing_direction", 0, 5, 0)


        val FILL_LEVEL: IntPropertyType = IntPropertyType.of("fill_level", 0, 6, 0)

        //CompassRoseDirection base index

        val GROUND_SIGN_DIRECTION: IntPropertyType = IntPropertyType.of("ground_sign_direction", 0, 15, 0)


        val GROWING_PLANT_AGE: IntPropertyType = IntPropertyType.of("growing_plant_age", 0, 25, 0)


        val GROWTH: IntPropertyType = IntPropertyType.of("growth", 0, 7, 0)


        val HANGING: BooleanPropertyType = BooleanPropertyType.of("hanging", false)


        val HEAD_PIECE_BIT: BooleanPropertyType = BooleanPropertyType.of("head_piece_bit", false)


        val HEIGHT: IntPropertyType = IntPropertyType.of("height", 0, 7, 0)

        val HONEY_LEVEL: IntPropertyType = IntPropertyType.of("honey_level", 0, 5, 0)


        val HUGE_MUSHROOM_BITS: IntPropertyType = IntPropertyType.of("huge_mushroom_bits", 0, 15, 0)


        val IN_WALL_BIT: BooleanPropertyType = BooleanPropertyType.of("in_wall_bit", false)


        val INFINIBURN_BIT: BooleanPropertyType = BooleanPropertyType.of("infiniburn_bit", false)


        val ITEM_FRAME_MAP_BIT: BooleanPropertyType = BooleanPropertyType.of("item_frame_map_bit", false)


        val ITEM_FRAME_PHOTO_BIT: BooleanPropertyType = BooleanPropertyType.of("item_frame_photo_bit", false)


        val KELP_AGE: IntPropertyType = IntPropertyType.of("kelp_age", 0, 25, 0)


        val LEVER_DIRECTION: EnumPropertyType<LeverDirection> = EnumPropertyType.of(
            "lever_direction",
            LeverDirection::class.java, LeverDirection.entries[0]
        )


        val LIQUID_DEPTH: IntPropertyType = IntPropertyType.of("liquid_depth", 0, 15, 0)


        val LIT: BooleanPropertyType = BooleanPropertyType.of("lit", false)


        val MINECRAFT_BLOCK_FACE: EnumPropertyType<BlockFace> = EnumPropertyType.of(
            "minecraft:block_face",
            BlockFace::class.java, BlockFace.DOWN
        )

        //CommonPropertyMap#CARDINAL_BLOCKFACE

        val MINECRAFT_CARDINAL_DIRECTION: EnumPropertyType<MinecraftCardinalDirection> = EnumPropertyType.of(
            "minecraft:cardinal_direction",
            MinecraftCardinalDirection::class.java, MinecraftCardinalDirection.entries[0]
        )


        val MINECRAFT_FACING_DIRECTION: EnumPropertyType<BlockFace> = EnumPropertyType.of(
            "minecraft:facing_direction",
            BlockFace::class.java, BlockFace.entries[0]
        )


        val MINECRAFT_VERTICAL_HALF: EnumPropertyType<MinecraftVerticalHalf> = EnumPropertyType.of(
            "minecraft:vertical_half",
            MinecraftVerticalHalf::class.java, MinecraftVerticalHalf.entries[0]
        )


        val MOISTURIZED_AMOUNT: IntPropertyType = IntPropertyType.of("moisturized_amount", 0, 7, 0)


        val MONSTER_EGG_STONE_TYPE: EnumPropertyType<MonsterEggStoneType> = EnumPropertyType.of(
            "monster_egg_stone_type",
            MonsterEggStoneType::class.java, MonsterEggStoneType.entries[0]
        )


        val MULTI_FACE_DIRECTION_BITS: IntPropertyType = IntPropertyType.of("multi_face_direction_bits", 0, 63, 0)

        val NEW_LEAF_TYPE: EnumPropertyType<NewLeafType> = EnumPropertyType.of(
            "new_leaf_type",
            NewLeafType::class.java, NewLeafType.entries[0]
        )


        val OCCUPIED_BIT: BooleanPropertyType = BooleanPropertyType.of("occupied_bit", false)

        val OLD_LEAF_TYPE: EnumPropertyType<OldLeafType> = EnumPropertyType.of(
            "old_leaf_type",
            OldLeafType::class.java, OldLeafType.entries[0]
        )


        val OPEN_BIT: BooleanPropertyType = BooleanPropertyType.of("open_bit", false)


        val ORIENTATION: EnumPropertyType<Orientation> = EnumPropertyType.of(
            "orientation",
            Orientation::class.java, Orientation.entries[0]
        )


        val OUTPUT_LIT_BIT: BooleanPropertyType = BooleanPropertyType.of("output_lit_bit", false)


        val OUTPUT_SUBTRACT_BIT: BooleanPropertyType = BooleanPropertyType.of("output_subtract_bit", false)


        val PERSISTENT_BIT: BooleanPropertyType = BooleanPropertyType.of("persistent_bit", false)


        val PILLAR_AXIS: EnumPropertyType<BlockFace.Axis> = EnumPropertyType.of(
            "pillar_axis",
            BlockFace.Axis::class.java, BlockFace.Axis.entries.toTypedArray()[0]
        )


        val PORTAL_AXIS: EnumPropertyType<PortalAxis> = EnumPropertyType.of(
            "portal_axis",
            PortalAxis::class.java, PortalAxis.entries[0]
        )


        val POWERED_BIT: BooleanPropertyType = BooleanPropertyType.of("powered_bit", false)


        val PRISMARINE_BLOCK_TYPE: EnumPropertyType<PrismarineBlockType> = EnumPropertyType.of(
            "prismarine_block_type",
            PrismarineBlockType::class.java, PrismarineBlockType.entries[0]
        )


        val PROPAGULE_STAGE: IntPropertyType = IntPropertyType.of("propagule_stage", 0, 4, 0)


        val RAIL_DATA_BIT: BooleanPropertyType = BooleanPropertyType.of("rail_data_bit", false)


        val RAIL_DIRECTION_10: IntPropertyType = IntPropertyType.of("rail_direction", 0, 9, 0)


        val RAIL_DIRECTION_6: IntPropertyType = IntPropertyType.of("rail_direction", 0, 5, 0)


        val REDSTONE_SIGNAL: IntPropertyType = IntPropertyType.of("redstone_signal", 0, 15, 0)


        val REPEATER_DELAY: IntPropertyType = IntPropertyType.of("repeater_delay", 0, 3, 0)


        val RESPAWN_ANCHOR_CHARGE: IntPropertyType = IntPropertyType.of("respawn_anchor_charge", 0, 4, 0)


        val ROTATION: IntPropertyType = IntPropertyType.of("rotation", 0, 3, 0)

        val SAND_TYPE: EnumPropertyType<SandType> = EnumPropertyType.of(
            "sand_type",
            SandType::class.java, SandType.entries[0]
        )


        val SCULK_SENSOR_PHASE: IntPropertyType = IntPropertyType.of("sculk_sensor_phase", 0, 2, 0)


        val SEA_GRASS_TYPE: EnumPropertyType<SeaGrassType> = EnumPropertyType.of(
            "sea_grass_type",
            SeaGrassType::class.java, SeaGrassType.entries[0]
        )


        val SPONGE_TYPE: EnumPropertyType<SpongeType> = EnumPropertyType.of(
            "sponge_type",
            SpongeType::class.java, SpongeType.entries[0]
        )


        val STABILITY: IntPropertyType = IntPropertyType.of("stability", 0, 7, 0)


        val STABILITY_CHECK: BooleanPropertyType = BooleanPropertyType.of("stability_check", false)

        val STONE_BRICK_TYPE: EnumPropertyType<StoneBrickType> = EnumPropertyType.of(
            "stone_brick_type",
            StoneBrickType::class.java, StoneBrickType.entries[0]
        )

        val STONE_SLAB_TYPE: EnumPropertyType<StoneSlabType> = EnumPropertyType.of(
            "stone_slab_type",
            StoneSlabType::class.java, StoneSlabType.entries[0]
        )

        val STONE_SLAB_TYPE_2: EnumPropertyType<StoneSlabType2> = EnumPropertyType.of(
            "stone_slab_type_2",
            StoneSlabType2::class.java, StoneSlabType2.entries[0]
        )

        val STONE_SLAB_TYPE_3: EnumPropertyType<StoneSlabType3> = EnumPropertyType.of(
            "stone_slab_type_3",
            StoneSlabType3::class.java, StoneSlabType3.entries[0]
        )

        val STONE_SLAB_TYPE_4: EnumPropertyType<StoneSlabType4> = EnumPropertyType.of(
            "stone_slab_type_4",
            StoneSlabType4::class.java, StoneSlabType4.entries[0]
        )

        val STRIPPED_BIT: BooleanPropertyType = BooleanPropertyType.of("stripped_bit", false)


        val STRUCTURE_BLOCK_TYPE: EnumPropertyType<StructureBlockType> = EnumPropertyType.of(
            "structure_block_type",
            StructureBlockType::class.java, StructureBlockType.entries[0]
        )

        val STRUCTURE_VOID_TYPE: EnumPropertyType<StructureVoidType> = EnumPropertyType.of(
            "structure_void_type",
            StructureVoidType::class.java, StructureVoidType.entries[0]
        )


        val SUSPENDED_BIT: BooleanPropertyType = BooleanPropertyType.of("suspended_bit", false)

        val TALL_GRASS_TYPE: EnumPropertyType<TallGrassType> = EnumPropertyType.of(
            "tall_grass_type",
            TallGrassType::class.java, TallGrassType.entries[0]
        )


        val TOGGLE_BIT: BooleanPropertyType = BooleanPropertyType.of("toggle_bit", false)


        val TORCH_FACING_DIRECTION: EnumPropertyType<TorchFacingDirection> = EnumPropertyType.of(
            "torch_facing_direction",
            TorchFacingDirection::class.java, TorchFacingDirection.entries[0]
        )


        val TRIGGERED_BIT: BooleanPropertyType = BooleanPropertyType.of("triggered_bit", false)


        val TURTLE_EGG_COUNT: EnumPropertyType<TurtleEggCount> = EnumPropertyType.of(
            "turtle_egg_count",
            TurtleEggCount::class.java, TurtleEggCount.entries[0]
        )


        val TWISTING_VINES_AGE: IntPropertyType = IntPropertyType.of("twisting_vines_age", 0, 25, 0)


        val UPDATE_BIT: BooleanPropertyType = BooleanPropertyType.of("update_bit", false)


        val UPPER_BLOCK_BIT: BooleanPropertyType = BooleanPropertyType.of("upper_block_bit", false)


        val UPSIDE_DOWN_BIT: BooleanPropertyType = BooleanPropertyType.of("upside_down_bit", false)


        val VINE_DIRECTION_BITS: IntPropertyType = IntPropertyType.of("vine_direction_bits", 0, 15, 0)


        val WALL_BLOCK_TYPE: EnumPropertyType<WallBlockType> = EnumPropertyType.of(
            "wall_block_type",
            WallBlockType::class.java, WallBlockType.entries[0]
        )


        val WALL_CONNECTION_TYPE_EAST: EnumPropertyType<WallConnectionType> = EnumPropertyType.of(
            "wall_connection_type_east",
            WallConnectionType::class.java, WallConnectionType.entries[0]
        )


        val WALL_CONNECTION_TYPE_NORTH: EnumPropertyType<WallConnectionType> = EnumPropertyType.of(
            "wall_connection_type_north",
            WallConnectionType::class.java, WallConnectionType.entries[0]
        )


        val WALL_CONNECTION_TYPE_SOUTH: EnumPropertyType<WallConnectionType> = EnumPropertyType.of(
            "wall_connection_type_south",
            WallConnectionType::class.java, WallConnectionType.entries[0]
        )


        val WALL_CONNECTION_TYPE_WEST: EnumPropertyType<WallConnectionType> = EnumPropertyType.of(
            "wall_connection_type_west",
            WallConnectionType::class.java, WallConnectionType.entries[0]
        )


        val PALE_MOSS_CARPET_SIDE_EAST: EnumPropertyType<PaleMossCarpetSide> = EnumPropertyType.of(
            "pale_moss_carpet_side_east",
            PaleMossCarpetSide::class.java, PaleMossCarpetSide.entries[0]
        )


        val PALE_MOSS_CARPET_SIDE_NORTH: EnumPropertyType<PaleMossCarpetSide> = EnumPropertyType.of(
            "pale_moss_carpet_side_north",
            PaleMossCarpetSide::class.java, PaleMossCarpetSide.entries[0]
        )


        val PALE_MOSS_CARPET_SIDE_SOUTH: EnumPropertyType<PaleMossCarpetSide> = EnumPropertyType.of(
            "pale_moss_carpet_side_south",
            PaleMossCarpetSide::class.java, PaleMossCarpetSide.entries[0]
        )


        val PALE_MOSS_CARPET_SIDE_WEST: EnumPropertyType<PaleMossCarpetSide> = EnumPropertyType.of(
            "pale_moss_carpet_side_west",
            PaleMossCarpetSide::class.java, PaleMossCarpetSide.entries[0]
        )


        val TIP: BooleanPropertyType = BooleanPropertyType.of("tip", false)


        val NATURAL: BooleanPropertyType = BooleanPropertyType.of("natural", false)


        val WALL_POST_BIT: BooleanPropertyType = BooleanPropertyType.of("wall_post_bit", false)


        val WEEPING_VINES_AGE: IntPropertyType = IntPropertyType.of("weeping_vines_age", 0, 25, 0)


        val WEIRDO_DIRECTION: IntPropertyType = IntPropertyType.of("weirdo_direction", 0, 3, 0)

        val WOOD_TYPE: EnumPropertyType<WoodType> = EnumPropertyType.of(
            "wood_type",
            WoodType::class.java, WoodType.entries[0]
        )


        val TRIAL_SPAWNER_STATE: IntPropertyType = IntPropertyType.of("trial_spawner_state", 0, 5, 0)


        val VAULT_STATE: EnumPropertyType<VaultState> = EnumPropertyType.of(
            "vault_state",
            VaultState::class.java, VaultState.entries[0]
        )


        val OMINOUS: BooleanPropertyType = BooleanPropertyType.of("ominous", false)

        val REHYDRATION_LEVEL: IntPropertyType = IntPropertyType.of("rehydration_level", 0, 3, 0)

        val VALUES: List<BlockPropertyType<*>> = listOf<BlockPropertyType<*>>(
            OMINOUS,
            VAULT_STATE,
            TRIAL_SPAWNER_STATE,
            ACTIVE,
            AGE_16,
            AGE_3,
            AGE_4,
            AGE_6,
            AGE_BIT,
            ALLOW_UNDERWATER_BIT,
            ATTACHED_BIT,
            ATTACHMENT,
            BAMBOO_LEAF_SIZE,
            BAMBOO_STALK_THICKNESS,
            BIG_DRIPLEAF_HEAD,
            BIG_DRIPLEAF_TILT,
            BITE_COUNTER,
            BLOCK_LIGHT_LEVEL,
            BLOOM,
            BOOKS_STORED,
            BREWING_STAND_SLOT_A_BIT,
            BREWING_STAND_SLOT_B_BIT,
            BREWING_STAND_SLOT_C_BIT,
            BRUSHED_PROGRESS,
            BUTTON_PRESSED_BIT,
            CAN_SUMMON,
            CANDLES,
            CAULDRON_LIQUID,
            CHEMISTRY_TABLE_TYPE,
            CHISEL_TYPE,
            CLUSTER_COUNT,
            COLOR,
            COLOR_BIT,
            COMPOSTER_FILL_LEVEL,
            CONDITIONAL_BIT,
            CORAL_DIRECTION,
            CORAL_FAN_DIRECTION,
            CORAL_HANG_TYPE_BIT,
            COVERED_BIT,
            CRACKED_STATE,
            CRAFTING,
            DAMAGE,
            DEAD_BIT,
            DEPRECATED,
            DIRECTION,
            DIRT_TYPE,
            DISARMED_BIT,
            DOOR_HINGE_BIT,
            DOUBLE_PLANT_TYPE,
            DRAG_DOWN,
            DRIPSTONE_THICKNESS,
            END_PORTAL_EYE_BIT,
            EXPLODE_BIT,
            EXTINGUISHED,
            FACING_DIRECTION,
            FILL_LEVEL,
            GROUND_SIGN_DIRECTION,
            GROWING_PLANT_AGE,
            GROWTH,
            HANGING,
            HEAD_PIECE_BIT,
            HEIGHT,
            HONEY_LEVEL,
            HUGE_MUSHROOM_BITS,
            IN_WALL_BIT,
            INFINIBURN_BIT,
            ITEM_FRAME_MAP_BIT,
            ITEM_FRAME_PHOTO_BIT,
            KELP_AGE,
            LEVER_DIRECTION,
            LIQUID_DEPTH,
            LIT,
            MINECRAFT_BLOCK_FACE,
            MINECRAFT_CARDINAL_DIRECTION,
            MINECRAFT_FACING_DIRECTION,
            MINECRAFT_VERTICAL_HALF,
            MOISTURIZED_AMOUNT,
            MONSTER_EGG_STONE_TYPE,
            MULTI_FACE_DIRECTION_BITS,
            NEW_LEAF_TYPE,
            OCCUPIED_BIT,
            OLD_LEAF_TYPE,
            OPEN_BIT,
            ORIENTATION,
            OUTPUT_LIT_BIT,
            OUTPUT_SUBTRACT_BIT,
            PERSISTENT_BIT,
            PILLAR_AXIS,
            PORTAL_AXIS,
            POWERED_BIT,
            PRISMARINE_BLOCK_TYPE,
            PROPAGULE_STAGE,
            RAIL_DATA_BIT,
            RAIL_DIRECTION_10,
            RAIL_DIRECTION_6,
            REDSTONE_SIGNAL,
            REPEATER_DELAY,
            RESPAWN_ANCHOR_CHARGE,
            ROTATION,
            SAND_TYPE,
            SCULK_SENSOR_PHASE,
            SEA_GRASS_TYPE,
            SPONGE_TYPE,
            STABILITY,
            STABILITY_CHECK,
            STONE_BRICK_TYPE,
            STONE_SLAB_TYPE,
            STONE_SLAB_TYPE_2,
            STONE_SLAB_TYPE_3,
            STONE_SLAB_TYPE_4,
            STRIPPED_BIT,
            STRUCTURE_BLOCK_TYPE,
            STRUCTURE_VOID_TYPE,
            SUSPENDED_BIT,
            TALL_GRASS_TYPE,
            TOGGLE_BIT,
            TORCH_FACING_DIRECTION,
            TRIGGERED_BIT,
            TURTLE_EGG_COUNT,
            TWISTING_VINES_AGE,
            UPDATE_BIT,
            UPPER_BLOCK_BIT,
            UPSIDE_DOWN_BIT,
            VINE_DIRECTION_BITS,
            WALL_BLOCK_TYPE,
            WALL_CONNECTION_TYPE_EAST,
            WALL_CONNECTION_TYPE_NORTH,
            WALL_CONNECTION_TYPE_SOUTH,
            WALL_CONNECTION_TYPE_WEST,
            WALL_POST_BIT,
            WEEPING_VINES_AGE,
            WEIRDO_DIRECTION,
            WOOD_TYPE,
            CORAL_COLOR
        )
    }
}
