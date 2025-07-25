package org.chorus_oss.chorus.registry


import org.chorus_oss.chorus.Chorus
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.custom.CustomEntity
import org.chorus_oss.chorus.entity.item.*
import org.chorus_oss.chorus.entity.mob.*
import org.chorus_oss.chorus.entity.mob.animal.*
import org.chorus_oss.chorus.entity.mob.monster.*
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.*
import org.chorus_oss.chorus.entity.mob.villagers.EntityVillager
import org.chorus_oss.chorus.entity.mob.villagers.EntityVillagerV2
import org.chorus_oss.chorus.entity.mob.water_animal.EntityDolphin
import org.chorus_oss.chorus.entity.mob.water_animal.EntityTadpole
import org.chorus_oss.chorus.entity.mob.water_animal.EntityTropicalfish
import org.chorus_oss.chorus.entity.mob.water_animal.fish.EntityCod
import org.chorus_oss.chorus.entity.mob.water_animal.fish.EntityPufferfish
import org.chorus_oss.chorus.entity.mob.water_animal.fish.EntitySalmon
import org.chorus_oss.chorus.entity.projectile.*
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityArrow
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityThrownTrident
import org.chorus_oss.chorus.entity.projectile.throwable.*
import org.chorus_oss.chorus.entity.weather.EntityLightningBolt
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.NBTIO.read
import org.chorus_oss.chorus.nbt.NBTIO.write
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.plugin.Plugin
import org.chorus_oss.chorus.registry.EntityRegistry.EntityDefinition
import org.chorus_oss.chorus.utils.Loggable
import org.jetbrains.annotations.UnmodifiableView
import java.io.BufferedInputStream
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.primaryConstructor


class EntityRegistry : IRegistry<EntityDefinition, KClass<out Entity>?, KClass<out Entity>> {
    override fun init() {
        if (isLoad.getAndSet(true)) return
        register(
            EntityDefinition(EntityID.CHICKEN, "", 10, hasSpawnegg = true, summonable = true),
            EntityChicken::class
        )
        register(
            EntityDefinition(EntityID.COW, "", 11, hasSpawnegg = true, summonable = true),
            EntityCow::class
        )
        register(
            EntityDefinition(EntityID.PIG, "", 12, hasSpawnegg = true, summonable = true),
            EntityPig::class
        )
        register(
            EntityDefinition(EntityID.SHEEP, "", 13, hasSpawnegg = true, summonable = true),
            EntitySheep::class
        )
        register(
            EntityDefinition(EntityID.WOLF, "", 14, hasSpawnegg = true, summonable = true),
            EntityWolf::class
        )
        register(
            EntityDefinition(EntityID.VILLAGER, "", 15, hasSpawnegg = false, summonable = true),
            EntityVillager::class
        )
        register(
            EntityDefinition(EntityID.MOOSHROOM, "", 16, hasSpawnegg = true, summonable = true),
            EntityMooshroom::class
        )
        register(
            EntityDefinition(EntityID.SQUID, "", 17, hasSpawnegg = true, summonable = true),
            EntitySquid::class
        )
        register(
            EntityDefinition(EntityID.RABBIT, "", 18, hasSpawnegg = true, summonable = true),
            EntityRabbit::class
        )
        register(
            EntityDefinition(EntityID.BAT, "", 19, hasSpawnegg = true, summonable = true),
            EntityBat::class
        )
        register(
            EntityDefinition(EntityID.IRON_GOLEM, "", 20, hasSpawnegg = true, summonable = true),
            EntityIronGolem::class
        )
        register(
            EntityDefinition(EntityID.SNOW_GOLEM, "", 21, hasSpawnegg = true, summonable = true),
            EntitySnowGolem::class
        )
        register(
            EntityDefinition(EntityID.OCELOT, "", 22, hasSpawnegg = true, summonable = true),
            EntityOcelot::class
        )
        register(
            EntityDefinition(EntityID.HORSE, "", 23, hasSpawnegg = true, summonable = true),
            EntityHorse::class
        )
        register(
            EntityDefinition(EntityID.DONKEY, "", 24, hasSpawnegg = true, summonable = true),
            EntityDonkey::class
        )
        register(
            EntityDefinition(EntityID.MULE, "", 25, hasSpawnegg = true, summonable = true),
            EntityMule::class
        )
        register(
            EntityDefinition(EntityID.SKELETON_HORSE, "", 26, hasSpawnegg = true, summonable = true),
            EntitySkeletonHorse::class
        )
        register(
            EntityDefinition(EntityID.ZOMBIE_HORSE, "", 27, hasSpawnegg = true, summonable = true),
            EntityZombieHorse::class
        )
        register(
            EntityDefinition(EntityID.POLAR_BEAR, "", 28, hasSpawnegg = true, summonable = true),
            EntityPolarBear::class
        )
        register(
            EntityDefinition(EntityID.LLAMA, "", 29, hasSpawnegg = true, summonable = true),
            EntityLlama::class
        )
        register(
            EntityDefinition(EntityID.PARROT, "", 30, hasSpawnegg = true, summonable = true),
            EntityParrot::class
        )
        register(
            EntityDefinition(EntityID.DOLPHIN, "", 31, hasSpawnegg = true, summonable = true),
            EntityDolphin::class
        )
        register(
            EntityDefinition(EntityID.ZOMBIE, "", 32, hasSpawnegg = true, summonable = true),
            EntityZombie::class
        )
        register(
            EntityDefinition(EntityID.CREEPER, "", 33, hasSpawnegg = true, summonable = true),
            EntityCreeper::class
        )
        register(
            EntityDefinition(EntityID.SKELETON, "", 34, hasSpawnegg = true, summonable = true),
            EntitySkeleton::class
        )
        register(
            EntityDefinition(EntityID.SPIDER, "", 35, hasSpawnegg = true, summonable = true),
            EntitySpider::class
        )
        register(
            EntityDefinition(EntityID.ZOMBIE_PIGMAN, "", 36, hasSpawnegg = true, summonable = true),
            EntityZombiePigman::class
        )
        register(
            EntityDefinition(EntityID.SLIME, "", 37, hasSpawnegg = true, summonable = true),
            EntitySlime::class
        )
        register(
            EntityDefinition(EntityID.ENDERMAN, "", 38, hasSpawnegg = true, summonable = true),
            EntityEnderman::class
        )
        register(
            EntityDefinition(EntityID.SILVERFISH, "", 39, hasSpawnegg = true, summonable = true),
            EntitySilverfish::class
        )
        register(
            EntityDefinition(EntityID.CAVE_SPIDER, "", 40, hasSpawnegg = true, summonable = true),
            EntityCaveSpider::class
        )
        register(
            EntityDefinition(EntityID.GHAST, "", 41, hasSpawnegg = true, summonable = true),
            EntityGhast::class
        )
        register(
            EntityDefinition(EntityID.MAGMA_CUBE, "", 42, hasSpawnegg = true, summonable = true),
            EntityMagmaCube::class
        )
        register(
            EntityDefinition(EntityID.BLAZE, "", 43, hasSpawnegg = true, summonable = true),
            EntityBlaze::class
        )
        register(
            EntityDefinition(EntityID.ZOMBIE_VILLAGER, "", 44, hasSpawnegg = false, summonable = true),
            EntityZombieVillager::class
        )
        register(
            EntityDefinition(EntityID.WITCH, "", 45, hasSpawnegg = true, summonable = true),
            EntityWitch::class
        )
        register(
            EntityDefinition(EntityID.STRAY, "", 46, hasSpawnegg = true, summonable = true),
            EntityStray::class
        )
        register(
            EntityDefinition(EntityID.HUSK, "", 47, hasSpawnegg = true, summonable = true),
            EntityHusk::class
        )
        register(
            EntityDefinition(EntityID.WITHER_SKELETON, "", 48, hasSpawnegg = true, summonable = true),
            EntityWitherSkeleton::class
        )
        register(
            EntityDefinition(EntityID.GUARDIAN, "", 49, hasSpawnegg = true, summonable = true),
            EntityGuardian::class
        )
        register(
            EntityDefinition(EntityID.ELDER_GUARDIAN, "", 50, hasSpawnegg = true, summonable = true),
            EntityElderGuardian::class
        )
        register(
            EntityDefinition(EntityID.NPC, "", 51, hasSpawnegg = true, summonable = true),
            EntityNPC::class
        )
        register(
            EntityDefinition(EntityID.WITHER, "", 52, hasSpawnegg = true, summonable = true),
            EntityWither::class
        )
        register(
            EntityDefinition(EntityID.ENDER_DRAGON, "", 53, hasSpawnegg = true, summonable = true),
            EntityEnderDragon::class
        )
        register(
            EntityDefinition(EntityID.SHULKER, "", 54, hasSpawnegg = true, summonable = true),
            EntityShulker::class
        )
        register(
            EntityDefinition(EntityID.ENDERMITE, "", 55, hasSpawnegg = true, summonable = true),
            EntityEndermite::class
        )
        //        register(new EntityDefinition(AGENT, "", 56, hasSpawnegg = false, summonable = false), EntityAgent.class);
        register(
            EntityDefinition(EntityID.VINDICATOR, "", 57, hasSpawnegg = true, summonable = true),
            EntityVindicator::class
        )
        register(
            EntityDefinition(EntityID.PHANTOM, "", 58, hasSpawnegg = true, summonable = true),
            EntityPhantom::class
        )
        register(
            EntityDefinition(EntityID.RAVAGER, "", 59, hasSpawnegg = true, summonable = true),
            EntityRavager::class
        )
        register(
            EntityDefinition(EntityID.ARMOR_STAND, "", 61, hasSpawnegg = false, summonable = true),
            EntityArmorStand::class
        )
        //        register(new EntityDefinition(TRIPOD_CAMERA, "", 62, hasSpawnegg = false, summonable = false), EntityTripodCamera.class);
        register(
            EntityDefinition(EntityID.ITEM, "", 64, hasSpawnegg = false, summonable = false),
            EntityItem::class
        )
        register(
            EntityDefinition(EntityID.TNT, "", 65, hasSpawnegg = false, summonable = true),
            EntityTnt::class
        )
        register(
            EntityDefinition(EntityID.FALLING_BLOCK, "", 66, hasSpawnegg = false, summonable = false),
            EntityFallingBlock::class
        )
        register(
            EntityDefinition(EntityID.XP_BOTTLE, "", 68, hasSpawnegg = false, summonable = true),
            EntityXpBottle::class
        )
        register(
            EntityDefinition(EntityID.XP_ORB, "", 69, hasSpawnegg = false, summonable = true),
            EntityXpOrb::class
        )
        //        register(new EntityDefinition(EYE_OF_ENDER_SIGNAL, "", 70, hasSpawnegg = false, summonable = false), EntityEyeOfEnderSignal.class);
        register(
            EntityDefinition(EntityID.ENDER_CRYSTAL, "", 71, hasSpawnegg = false, summonable = true),
            EntityEnderCrystal::class
        )
        register(
            EntityDefinition(EntityID.FIREWORKS_ROCKET, "", 72, hasSpawnegg = false, summonable = true),
            EntityFireworksRocket::class
        )
        register(
            EntityDefinition(EntityID.THROWN_TRIDENT, "", 73, hasSpawnegg = false, summonable = false),
            EntityThrownTrident::class
        )
        register(
            EntityDefinition(EntityID.TURTLE, "", 74, hasSpawnegg = true, summonable = true),
            EntityTurtle::class
        )
        register(
            EntityDefinition(EntityID.CAT, "", 75, hasSpawnegg = true, summonable = true),
            EntityCat::class
        )
        register(
            EntityDefinition(EntityID.SHULKER_BULLET, "", 76, hasSpawnegg = false, summonable = false),
            EntityShulkerBullet::class
        )
        register(
            EntityDefinition(EntityID.FISHING_HOOK, "", 77, hasSpawnegg = false, summonable = false),
            EntityFishingHook::class
        )
        register(
            EntityDefinition(EntityID.DRAGON_FIREBALL, "", 79, hasSpawnegg = false, summonable = false),
            EntityDragonFireball::class
        )
        register(
            EntityDefinition(EntityID.ARROW, "", 80, hasSpawnegg = false, summonable = true),
            EntityArrow::class
        )
        register(
            EntityDefinition(EntityID.SNOWBALL, "", 81, hasSpawnegg = false, summonable = true),
            EntitySnowball::class
        )
        register(
            EntityDefinition(EntityID.EGG, "", 82, hasSpawnegg = false, summonable = true),
            EntityEgg::class
        )
        register(
            EntityDefinition(EntityID.PAINTING, "", 83, hasSpawnegg = false, summonable = false),
            EntityPainting::class
        )
        register(
            EntityDefinition(EntityID.MINECART, "", 84, hasSpawnegg = false, summonable = true),
            EntityMinecart::class
        )
        register(
            EntityDefinition(EntityID.FIREBALL, "", 85, hasSpawnegg = false, summonable = false),
            EntityFireball::class
        )
        register(
            EntityDefinition(EntityID.SPLASH_POTION, "", 86, hasSpawnegg = false, summonable = true),
            EntitySplashPotion::class
        )
        register(
            EntityDefinition(EntityID.ENDER_PEARL, "", 87, hasSpawnegg = false, summonable = false),
            EntityEnderPearl::class
        )
        //        register(new EntityDefinition(LEASH_KNOT, "", 88, hasSpawnegg = false, summonable = true), EntityLeashKnot.class);
        register(
            EntityDefinition(EntityID.WITHER_SKULL, "", 89, hasSpawnegg = false, summonable = false),
            EntityWitherSkull::class
        )
        register(
            EntityDefinition(EntityID.BOAT, "", 90, hasSpawnegg = false, summonable = true),
            EntityBoat::class
        )
        register(
            EntityDefinition(EntityID.WITHER_SKULL_DANGEROUS, "", 91, hasSpawnegg = false, summonable = false),
            EntityWitherSkullDangerous::class
        )
        register(
            EntityDefinition(EntityID.LIGHTNING_BOLT, "", 93, hasSpawnegg = false, summonable = true),
            EntityLightningBolt::class
        )
        register(
            EntityDefinition(EntityID.SMALL_FIREBALL, "", 94, hasSpawnegg = false, summonable = false),
            EntitySmallFireball::class
        )
        register(
            EntityDefinition(EntityID.AREA_EFFECT_CLOUD, "", 95, hasSpawnegg = false, summonable = false),
            EntityAreaEffectCloud::class
        )
        register(
            EntityDefinition(EntityID.HOPPER_MINECART, "", 96, hasSpawnegg = false, summonable = true),
            EntityHopperMinecart::class
        )
        register(
            EntityDefinition(EntityID.TNT_MINECART, "", 97, hasSpawnegg = false, summonable = true),
            EntityTntMinecart::class
        )
        register(
            EntityDefinition(EntityID.CHEST_MINECART, "", 98, hasSpawnegg = false, summonable = true),
            EntityChestMinecart::class
        )
        //        register(new EntityDefinition(COMMAND_BLOCK_MINECART, "", 100, hasSpawnegg = false, summonable = true), EntityCommandBlockMinecart.class);
        register(
            EntityDefinition(EntityID.LINGERING_POTION, "", 101, hasSpawnegg = false, summonable = false),
            EntityLingeringPotion::class
        )
        register(
            EntityDefinition(EntityID.LLAMA_SPIT, "", 102, hasSpawnegg = false, summonable = false),
            EntityLlamaSpit::class
        )
        register(
            EntityDefinition(EntityID.EVOCATION_FANG, "", 103, hasSpawnegg = false, summonable = true),
            EntityEvocationFang::class
        )
        register(
            EntityDefinition(EntityID.EVOCATION_ILLAGER, "", 104, hasSpawnegg = true, summonable = true),
            EntityEvocationIllager::class
        )
        register(
            EntityDefinition(EntityID.VEX, "", 105, hasSpawnegg = true, summonable = true),
            EntityVex::class
        )
        //        register(new EntityDefinition(ICE_BOMB, "", 106, hasSpawnegg = false, summonable = false), EntityIceBomb.class);
//        register(new EntityDefinition(BALLOON, "", 107, hasSpawnegg = false, summonable = false), EntityBalloon.class);
        register(
            EntityDefinition(EntityID.PUFFERFISH, "", 108, hasSpawnegg = true, summonable = true),
            EntityPufferfish::class
        )
        register(
            EntityDefinition(EntityID.SALMON, "", 109, hasSpawnegg = true, summonable = true),
            EntitySalmon::class
        )
        register(
            EntityDefinition(EntityID.DROWNED, "", 110, hasSpawnegg = true, summonable = true),
            EntityDrowned::class
        )
        register(
            EntityDefinition(EntityID.TROPICALFISH, "", 111, hasSpawnegg = true, summonable = true),
            EntityTropicalfish::class
        )
        register(
            EntityDefinition(EntityID.COD, "", 112, hasSpawnegg = true, summonable = true),
            EntityCod::class
        )
        register(
            EntityDefinition(EntityID.PANDA, "", 113, hasSpawnegg = true, summonable = true),
            EntityPanda::class
        )
        register(
            EntityDefinition(EntityID.PILLAGER, "", 114, hasSpawnegg = true, summonable = true),
            EntityPillager::class
        )
        register(
            EntityDefinition(EntityID.VILLAGER_V2, "", 115, hasSpawnegg = true, summonable = false),
            EntityVillagerV2::class
        )
        register(
            EntityDefinition(EntityID.ZOMBIE_VILLAGER_V2, "", 116, hasSpawnegg = true, summonable = false),
            EntityZombieVillagerV2::class
        )
        register(
            EntityDefinition(EntityID.WANDERING_TRADER, "", 118, hasSpawnegg = true, summonable = true),
            EntityWanderingTrader::class
        )
        //        register(new EntityDefinition(ELDER_GUARDIAN_GHOST, "", 120, hasSpawnegg = false, summonable = true), EntityElderGuardianGhost.class);
        register(
            EntityDefinition(EntityID.FOX, "", 121, hasSpawnegg = true, summonable = true),
            EntityFox::class
        )
        register(
            EntityDefinition(EntityID.BEE, "", 122, hasSpawnegg = true, summonable = true),
            EntityBee::class
        )
        register(
            EntityDefinition(EntityID.PIGLIN, "", 123, hasSpawnegg = true, summonable = true),
            EntityPiglin::class
        )
        register(
            EntityDefinition(EntityID.HOGLIN, "", 124, hasSpawnegg = true, summonable = true),
            EntityHoglin::class
        )
        register(
            EntityDefinition(EntityID.STRIDER, "", 125, hasSpawnegg = true, summonable = true),
            EntityStrider::class
        )
        register(
            EntityDefinition(EntityID.ZOGLIN, "", 126, hasSpawnegg = true, summonable = true),
            EntityZoglin::class
        )
        register(
            EntityDefinition(EntityID.PIGLIN_BRUTE, "", 127, hasSpawnegg = true, summonable = true),
            EntityPiglinBrute::class
        )
        register(
            EntityDefinition(EntityID.GOAT, "", 128, hasSpawnegg = true, summonable = true),
            EntityGoat::class
        )
        register(
            EntityDefinition(EntityID.GLOW_SQUID, "", 129, hasSpawnegg = true, summonable = true),
            EntityGlowSquid::class
        )
        register(
            EntityDefinition(EntityID.AXOLOTL, "", 130, hasSpawnegg = true, summonable = true),
            EntityAxolotl::class
        )
        register(
            EntityDefinition(EntityID.WARDEN, "", 131, hasSpawnegg = true, summonable = true),
            EntityWarden::class
        )
        register(
            EntityDefinition(EntityID.FROG, "", 132, hasSpawnegg = true, summonable = true),
            EntityFrog::class
        )
        register(
            EntityDefinition(EntityID.TADPOLE, "", 133, hasSpawnegg = true, summonable = true),
            EntityTadpole::class
        )
        register(
            EntityDefinition(EntityID.ALLAY, "", 134, hasSpawnegg = true, summonable = true),
            EntityAllay::class
        )
        register(
            EntityDefinition(EntityID.CAMEL, "", 138, hasSpawnegg = true, summonable = true),
            EntityCamel::class
        )
        register(
            EntityDefinition(EntityID.SNIFFER, "", 139, hasSpawnegg = true, summonable = true),
            EntitySniffer::class
        )
        register(
            EntityDefinition(EntityID.TRADER_LLAMA, "", 157, hasSpawnegg = true, summonable = true),
            EntityTraderLlama::class
        )
        register(
            EntityDefinition(EntityID.CHEST_BOAT, "", 218, hasSpawnegg = false, summonable = true),
            EntityChestBoat::class
        )
        register(
            EntityDefinition(EntityID.ARMADILLO, "", 142, hasSpawnegg = true, summonable = true),
            EntityArmadillo::class
        )
        register(
            EntityDefinition(EntityID.BREEZE, "", 140, hasSpawnegg = true, summonable = true),
            EntityBreeze::class
        )
        register(
            EntityDefinition(EntityID.BREEZE_WIND_CHARGE_PROJECTILE, "", 141, hasSpawnegg = false, summonable = false),
            EntityBreezeWindCharge::class
        )
        register(
            EntityDefinition(EntityID.WIND_CHARGE_PROJECTILE, "", 143, hasSpawnegg = false, summonable = false),
            EntityWindCharge::class
        )
        register(
            EntityDefinition(EntityID.BOGGED, "", 144, hasSpawnegg = true, summonable = true),
            EntityBogged::class
        )
        register(
            EntityDefinition(EntityID.CREAKING, "", 146, hasSpawnegg = true, summonable = true),
            EntityCreaking::class
        )
        register(
            EntityDefinition(EntityID.HAPPY_GHAST, "", 147, hasSpawnegg = true, summonable = true),
            EntityHappyGhast::class
        )

        this.rebuildTag()
    }

    fun getEntityClass(id: String): KClass<out Entity>? {
        return CLASS[id]
    }

    fun getEntityClass(id: Int): KClass<out Entity>? {
        return getEntityClass(RID2ID[id] ?: return null)
    }

    fun getEntityNetworkId(entityID: String): Int {
        return ID2RID[entityID] ?: throw RuntimeException("Unknown EntityID: $entityID")
    }

    fun getEntityIdentifier(networkID: Int): String {
        return RID2ID[networkID] ?: throw RuntimeException("Unknown NetworkID: $networkID")
    }

    fun getEntityDefinition(id: String): EntityDefinition {
        return DEFINITIONS[id] ?: throw RuntimeException("Unknown EntityID: $id")
    }

    val customEntityDefinitions: @UnmodifiableView MutableList<EntityDefinition>
        get() = Collections.unmodifiableList(CUSTOM_ENTITY_DEFINITIONS)

    val knownEntityIds: MutableCollection<Int>
        /**
         * 获得全部实体的网络id
         *
         *
         * Get the network id of all entities
         *
         * @return the known entity ids
         */
        get() = ID2RID.values

    val entityId2NetworkIdMap: Map<Int, String>
        get() = Collections.unmodifiableMap(RID2ID)

    val knownEntities: Map<String, KClass<out Entity>>
        /**
         * 获取全部已经注册的实体，包括自定义实体
         *
         *
         * Get all registered entities, including custom entities
         *
         * @return the known entities
         */
        get() = CLASS

    fun provideEntity(id: String, chunk: IChunk, nbt: CompoundTag, vararg args: Any?): Entity? {
        val exceptions = mutableListOf<Throwable>()

        var entity: Entity? = null
        try {
            val ctor = FAST_NEW[id] ?: throw RegisterException("Cannot find Entity for id: $id")
            entity = ctor.call(chunk, nbt, *args)
        } catch (e: Throwable) {
            exceptions.add(e)
        }

        if (entity == null) {
            val cause: Exception = IllegalArgumentException(
                "Could not create an entity of identifier $id",
                if (exceptions.isNotEmpty()) exceptions[0] else null
            )
            if (exceptions.size > 1) {
                for (i in 1..<exceptions.size) {
                    cause.addSuppressed(exceptions[i])
                }
            }
            log.error("Could not create an entity of type {}", id, cause)
        } else {
            return entity
        }
        return null
    }

    override fun get(key: EntityDefinition): KClass<out Entity>? {
        return CLASS[key.id]
    }

    override fun reload() {
        isLoad.set(false)
        CLASS.clear()
        FAST_NEW.clear()
        ID2RID.clear()
        RID2ID.clear()
        DEFINITIONS.clear()
        CUSTOM_ENTITY_DEFINITIONS.clear()
        init()
    }

    @Throws(RegisterException::class)
    override fun register(key: EntityDefinition, value: KClass<out Entity>) {
        if (CLASS.putIfAbsent(key.id, value) == null) {
            FAST_NEW[key.id] =
                value.primaryConstructor ?: throw RegisterException("Entity must have a primary constructor")

            ID2RID[key.id] = key.rid
            RID2ID[key.rid] = key.id
            DEFINITIONS[key.id] = key
        } else {
            throw RegisterException("This Entity has already been registered with the identifier: " + key.id)
        }
    }

    /**
     * Register an entity to override internal entity.
     *
     * @param plugin   the plugin
     * @param entityId the entity id [EntityID]
     * @param value    the entity class,must extends internal entity
     * @throws RegisterException the register exception
     */
    @Throws(RegisterException::class)
    fun registerOverrideEntity(plugin: Plugin, entityId: String, value: KClass<out Entity>) {
        val key = getEntityDefinition(entityId)
        val entityClass = getEntityClass(entityId)
            ?: throw RegisterException(
                "This entity class does not override because can't find entity class from entityId {}",
                entityId
            )
        if (!entityClass.isSuperclassOf(value)) {
            throw RegisterException(
                "This entity class {} does not override the {} because is not assignable from {}!",
                entityClass.simpleName ?: "",
                value.simpleName ?: "",
                value.simpleName ?: ""
            )
        }
        try {
            FAST_NEW[key.id] = value.constructors.find { ctor ->
                ctor.parameters.map {
                    it.type.classifier
                } == listOf(IChunk::class, CompoundTag::class)
            } ?: throw RegisterException("Entity must have constructor(IChunk, CompoundTag)")
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        }
        ID2RID[key.id] = key.rid
        RID2ID[key.rid] = key.id
        DEFINITIONS[key.id] = key
    }

    /**
     * register custom entity
     */
    @Throws(RegisterException::class)
    fun registerCustomEntity(plugin: Plugin, key: CustomEntityDefinition, value: KClass<out Entity>) {
        if (value.isSubclassOf(CustomEntity::class)) {
            if (CLASS.putIfAbsent(key.id, value) == null) {
                try {
                    FAST_NEW[key.id] = value.constructors.find { ctor ->
                        ctor.parameters.map {
                            it.type.classifier
                        } == listOf(IChunk::class, CompoundTag::class)
                    } ?: throw RegisterException("Entity must have constructor(IChunk, CompoundTag)")
                } catch (e: NoSuchMethodException) {
                    throw RuntimeException(e)
                }
                val rid = RUNTIME_ID.getAndIncrement()
                ID2RID[key.id] = rid
                RID2ID[rid] = key.id
                val entityDefinition = EntityDefinition(key.id, key.bid, rid, key.hasSpawnegg, key.summonable)
                DEFINITIONS[key.id] = entityDefinition
                CUSTOM_ENTITY_DEFINITIONS.add(entityDefinition)
            } else {
                throw RegisterException("This Entity has already been registered with the identifier: " + key.id)
            }
        } else {
            throw RegisterException("This class does not implement the CustomEntity interface and cannot be registered as a custom entity!")
        }
    }

    class CustomEntityDefinition(val id: String, val bid: String, val hasSpawnegg: Boolean, val summonable: Boolean)

    @JvmRecord
    data class EntityDefinition(
        val id: String,
        val bid: String,
        val rid: Int,
        val hasSpawnegg: Boolean,
        val summonable: Boolean
    ) {
        fun toNBT(): CompoundTag {
            return CompoundTag()
                .putString("bid", bid)
                .putBoolean("hasspawnegg", hasSpawnegg)
                .putString("id", id)
                .putInt("rid", rid)
                .putBoolean("summonable", summonable)
        }
    }

    val tag: ByteArray
        get() = TAG!!.clone()

    fun rebuildTag() {
        try {
            Chorus::class.java.module.getResourceAsStream("entity_identifiers.nbt").use { inputStream ->
                if (inputStream == null) {
                    throw AssertionError("Could not find entity_identifiers.nbt")
                }
                val bis = BufferedInputStream(inputStream)
                val nbt = read(bis, ByteOrder.BIG_ENDIAN, true)
                val list = nbt.getList("idlist", CompoundTag::class.java)
                for (customEntityDefinition in Registries.ENTITY.customEntityDefinitions) {
                    list.add(customEntityDefinition.toNBT())
                }
                nbt.putList("idlist", list)
                TAG = write(nbt, ByteOrder.BIG_ENDIAN, true)
            }
        } catch (e: Exception) {
            throw AssertionError("Error whilst loading entity_identifiers.dat", e)
        }
    }

    companion object : Loggable {
        private val CLASS = HashMap<String, KClass<out Entity>>()
        private val FAST_NEW = HashMap<String, KFunction<Entity>>()
        private val ID2RID = HashMap<String, Int>()
        private val RID2ID = HashMap<Int, String>()
        private val DEFINITIONS = HashMap<String, EntityDefinition>()
        private val CUSTOM_ENTITY_DEFINITIONS: MutableList<EntityDefinition> = ArrayList()
        private val isLoad = AtomicBoolean(false)
        private var TAG: ByteArray? = null


        private val RUNTIME_ID = AtomicInteger(10000)
    }
}
