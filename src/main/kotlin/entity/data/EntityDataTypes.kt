package org.chorus_oss.chorus.entity.data

import org.chorus_oss.chorus.block.BlockAir
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.*

object EntityDataTypes {
    val FLAGS: EntityDataType<EnumSet<EntityFlag>> = object : EntityDataType<EnumSet<EntityFlag>>(
        EnumSet.noneOf(
            EntityFlag::class.java
        ), "FLAGS", 0, Transformers.FLAGS
    ) {
        override fun isInstance(value: Any?): Boolean {
            return value is EnumSet<*> && (value.isEmpty() || (value as Set<*>).iterator().next() is EntityFlag)
        }
    }
    val STRUCTURAL_INTEGRITY: EntityDataType<Int> = EntityDataType(0, "STRUCTURAL_INTEGRITY", 1)
    val VARIANT: EntityDataType<Int> = EntityDataType(0, "VARIANT", 2)
    val COLOR: EntityDataType<Byte> = EntityDataType(0.toByte(), "COLOR", 3)

    val NAME: EntityDataType<String> = EntityDataType("", "NAME", 4)

    /**
     * Unique ID of the entity that owns or created this entity.
     */
    val OWNER_EID: EntityDataType<Long> = EntityDataType(0L, "OWNER_EID", 5)
    val TARGET_EID: EntityDataType<Long> = EntityDataType(0L, "TARGET_EID", 6)

    val AIR_SUPPLY: EntityDataType<Short> = EntityDataType(0.toShort(), "AIR_SUPPLY", 7)
    val EFFECT_COLOR: EntityDataType<Int> = EntityDataType(0, "EFFECT_COLOR", 8)
    val EFFECT_AMBIENCE: EntityDataType<Byte> = EntityDataType(0.toByte(), "EFFECT_AMBIENCE", 9)
    val JUMP_DURATION: EntityDataType<Byte> = EntityDataType(0.toByte(), "JUMP_DURATION", 10)
    val HURT_TICKS: EntityDataType<Int> = EntityDataType(0, "HURT_TICKS", 11)
    val HURT_DIRECTION: EntityDataType<Int> = EntityDataType(0, "HURT_DIRECTION", 12)
    val ROW_TIME_LEFT: EntityDataType<Float> = EntityDataType(0f, "ROW_TIME_LEFT", 13)
    val ROW_TIME_RIGHT: EntityDataType<Float> = EntityDataType(0f, "ROW_TIME_RIGHT", 14)
    val VALUE: EntityDataType<Int> = EntityDataType(0, "VALUE", 15)

    // Same ID shares three different types -facepalm-
    val HORSE_FLAGS: EntityDataType<Int> = EntityDataType(0, "HORSE_FLAGS", 16) //int (id | (data << 16))
    val DISPLAY_OFFSET: EntityDataType<Int> = EntityDataType(0, "DISPLAY_OFFSET", 17)
    val CUSTOM_DISPLAY: EntityDataType<Byte> = EntityDataType(0.toByte(), "CUSTOM_DISPLAY", 18)
    val HORSE_TYPE: EntityDataType<Byte> = EntityDataType(0.toByte(), "HORSE_TYPE", 19)
    val OLD_SWELL: EntityDataType<Int> = EntityDataType(0, "OLD_SWELL", 20)
    val SWELL_DIRECTION: EntityDataType<Int> = EntityDataType(0, "SWELL_DIRECTION", 21)
    val CHARGE_AMOUNT: EntityDataType<Byte> = EntityDataType(0.toByte(), "CHARGE_AMOUNT", 22)
    val CARRY_BLOCK_STATE: EntityDataType<BlockState> = EntityDataType(
        BlockAir.STATE, "CARRY_BLOCK_STATE", 23, Transformers.BLOCK
    )
    val CLIENT_EVENT: EntityDataType<Byte> = EntityDataType(0.toByte(), "CLIENT_EVENT", 24)
    val USING_ITEM: EntityDataType<Boolean> = EntityDataType(
        false, "USING_ITEM", 25, Transformers.BOOLEAN_TO_BYTE
    )
    val PLAYER_FLAGS: EntityDataType<Byte> = EntityDataType(0.toByte(), "PLAYER_FLAGS", 26)
    val PLAYER_INDEX: EntityDataType<Int> = EntityDataType(0, "PLAYER_INDEX", 27)

    val BED_POSITION: EntityDataType<BlockVector3> = EntityDataType(BlockVector3(), "BED_POSITION", 28)

    /**
     * Power of fireball (x-axis)
     */
    val FIREBALL_POWER_X: EntityDataType<Float> = EntityDataType(0f, "FIREBALL_POWER_X", 29)

    /**
     * Power of fireball (y-axis)
     */
    val FIREBALL_POWER_Y: EntityDataType<Float> = EntityDataType(0f, "FIREBALL_POWER_Y", 30)

    /**
     * Power of fireball (z-axis)
     */
    val FIREBALL_POWER_Z: EntityDataType<Float> = EntityDataType(0f, "FIREBALL_POWER_Z", 31)

    /**
     * Potion aux value used for an Arrow's trail. (Equal to the potion ID - 1)
     */
    val AUX_POWER: EntityDataType<Byte> = EntityDataType(0.toByte(), "AUX_POWER", 32)
    val FISH_X: EntityDataType<Float> = EntityDataType(0f, "FISH_X", 33)
    val FISH_Z: EntityDataType<Float> = EntityDataType(0f, "FISH_Z", 34)
    val FISH_ANGLE: EntityDataType<Float> = EntityDataType(0f, "FISH_ANGLE", 35)
    val AUX_VALUE_DATA: EntityDataType<Short> = EntityDataType(0.toShort(), "AUX_VALUE_DATA", 36)

    /**
     * Unique ID for the entity who holds a leash to the current entity.
     */
    val LEASH_HOLDER: EntityDataType<Long> = EntityDataType(0L, "LEASH_HOLDER", 37)

    /**
     * Set the scale of this entity.
     * 1 is the default size defined by `EntityDataType#WIDTH` and `EntityDataType#HEIGHT`.
     */
    val SCALE: EntityDataType<Float> = EntityDataType(0f, "SCALE", 38)

    val HAS_NPC: EntityDataType<Boolean> = EntityDataType(
        false, "HAS_NPC", 39, Transformers.BOOLEAN_TO_BYTE
    )

    val NPC_DATA: EntityDataType<String> = EntityDataType("", "NPC_DATA", 40)

    val ACTIONS: EntityDataType<String> = EntityDataType("", "ACTIONS", 41)

    val AIR_SUPPLY_MAX: EntityDataType<Short> = EntityDataType(0.toShort(), "AIR_SUPPLY_MAX", 42)
    val MARK_VARIANT: EntityDataType<Int> = EntityDataType(0, "MARK_VARIANT", 43)
    val CONTAINER_TYPE: EntityDataType<Byte> = EntityDataType(0.toByte(), "CONTAINER_TYPE", 44)
    val CONTAINER_SIZE: EntityDataType<Int> = EntityDataType(0, "CONTAINER_SIZE", 45)
    val CONTAINER_STRENGTH_MODIFIER: EntityDataType<Int> = EntityDataType(0, "CONTAINER_STRENGTH_MODIFIER", 46)

    /**
     * Target position of Ender Crystal beam.
     */
    val BLOCK_TARGET_POS: EntityDataType<BlockVector3> = EntityDataType(BlockVector3(), "BLOCK_TARGET_POS", 47)
    val WITHER_INVULNERABLE_TICKS: EntityDataType<Int> = EntityDataType(0, "WITHER_INVULNERABLE_TICKS", 48)

    /**
     * Unique entity ID to target for the left head of a Wither.
     */
    val WITHER_TARGET_A: EntityDataType<Long> = EntityDataType(0L, "WITHER_TARGET_A", 49)

    /**
     * Unique entity ID to target for the middle head of a Wither.
     */
    val WITHER_TARGET_B: EntityDataType<Long> = EntityDataType(0L, "WITHER_TARGET_B", 50)

    /**
     * Unique entity ID to target for the right head of a Wither.
     */
    val WITHER_TARGET_C: EntityDataType<Long> = EntityDataType(0L, "WITHER_TARGET_C", 51)
    val WITHER_AERIAL_ATTACK: EntityDataType<Short> = EntityDataType(0.toShort(), "WITHER_AERIAL_ATTACK", 52)

    val WIDTH: EntityDataType<Float> = EntityDataType(0f, "WIDTH", 53)

    val HEIGHT: EntityDataType<Float> = EntityDataType(0f, "HEIGHT", 54)
    val FUSE_TIME: EntityDataType<Int> = EntityDataType(0, "FUSE_TIME", 55)
    val SEAT_OFFSET: EntityDataType<Vector3f> = EntityDataType(Vector3f(), "SEAT_OFFSET", 56)
    val SEAT_LOCK_RIDER_ROTATION: EntityDataType<Boolean> = EntityDataType(
        false, "SEAT_LOCK_RIDER_ROTATION", 57, Transformers.BOOLEAN_TO_BYTE
    )
    val SEAT_LOCK_RIDER_ROTATION_DEGREES: EntityDataType<Float> =
        EntityDataType(0f, "SEAT_LOCK_RIDER_ROTATION_DEGREES", 58)
    val SEAT_HAS_ROTATION: EntityDataType<Boolean> = EntityDataType(
        false, "SEAT_HAS_ROTATION", 59, Transformers.BOOLEAN_TO_BYTE
    )
    val SEAT_ROTATION_OFFSET_DEGREES: EntityDataType<Float> = EntityDataType(0f, "SEAT_ROTATION_OFFSET_DEGREES", 60)

    /**
     * Radius of Area Effect Cloud
     */
    val AREA_EFFECT_CLOUD_RADIUS: EntityDataType<Float> = EntityDataType(0f, "AREA_EFFECT_CLOUD_RADIUS", 61)
    val AREA_EFFECT_CLOUD_WAITING: EntityDataType<Int> = EntityDataType(0, "AREA_EFFECT_CLOUD_WAITING", 62)
    val AREA_EFFECT_CLOUD_PARTICLE: EntityDataType<Int> = EntityDataType(0, "AREA_EFFECT_CLOUD_PARTICLE", 63)
    val SHULKER_PEEK_AMOUNT: EntityDataType<Int> = EntityDataType(0, "SHULKER_PEEK_AMOUNT", 64)
    val SHULKER_ATTACH_FACE: EntityDataType<Int> = EntityDataType(0, "SHULKER_ATTACH_FACE", 65)
    val SHULKER_ATTACHED: EntityDataType<Boolean> = EntityDataType(
        false, "SHULKER_ATTACHED", 66, Transformers.BOOLEAN_TO_BYTE
    )

    /**
     * Position a Shulker entity is attached from.
     */
    val SHULKER_ATTACH_POS: EntityDataType<BlockVector3> = EntityDataType(BlockVector3(), "SHULKER_ATTACH_POS", 67)

    /**
     * Sets the unique ID of the player that is trading with this entity.
     */
    val TRADE_TARGET_EID: EntityDataType<Long> = EntityDataType(0L, "TRADE_TARGET_EID", 68)

    /**
     * Previously used for the villager V1 entity.
     */
    val CAREER: EntityDataType<Int> = EntityDataType(0, "CAREER", 69)
    val COMMAND_BLOCK_ENABLED: EntityDataType<Boolean> = EntityDataType(
        false, "COMMAND_BLOCK_ENABLED", 70, Transformers.BOOLEAN_TO_BYTE
    )
    val COMMAND_BLOCK_NAME: EntityDataType<String> = EntityDataType("", "COMMAND_BLOCK_NAME", 71)
    val COMMAND_BLOCK_LAST_OUTPUT: EntityDataType<String> = EntityDataType("", "COMMAND_BLOCK_LAST_OUTPUT", 72)
    val COMMAND_BLOCK_TRACK_OUTPUT: EntityDataType<Boolean> = EntityDataType(
        false, "COMMAND_BLOCK_TRACK_OUTPUT", 73, Transformers.BOOLEAN_TO_BYTE
    )
    val CONTROLLING_RIDER_SEAT_INDEX: EntityDataType<Byte> =
        EntityDataType(0.toByte(), "CONTROLLING_RIDER_SEAT_INDEX", 74)
    val STRENGTH: EntityDataType<Int> = EntityDataType(0, "STRENGTH", 75)
    val STRENGTH_MAX: EntityDataType<Int> = EntityDataType(0, "STRENGTH_MAX", 76)
    val EVOKER_SPELL_CASTING_COLOR: EntityDataType<Int> = EntityDataType(0, "EVOKER_SPELL_CASTING_COLOR", 77)
    val DATA_LIFETIME_TICKS: EntityDataType<Int> = EntityDataType(0, "DATA_LIFETIME_TICKS", 78)
    val ARMOR_STAND_POSE_INDEX: EntityDataType<Int> = EntityDataType(0, "ARMOR_STAND_POSE_INDEX", 79)
    val END_CRYSTAL_TICK_OFFSET: EntityDataType<Int> = EntityDataType(0, "END_CRYSTAL_TICK_OFFSET", 80)

    val NAMETAG_ALWAYS_SHOW: EntityDataType<Byte> = EntityDataType(0.toByte(), "NAMETAG_ALWAYS_SHOW", 81)
    val COLOR_2: EntityDataType<Byte> = EntityDataType(0.toByte(), "COLOR_2", 82)
    val NAME_AUTHOR: EntityDataType<String> = EntityDataType("", "NAME_AUTHOR", 83)

    val SCORE: EntityDataType<String> = EntityDataType("", "SCORE", 84)

    /**
     * Unique entity ID that the balloon string is attached to.
     * Disable by setting value to -1.
     */
    val BALLOON_ANCHOR_EID: EntityDataType<Long> = EntityDataType(0L, "BALLOON_ANCHOR_EID", 85)
    val PUFFED_STATE: EntityDataType<Byte> = EntityDataType(0.toByte(), "PUFFED_STATE", 86)
    val BOAT_BUBBLE_TIME: EntityDataType<Int> = EntityDataType(0, "BOAT_BUBBLE_TIME", 87)

    /**
     * The unique entity ID of the player's Agent. (Education Edition only)
     */
    val AGENT_EID: EntityDataType<Long> = EntityDataType(0L, "AGENT_EID", 88)
    val SITTING_AMOUNT: EntityDataType<Float> = EntityDataType(0f, "SITTING_AMOUNT", 89)
    val SITTING_AMOUNT_PREVIOUS: EntityDataType<Float> = EntityDataType(0f, "SITTING_AMOUNT_PREVIOUS", 90)
    val EATING_COUNTER: EntityDataType<Int> = EntityDataType(0, "EATING_COUNTER", 91)
    val FLAGS_2: EntityDataType<EnumSet<EntityFlag>> = EntityDataType(
        EnumSet.noneOf(
            EntityFlag::class.java
        ), "FLAGS_2", 92, Transformers.FLAGS_EXTEND
    )
    val LAYING_AMOUNT: EntityDataType<Float> = EntityDataType(0f, "LAYING_AMOUNT", 93)
    val LAYING_AMOUNT_PREVIOUS: EntityDataType<Float> = EntityDataType(0f, "LAYING_AMOUNT_PREVIOUS", 94)
    val AREA_EFFECT_CLOUD_DURATION: EntityDataType<Int> = EntityDataType(0, "AREA_EFFECT_CLOUD_DURATION", 95)
    val AREA_EFFECT_CLOUD_SPAWN_TIME: EntityDataType<Int> = EntityDataType(0, "AREA_EFFECT_CLOUD_SPAWN_TIME", 96)
    val AREA_EFFECT_CLOUD_CHANGE_RATE: EntityDataType<Float> = EntityDataType(0f, "AREA_EFFECT_CLOUD_CHANGE_RATE", 97)
    val AREA_EFFECT_CLOUD_CHANGE_ON_PICKUP: EntityDataType<Float> =
        EntityDataType(0f, "AREA_EFFECT_CLOUD_CHANGE_ON_PICKUP", 98)
    val AREA_EFFECT_CLOUD_PICKUP_COUNT: EntityDataType<Int> = EntityDataType(0, "AREA_EFFECT_CLOUD_PICKUP_COUNT", 99)

    val INTERACT_TEXT: EntityDataType<String> = EntityDataType("", "INTERACT_TEXT", 100)
    val TRADE_TIER: EntityDataType<Int> = EntityDataType(0, "TRADE_TIER", 101)
    val MAX_TRADE_TIER: EntityDataType<Int> = EntityDataType(0, "MAX_TRADE_TIER", 102)
    val TRADE_EXPERIENCE: EntityDataType<Int> = EntityDataType(0, "TRADE_EXPERIENCE", 103)
    val SKIN_ID: EntityDataType<Int> = EntityDataType(0, "SKIN_ID", 104)
    val SPAWNING_FRAMES: EntityDataType<Int> = EntityDataType(0, "SPAWNING_FRAMES", 105)
    val COMMAND_BLOCK_TICK_DELAY: EntityDataType<Int> = EntityDataType(0, "COMMAND_BLOCK_TICK_DELAY", 106)
    val COMMAND_BLOCK_EXECUTE_ON_FIRST_TICK: EntityDataType<Boolean> = EntityDataType(
        false, "COMMAND_BLOCK_EXECUTE_ON_FIRST_TICK", 107, Transformers.BOOLEAN_TO_BYTE
    )
    val AMBIENT_SOUND_INTERVAL: EntityDataType<Float> = EntityDataType(0f, "AMBIENT_SOUND_INTERVAL", 108)
    val AMBIENT_SOUND_INTERVAL_RANGE: EntityDataType<Float> = EntityDataType(0f, "AMBIENT_SOUND_INTERVAL_RANGE", 109)
    val AMBIENT_SOUND_EVENT_NAME: EntityDataType<String> = EntityDataType("", "AMBIENT_SOUND_EVENT_NAME", 110)
    val FALL_DAMAGE_MULTIPLIER: EntityDataType<Float> = EntityDataType(0f, "FALL_DAMAGE_MULTIPLIER", 111)
    val NAME_RAW_TEXT: EntityDataType<String> = EntityDataType("", "NAME_RAW_TEXT", 112)
    val CAN_RIDE_TARGET: EntityDataType<Boolean> = EntityDataType(
        false, "CAN_RIDE_TARGET", 113, Transformers.BOOLEAN_TO_BYTE
    )
    val LOW_TIER_CURED_TRADE_DISCOUNT: EntityDataType<Int> = EntityDataType(0, "LOW_TIER_CURED_TRADE_DISCOUNT", 114)
    val HIGH_TIER_CURED_TRADE_DISCOUNT: EntityDataType<Int> = EntityDataType(0, "HIGH_TIER_CURED_TRADE_DISCOUNT", 115)
    val NEARBY_CURED_TRADE_DISCOUNT: EntityDataType<Int> = EntityDataType(0, "NEARBY_CURED_TRADE_DISCOUNT", 116)
    val NEARBY_CURED_DISCOUNT_TIME_STAMP: EntityDataType<Int> =
        EntityDataType(0, "NEARBY_CURED_DISCOUNT_TIME_STAMP", 117)

    /**
     * Set custom hitboxes for an entity. This will override the hitbox defined with [EntityDataTypes.SCALE],
     * [EntityDataTypes.WIDTH] and [EntityDataTypes.HEIGHT], but will not affect the collisions.
     * Setting the hitbox to an empty list will revert to default behaviour.
     *
     *
     * NBT format
     * <pre>
     * {
     * "Hitboxes": [
     * {
     * "MinX": 0f,
     * "MinY": 0f,
     * "MinZ": 0f,
     * "MaxX": 1f,
     * "MaxY": 1f,
     * "MaxZ": 1f,
     * "PivotX": 0f,
     * "PivotY": 0f,
     * "PivotZ": 0f,
     * }
     * ]
     * }
    </pre> *
     */
    val HITBOX: EntityDataType<CompoundTag> = EntityDataType(CompoundTag(), "HITBOX", 118)
    val IS_BUOYANT: EntityDataType<Boolean> = EntityDataType(false, "IS_BUOYANT", 119, Transformers.BOOLEAN_TO_BYTE)
    val FREEZING_EFFECT_STRENGTH: EntityDataType<Float> = EntityDataType(0f, "FREEZING_EFFECT_STRENGTH", 120)
    val BUOYANCY_DATA: EntityDataType<String> = EntityDataType("", "BUOYANCY_DATA", 121)
    val GOAT_HORN_COUNT: EntityDataType<Int> = EntityDataType(0, "GOAT_HORN_COUNT", 122)
    val BASE_RUNTIME_ID: EntityDataType<String> = EntityDataType("", "BASE_RUNTIME_ID", 123)
    val MOVEMENT_SOUND_DISTANCE_OFFSET: EntityDataType<Float> =
        EntityDataType(0f, "MOVEMENT_SOUND_DISTANCE_OFFSET", 124)
    val HEARTBEAT_INTERVAL_TICKS: EntityDataType<Int> = EntityDataType(0, "HEARTBEAT_INTERVAL_TICKS", 125)
    val HEARTBEAT_SOUND_EVENT: EntityDataType<Int> = EntityDataType(0, "HEARTBEAT_SOUND_EVENT", 126)
    val PLAYER_LAST_DEATH_POS: EntityDataType<BlockVector3> =
        EntityDataType(BlockVector3(), "PLAYER_LAST_DEATH_POS", 127)
    val PLAYER_LAST_DEATH_DIMENSION: EntityDataType<Int> = EntityDataType(0, "PLAYER_LAST_DEATH_DIMENSION", 128)
    val PLAYER_HAS_DIED: EntityDataType<Boolean> =
        EntityDataType(false, "PLAYER_HAS_DIED", 129, Transformers.BOOLEAN_TO_BYTE)
    val COLLISION_BOX: EntityDataType<Vector3f> = EntityDataType(Vector3f(), "COLLISION_BOX", 130)
    val VISIBLE_MOB_EFFECTS: EntityDataType<Long> = EntityDataType(0L, "VISIBLE_MOB_EFFECTS", 131)
    val FILTERED_NAME: EntityDataType<String> = EntityDataType("", "FILTERED_NAME", 132)
    val BED_ENTER_POSITION: EntityDataType<Vector3f> = EntityDataType(Vector3f(), "BED_ENTER_POSITION", 133)
    val SEAT_THIRD_PERSON_CAMERA_RADIUS: EntityDataType<Float> =
        EntityDataType(0.0f, "SEAT_THIRD_PERSON_CAMERA_RADIUS", 134)
    val SEAT_CAMERA_RELAX_DISTANCE_SMOOTHING: EntityDataType<Float> =
        EntityDataType(0.0f, "SEAT_CAMERA_RELAX_DISTANCE_SMOOTHING", 135)
}
