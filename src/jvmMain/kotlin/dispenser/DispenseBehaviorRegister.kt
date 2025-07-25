package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.registry.Registries
import java.util.regex.Pattern

object DispenseBehaviorRegister {
    private val behaviors: MutableMap<String, DispenseBehavior> = HashMap()
    private val defaultBehavior: DispenseBehavior = DefaultDispenseBehavior()

    fun registerBehavior(itemIdentifier: String, behavior: DispenseBehavior) {
        behaviors[itemIdentifier] = behavior
    }

    @JvmStatic
    fun getBehavior(identifier: String): DispenseBehavior {
        return behaviors.getOrDefault(identifier, defaultBehavior)
    }

    fun removeDispenseBehavior(identifier: String) {
        behaviors.remove(identifier)
    }

    @JvmStatic
    fun init() {
        registerBehavior(ItemID.SHEARS, ShearsDispenseBehavior())
        registerBehavior(ItemID.BUCKET, BucketDispenseBehavior())
        registerBehavior(ItemID.WATER_BUCKET, BucketDispenseBehavior())
        registerBehavior(ItemID.LAVA_BUCKET, BucketDispenseBehavior())
        registerBehavior(ItemID.BONE_MEAL, DyeDispenseBehavior())
        registerBehavior(ItemID.FIREWORK_ROCKET, FireworksDispenseBehavior())
        registerBehavior(ItemID.FLINT_AND_STEEL, FlintAndSteelDispenseBehavior())

        registerBehavior(ItemID.OAK_BOAT, BoatDispenseBehavior())
        registerBehavior(ItemID.BIRCH_BOAT, BoatDispenseBehavior())
        registerBehavior(ItemID.ACACIA_BOAT, BoatDispenseBehavior())
        registerBehavior(ItemID.DARK_OAK_BOAT, BoatDispenseBehavior())
        registerBehavior(ItemID.JUNGLE_BOAT, BoatDispenseBehavior())
        registerBehavior(ItemID.SPRUCE_BOAT, BoatDispenseBehavior())
        registerBehavior(ItemID.MANGROVE_BOAT, BoatDispenseBehavior())
        registerBehavior(ItemID.CHERRY_BOAT, BoatDispenseBehavior())
        registerBehavior(ItemID.PALE_OAK_BOAT, BoatDispenseBehavior())
        registerBehavior(ItemID.BAMBOO_RAFT, BoatDispenseBehavior())

        registerBehavior(ItemID.OAK_CHEST_BOAT, ChestBoatDispenseBehavior())
        registerBehavior(ItemID.BIRCH_CHEST_BOAT, ChestBoatDispenseBehavior())
        registerBehavior(ItemID.ACACIA_CHEST_BOAT, ChestBoatDispenseBehavior())
        registerBehavior(ItemID.DARK_OAK_CHEST_BOAT, ChestBoatDispenseBehavior())
        registerBehavior(ItemID.JUNGLE_CHEST_BOAT, ChestBoatDispenseBehavior())
        registerBehavior(ItemID.SPRUCE_CHEST_BOAT, ChestBoatDispenseBehavior())
        registerBehavior(ItemID.MANGROVE_CHEST_BOAT, ChestBoatDispenseBehavior())
        registerBehavior(ItemID.CHERRY_CHEST_BOAT, ChestBoatDispenseBehavior())
        registerBehavior(ItemID.PALE_OAK_CHEST_BOAT, ChestBoatDispenseBehavior())
        registerBehavior(ItemID.BAMBOO_CHEST_RAFT, ChestBoatDispenseBehavior())

        registerBehavior(BlockID.UNDYED_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.WHITE_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.ORANGE_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.MAGENTA_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.LIGHT_BLUE_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.YELLOW_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.LIME_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.PINK_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.GRAY_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.LIGHT_GRAY_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.CYAN_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.PURPLE_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.BLUE_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.BROWN_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.GREEN_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.RED_SHULKER_BOX, ShulkerBoxDispenseBehavior())
        registerBehavior(BlockID.BLACK_SHULKER_BOX, ShulkerBoxDispenseBehavior())

        val pattern = Pattern.compile("minecraft:.*_spawn_egg")
        Registries.ITEM.all.stream().filter(pattern.asPredicate()).forEach { id: String ->
            registerBehavior(id, SpawnEggDispenseBehavior())
        }

        registerBehavior(BlockID.TNT, TNTDispenseBehavior())
        registerBehavior(ItemID.ARROW, object : ProjectileDispenseBehavior(EntityID.ARROW) {
            override val motion: Double
                get() = super.motion * 1.5
        })
        //TODO: tipped arrow
        //TODO: spectral arrow
        registerBehavior(ItemID.EGG, ProjectileDispenseBehavior(EntityID.EGG))
        registerBehavior(ItemID.SNOWBALL, ProjectileDispenseBehavior(EntityID.SNOWBALL))
        registerBehavior(ItemID.WIND_CHARGE, ProjectileDispenseBehavior(EntityID.WIND_CHARGE_PROJECTILE))
        registerBehavior(ItemID.FIRE_CHARGE, object : ProjectileDispenseBehavior(EntityID.SMALL_FIREBALL) {
            override val accuracy: Float
                get() = 0f

            override fun initMotion(face: BlockFace): Vector3 {
                return Vector3(
                    face.xOffset.toDouble(),
                    face.yOffset.toDouble(),  /* + 0.1f*/
                    face.zOffset.toDouble()
                )
                    .normalize()
            }

            override val shootingSound: Sound
                get() = Sound.MOB_BLAZE_SHOOT
        })
        registerBehavior(ItemID.EXPERIENCE_BOTTLE, object : ProjectileDispenseBehavior(EntityID.XP_BOTTLE) {
            override val accuracy: Float
                get() = super.accuracy * 0.5f

            override val motion: Double
                get() = super.motion * 1.25
        })
        registerBehavior(ItemID.SPLASH_POTION, object : ProjectileDispenseBehavior(EntityID.SPLASH_POTION) {
            override val accuracy: Float
                get() = super.accuracy * 0.5f

            override val motion: Double
                get() = super.motion * 1.25
        })
        //        registerBehavior(ItemID.LINGERING_POTION, new ProjectileDispenseBehavior("LingeringPotion")); //TODO
        registerBehavior(ItemID.TRIDENT, object : ProjectileDispenseBehavior(EntityID.THROWN_TRIDENT) {
            override val accuracy: Float
                get() = super.accuracy * 0.5f

            override val motion: Double
                get() = super.motion * 1.25

            override val shootingSound: Sound
                get() = Sound.ITEM_TRIDENT_THROW
        })
        registerBehavior(ItemID.GLASS_BOTTLE, GlassBottleDispenseBehavior())
        registerBehavior(ItemID.POTION, WaterBottleDispenseBehavior())
        registerBehavior(ItemID.MINECART, MinecartDispenseBehavior(EntityID.MINECART))
        registerBehavior(ItemID.CHEST_MINECART, MinecartDispenseBehavior(EntityID.CHEST_MINECART))
        registerBehavior(ItemID.HOPPER_MINECART, MinecartDispenseBehavior(EntityID.HOPPER_MINECART))
        registerBehavior(ItemID.TNT_MINECART, MinecartDispenseBehavior(EntityID.TNT_MINECART))
        //TODO: 命令方块矿车
    }
}
