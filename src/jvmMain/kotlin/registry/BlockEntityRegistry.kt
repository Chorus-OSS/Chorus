package org.chorus_oss.chorus.registry

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import org.chorus_oss.chorus.blockentity.*
import java.util.concurrent.atomic.AtomicBoolean

class BlockEntityRegistry : BlockEntityID,
    IRegistry<String, Class<out BlockEntity>?, Class<out BlockEntity>> {
    override fun init() {
        if (isLoad.getAndSet(true)) return
        register0(BlockEntityID.FURNACE, BlockEntityFurnace::class.java)
        register0(BlockEntityID.CHEST, BlockEntityChest::class.java)
        register0(BlockEntityID.SIGN, BlockEntitySign::class.java)
        register0(BlockEntityID.ENCHANT_TABLE, BlockEntityEnchantTable::class.java)
        register0(BlockEntityID.SKULL, BlockEntitySkull::class.java)
        register0(BlockEntityID.FLOWER_POT, BlockEntityFlowerPot::class.java)
        register0(BlockEntityID.BREWING_STAND, BlockEntityBrewingStand::class.java)
        register0(BlockEntityID.ITEM_FRAME, BlockEntityItemFrame::class.java)
        register0(BlockEntityID.CAULDRON, BlockEntityCauldron::class.java)
        register0(BlockEntityID.ENDER_CHEST, BlockEntityEnderChest::class.java)
        register0(BlockEntityID.BEACON, BlockEntityBeacon::class.java)
        register0(BlockEntityID.PISTON_ARM, BlockEntityPistonArm::class.java)
        register0(BlockEntityID.COMPARATOR, BlockEntityComparator::class.java)
        register0(BlockEntityID.HOPPER, BlockEntityHopper::class.java)
        register0(BlockEntityID.BED, BlockEntityBed::class.java)
        register0(BlockEntityID.JUKEBOX, BlockEntityJukebox::class.java)
        register0(BlockEntityID.SHULKER_BOX, BlockEntityShulkerBox::class.java)
        register0(BlockEntityID.BANNER, BlockEntityBanner::class.java)
        register0(BlockEntityID.MUSIC, BlockEntityMusic::class.java)
        register0(BlockEntityID.MOB_SPAWNER, BlockEntityMobSpawner::class.java)
        register0(BlockEntityID.CREAKING_HEART, BlockEntityCreakingHeart::class.java)
        register0(BlockEntityID.LECTERN, BlockEntityLectern::class.java)
        register0(BlockEntityID.BLAST_FURNACE, BlockEntityBlastFurnace::class.java)
        register0(BlockEntityID.SMOKER, BlockEntitySmoker::class.java)
        register0(BlockEntityID.BEEHIVE, BlockEntityBeehive::class.java)
        register0(BlockEntityID.CONDUIT, BlockEntityConduit::class.java)
        register0(BlockEntityID.BARREL, BlockEntityBarrel::class.java)
        register0(BlockEntityID.CAMPFIRE, BlockEntityCampfire::class.java)
        register0(BlockEntityID.BELL, BlockEntityBell::class.java)
        register0(BlockEntityID.DAYLIGHT_DETECTOR, BlockEntityDaylightDetector::class.java)
        register0(BlockEntityID.DISPENSER, BlockEntityDispenser::class.java)
        register0(BlockEntityID.DROPPER, BlockEntityDropper::class.java)
        register0(BlockEntityID.MOVING_BLOCK, BlockEntityMovingBlock::class.java)
        register0(BlockEntityID.NETHER_REACTOR, BlockEntityNetherReactor::class.java)
        register0(BlockEntityID.LODESTONE, BlockEntityLodestone::class.java)
        register0(BlockEntityID.TARGET, BlockEntityTarget::class.java)
        register0(BlockEntityID.END_PORTAL, BlockEntityEndPortal::class.java)
        register0(BlockEntityID.END_GATEWAY, BlockEntityEndGateway::class.java)
        register0(BlockEntityID.COMMAND_BLOCK, BlockEntityCommandBlock::class.java)
        register0(BlockEntityID.SCULK_SENSOR, BlockEntitySculkSensor::class.java)
        register0(BlockEntityID.CALIBRATED_SCULK_SENSOR, BlockEntityCalibratedSculkSensor::class.java)
        register0(BlockEntityID.SCULK_CATALYST, BlockEntitySculkCatalyst::class.java)
        register0(BlockEntityID.SCULK_SHRIEKER, BlockEntitySculkShrieker::class.java)
        register0(BlockEntityID.STRUCTURE_BLOCK, BlockEntityStructBlock::class.java)
        register0(BlockEntityID.GLOW_ITEM_FRAME, BlockEntityGlowItemFrame::class.java)
        register0(BlockEntityID.HANGING_SIGN, BlockEntityHangingSign::class.java)
        register0(BlockEntityID.CHISELED_BOOKSHELF, BlockEntityChiseledBookshelf::class.java)
        register0(BlockEntityID.DECORATED_POT, BlockEntityDecoratedPot::class.java)
    }

    override operator fun get(key: String): Class<out BlockEntity?>? {
        return knownBlockEntities[key]
    }

    fun getSaveId(c: Class<out BlockEntity?>?): String? {
        return knownBlockEntities.inverse()[c]
    }

    fun trim() {
    }

    override fun reload() {
        isLoad.set(false)
        knownBlockEntities.clear()
        init()
    }

    @Throws(RegisterException::class)
    override fun register(key: String, value: Class<out BlockEntity>) {
        if (knownBlockEntities.putIfAbsent(key, value) != null) {
            throw RegisterException("This BlockEntity has already been registered with the identifier: $key")
        }
    }

    private fun register0(key: String, value: Class<out BlockEntity?>) {
        try {
            register(key, value)
        } catch (e: RegisterException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private val knownBlockEntities: BiMap<String, Class<out BlockEntity>> = HashBiMap.create(35)
        private val isLoad = AtomicBoolean(false)
    }
}
