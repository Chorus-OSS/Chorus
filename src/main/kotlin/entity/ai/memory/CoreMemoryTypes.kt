package org.chorus_oss.chorus.entity.ai.memory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockBed
import org.chorus_oss.chorus.block.BlockWoodenDoor
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.ai.memory.codec.BooleanMemoryCodec
import org.chorus_oss.chorus.entity.ai.memory.codec.MemoryCodec
import org.chorus_oss.chorus.entity.ai.memory.codec.NumberMemoryCodec
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityEvocationIllager.Spell
import org.chorus_oss.chorus.entity.mob.villagers.EntityVillagerV2
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3

/**
 * 核心使用到的记忆类型枚举
 *
 *
 * Enumeration of memory types used by the core
 */
object CoreMemoryTypes {
    // region 运动控制器相关
    /**
     * 实体视线目标记忆
     *
     *
     * Entity gaze target memory
     */
    val LOOK_TARGET = NullableMemoryType<Vector3>("minecraft:look_target")

    /**
     * 实体移动目标记忆
     *
     *
     * Entity moving target memory
     */
    val MOVE_TARGET = NullableMemoryType<Vector3>("minecraft:move_target")

    val FORCE_PERCHING = MemoryType("minecraft:force_perching", false)

    val STAY_NEARBY = NullableMemoryType<Vector3>("minecraft:stay_nearby")

    /**
     * 实体移动起点记忆
     *
     *
     * Entity movement starting point memory
     */
    val MOVE_DIRECTION_START = NullableMemoryType<Vector3>("minecraft:move_direction_start")

    /**
     * 实体移动终点记忆
     *
     *
     * Entity movement endpoint memory
     */
    val MOVE_DIRECTION_END = NullableMemoryType<Vector3>("minecraft:move_direction_end")

    /**
     * 实体是否需要更新路径的记忆
     *
     *
     * Whether the entity needs to update the memory of the path
     */
    val SHOULD_UPDATE_MOVE_DIRECTION = MemoryType("minecraft:should_update_move_direction", false)

    /**
     * 实体是否开启pitch
     *
     *
     * Whether pitch is enabled for the entity
     */
    val ENABLE_PITCH = MemoryType("minecraft:enable_pitch", true)

    /**
     * 控制实体是否开启升力控制器的记忆
     */
    val ENABLE_LIFT_FORCE = MemoryType("minecraft:enable_lift_force", true)

    /**
     * 控制实体是否开启下潜控制器的记忆
     */
    val ENABLE_DIVE_FORCE = MemoryType("minecraft:enable_dive_force", true)
    //以下这两个暂时未使用到
    //MemoryType<Boolean> ENABLE_YAW = new MemoryType<>("minecraft:enable_yaw", true);
    //MemoryType<Boolean> ENABLE_HEAD_YAW = new MemoryType<>("minecraft:enable_head_yaw", true);
    // endregion
    /**
     * 实体被攻击产生的攻击事件
     */
    val BE_ATTACKED_EVENT = NullableMemoryType<EntityDamageEvent>("minecraft:be_attacked_event")

    /**
     * 实体上一次被攻击的tick
     */
    val LAST_BE_ATTACKED_TIME = MemoryType("minecraft:last_be_attacked_time", -65536)

    val LAST_ATTACK_TIME = MemoryType("minecraft:last_attack_time", 0)

    val LAST_ATTACK_ENTITY = NullableMemoryType<Entity>("minecraft:last_attack_entity")

    val LAST_HOGLIN_ATTACK_TIME = MemoryType("minecraft:last_hoglin_attack_time", 0)

    val NEXT_SHED = MemoryType("minecraft:next_shed", 0)

    val LAST_MAGIC = MemoryType("minecraft:last_spell", Spell.NONE)

    val LAST_GOSSIP = MemoryType("minecraft:last_gossip", -65536)

    val GOSSIP = NullableMemoryType<MutableMap<String, MutableList<Int>>>("minecraft:gossip")

    val LAST_REFILL_SHIFT = MemoryType("minecraft:last_refill_shift", -1)

    /**
     * 实体的仇恨目标
     */
    val ATTACK_TARGET = NullableMemoryType<Entity>("minecraft:attack_target")

    /**
     * 实体的攻击目标是否被改变,目前仅在warden中使用
     */
    val IS_ATTACK_TARGET_CHANGED = MemoryType("minecraft:is_attack_target_changed", false)

    /**
     * 实体从生成的服务器tick
     */
    val ENTITY_SPAWN_TIME = MemoryType<Int>(
        "minecraft:entity_spawn_time"
    ) { Server.instance.tick }

    /**
     * 目前仅在creeper中使用，控制苦力怕是否应该爆炸
     */
    val SHOULD_EXPLODE = MemoryType("minecraft:should_explode", false)

    /**
     * 目前仅在creeper中使用
     */
    val EXPLODE_CANCELLABLE = MemoryType("minecraft:explode_cancellable", true)

    /**
     * 控制实体是否在繁殖
     */
    val IS_IN_LOVE = MemoryType("minecraft:is_in_love", false)

    /**
     * 上一次繁殖的时间tick
     */
    val LAST_IN_LOVE_TIME = MemoryType("minecraft:last_in_love_time", -65536)

    val WILLING = MemoryType("minecraft:willing", false)

    val PARENT = NullableMemoryType<Entity>("minecraft:parent")

    /**
     * 上一次下蛋的时间
     *
     *
     * 目前仅在Chicken中使用
     */
    val LAST_EGG_SPAWN_TIME = MemoryType<Int>(
        "minecraft:last_egg_spawn_time"
    ) { Server.instance.tick }

    /**
     * 最近符合条件的攻击目标
     *
     *
     * 通常写入在在NearestTargetEntitySensor
     */
    val NEAREST_SUITABLE_ATTACK_TARGET = NullableMemoryType<Entity>("minecraft:nearest_suitable_attack_target")

    /**
     * 最近持有动物要食用的食物的玩家
     */
    val NEAREST_FEEDING_PLAYER = NullableMemoryType<Player>("minecraft:nearest_feeding_player")

    /**
     * 最近的玩家
     */
    val NEAREST_PLAYER = NullableMemoryType<Player>("minecraft:nearest_player")

    val STARING_PLAYER = NullableMemoryType<Player>("minecraft:staring_player")

    /**
     * 玩家上一次攻击的实体
     */
    val ENTITY_ATTACKED_BY_OWNER = NullableMemoryType<Entity>("minecraft:entity_attacked_by_owner")

    /**
     * 上一次攻击玩家的实体
     */
    val ENTITY_ATTACKING_OWNER = NullableMemoryType<Entity>("minecraft:entity_attacking_owner")

    /**
     * 上一次喂养的时间
     */
    val LAST_BE_FEED_TIME = MemoryType("minecraft:last_be_feed_time", -65536)

    /**
     * 上一次喂养的玩家
     */
    val LAST_FEED_PLAYER = NullableMemoryType<Player>("minecraft:last_feeding_player")

    val LAST_ENDER_CRYSTAL_DESTROY = NullableMemoryType<BlockVector3>("minecraft:last_ender_crystal_destroy")

    /**
     * 目前仅在warden中使用
     */
    val ROUTE_UNREACHABLE_TIME = MemoryType("minecraft:route_unreachable_time", 0)

    /**
     * 实体的配偶
     */
    val ENTITY_SPOUSE = NullableMemoryType<Entity>("minecraft:entity_spouse")

    /**
     * 目前仅在warden中使用
     */
    val WARDEN_ANGER_VALUE = NullableMemoryType<MutableMap<Entity, Int>>("minecraft:warden_anger_value")

    /**
     * 最近的骷髅目标
     */
    val NEAREST_SKELETON = NullableMemoryType<Entity>("minecraft:nearest_skeleton")

    val NEAREST_ZOMBIE = NullableMemoryType<Entity>("minecraft:nearest_zombie")

    val NEAREST_ENDERMITE = NullableMemoryType<Entity>("minecraft:nearest_endermite")

    val NEAREST_GOLEM = NullableMemoryType<Entity>("minecraft:nearest_golem")

    val NEAREST_SHARED_ENTITY = NullableMemoryType<Entity>("minecraft:nearest_shared_entity")

    val LOOKING_BLOCK = NullableMemoryType<Class<out Block>>("minecraft:looking_block")

    val LOOKING_ITEM = NullableMemoryType<Class<out Item>>("minecraft:looking_item")

    val OCCUPIED_BED = NullableMemoryType<BlockBed>("minecraft:occupied_bed")

    val NEAREST_BLOCK = NullableMemoryType<Block>("minecraft:nearest_block")

    val NEAREST_BLOCK_2 = NullableMemoryType<Block>("minecraft:nearest_block_2")

    val SITE_BLOCK = NullableMemoryType<Block>("minecraft:site_block")

    val NEAREST_DOOR = NullableMemoryType<BlockWoodenDoor>("minecraft:nearest_door")

    val NEAREST_ITEM = NullableMemoryType<EntityItem>("minecraft:nearest_item")

    val GOSSIP_TARGET = NullableMemoryType<EntityVillagerV2>("minecraft:gossip_target")

    val LAST_ATTACK_CAST = MemoryType("minecraft:last_attack_cast", 0)

    val LAST_ATTACK_SUMMON = MemoryType("minecraft:last_attack_summon", 0)

    val LAST_ATTACK_DASH = MemoryType("minecraft:last_attack_dash", 0)

    val LAST_CONVERSION = MemoryType("minecraft:last_conversion", 0)

    val INVULNERABLE_TICKS = MemoryType("minecraft:invulnerable_ticks", 0)


    /**
     * 实体的主人
     */
    val OWNER = NullableMemoryType<Player>("minecraft:owner")

    // region 可持久化的记忆(会写入NBT)
    /**
     * 代表愤怒状态 和[EntityFlag.ANGRY]绑定
     *
     *
     * 目前仅在wolf中使用
     */
    val IS_ANGRY = MemoryType("minecraft:is_angry", false)
        .withCodec(
            BooleanMemoryCodec("Angry")
                .onInit { data, entity ->
                    entity.setDataFlag(EntityFlag.ANGRY, data)
                }
        )

    /**
     * 代表实体是否坐着的状态 和[EntityFlag.SITTING]绑定
     *
     *
     * 目前仅在wolf中使用
     */
    val IS_SITTING = MemoryType("minecraft:is_sitting", false)
        .withCodec(
            BooleanMemoryCodec("Sitting")
                .onInit { data, entity ->
                    entity.setDataFlag(EntityFlag.SITTING, data)
                }
        )

    /**
     * 代表实体主人 和[EntityFlag.TAMED] [EntityDataTypes.OWNER_EID]绑定
     *
     *
     * 目前仅在wolf中使用
     */
    val OWNER_NAME = NullableMemoryType<String>("minecraft:owner_name")
        .withCodec(
            MemoryCodec<String?>(
                { tag ->
                    if (tag.contains("OwnerName"))
                        tag.getString("OwnerName")
                    else null
                },
                { data, tag ->
                    if (data != null) {
                        tag.putString("OwnerName", data)
                    } else tag.remove("OwnerName")
                }
            ).onInit { data, entity ->
                if (data == null) {
                    entity.setDataProperty(EntityDataTypes.OWNER_EID, 0L)
                    entity.setDataFlag(EntityFlag.TAMED, false)
                } else {
                    entity.setDataFlag(EntityFlag.TAMED, true)
                    val owner = Server.instance.getPlayerExact(data)
                    if (owner != null && owner.isOnline) {
                        entity.setDataProperty(EntityDataTypes.OWNER_EID, owner.getUniqueID())
                    }
                }
            }
        )

    /**
     * 代表骑着某个实体的实体
     */
    val RIDER_NAME = NullableMemoryType<String>("minecraft:rider_name")
        .withCodec(
            MemoryCodec<String?>(
                { tag ->
                    if (tag.contains("RiderName"))
                        tag.getString("RiderName")
                    else null
                },
                { data, tag ->
                    if (data != null) {
                        tag.putString("RiderName", data)
                    } else tag.remove("RiderName")
                }
            ).onInit { data: String?, entity: EntityMob ->
                if (data == null) {
                    entity.setDataFlag(EntityFlag.WASD_CONTROLLED, false)
                } else {
                    entity.setDataFlag(EntityFlag.WASD_CONTROLLED)
                }
            }
        )

    /**
     * 代表实体的变种,和[EntityDataTypes.VARIANT]绑定
     */
    val VARIANT = NullableMemoryType<Int>("minecraft:variant")
        .withCodec(
            NumberMemoryCodec<Int?>("Variant")
                .onInit { data, entity ->
                    if (data != null) {
                        entity.setDataProperty(EntityDataTypes.VARIANT, data)
                    }
                }
        )

    /**
     * 代表实体的次要变种,和[EntityDataTypes.MARK_VARIANT]绑定
     */
    val MARK_VARIANT = NullableMemoryType<Int>("minecraft:mark_variant")
        .withCodec(
            NumberMemoryCodec<Int?>("MarkVariant")
                .onInit { data, entity ->
                    if (data != null) {
                        entity.setDataProperty(EntityDataTypes.MARK_VARIANT, data)
                    }
                }
        )

    /**
     * 代表实体的颜色，和[EntityDataTypes.COLOR]绑定
     * <br></br>
     * 例如狼的项圈
     * <br></br>
     * Wolf collar, Cat collar, Sheep wool, Tropical Fish base color
     */
    val COLOR = NullableMemoryType<Byte>("minecraft:color")
        .withCodec(
            NumberMemoryCodec<Byte?>("Color")
                .onInit { data, entity ->
                    if (data != null) {
                        entity.setDataProperty(EntityDataTypes.COLOR, data)
                    }
                }
        )

    /**
     * 代表实体的颜第二色，和[EntityDataTypes.COLOR_2]绑定
     * <br></br>
     * Tropical Fish secondary color
     */
    val COLOR2 = NullableMemoryType<Byte>("minecraft:color2")
        .withCodec(
            NumberMemoryCodec<Byte?>("Color2")
                .onInit { data, entity ->
                    if (data != null) {
                        entity.setDataProperty(EntityDataTypes.COLOR_2, data)
                    }
                }
        )

    /**
     * 和[EntityFlag.SHEARED]绑定
     *
     *
     * Sheep, Snow Golem
     */
    val IS_SHEARED = MemoryType("minecraft:is_sheared", false)
        .withCodec(
            BooleanMemoryCodec("Sheared")
                .onInit { data, entity ->
                    entity.setDataFlag(
                        EntityFlag.SHEARED,
                        data
                    )
                }
        ) // endregion
}
