package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.CauldronLiquid
import org.chorus_oss.chorus.blockentity.BlockEntityCauldron
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.event.entity.EntityCombustByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.player.PlayerBucketEmptyEvent
import org.chorus_oss.chorus.event.player.PlayerBucketFillEvent
import org.chorus_oss.chorus.item.*
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.particle.SmokeParticle
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.BlockColor
import org.chorus_oss.protocol.packets.LevelEventPacket
import kotlin.math.sqrt

class BlockCauldron(blockState: BlockState = properties.defaultState) : BlockSolid(blockState),
    BlockEntityHolder<BlockEntityCauldron> {
    override fun getBlockEntityType(): String {
        return BlockEntityID.CAULDRON
    }

    override fun getBlockEntityClass(): Class<out BlockEntityCauldron> {
        return BlockEntityCauldron::class.java
    }

    override val name: String
        get() = if (cauldronLiquid == CauldronLiquid.LAVA) "Lava Cauldron" else "Cauldron Block"

    override val resistance: Double
        get() = 10.0

    override val hardness: Double
        get() = 2.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun canBeActivated(): Boolean {
        return true
    }

    val isFull: Boolean
        get() = fillLevel == CommonBlockProperties.FILL_LEVEL.max

    val isEmpty: Boolean
        get() = fillLevel == CommonBlockProperties.FILL_LEVEL.min

    var fillLevel: Int
        get() = getPropertyValue(CommonBlockProperties.FILL_LEVEL)
        set(fillLevel) {
            this.setFillLevel(fillLevel, null)
        }

    fun setFillLevel(fillLevel: Int, player: Player?) {
        if (fillLevel == this.fillLevel) return

        if (fillLevel > this.fillLevel) {
            level.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    player ?: this,
                    position.add(0.5, 0.5, 0.5), VibrationType.FLUID_PLACE
                )
            )
        } else {
            level.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    player ?: this,
                    position.add(0.5, 0.5, 0.5), VibrationType.FLUID_PICKUP
                )
            )
        }

        this.setPropertyValue(CommonBlockProperties.FILL_LEVEL, fillLevel)
    }

    var cauldronLiquid: CauldronLiquid
        get() = this.getPropertyValue(
            CommonBlockProperties.CAULDRON_LIQUID
        )
        set(liquid) {
            this.setPropertyValue(
                CommonBlockProperties.CAULDRON_LIQUID,
                liquid
            )
        }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        // lava
        if (cauldronLiquid == CauldronLiquid.LAVA) {
            return onLavaActivate(item, player, blockFace, fx, fy, fz)
        }

        // non-lava
        val cauldron = blockEntity ?: return false

        if (item is ItemBucket) {
            if (item.fishEntityId != null) {
                return false
            }
            if (item.isEmpty) {
                if (!isFull || cauldron.isCustomColor() || cauldron.hasPotion()) {
                    return false
                }

                val newBucketID = when (this.cauldronLiquid) {
                    CauldronLiquid.POWDER_SNOW -> ItemID.POWDER_SNOW_BUCKET
                    CauldronLiquid.LAVA -> ItemID.LAVA_BUCKET
                    else -> ItemID.WATER_BUCKET
                }

                val ev = PlayerBucketFillEvent(
                    player!!,
                    this, blockFace,
                    this, item, get(newBucketID, 0, 1, item.compoundTag)
                )
                Server.instance.pluginManager.callEvent(ev)
                if (!ev.cancelled) {
                    replaceBucket(item, player, ev.item)
                    this.setFillLevel(CommonBlockProperties.FILL_LEVEL.min, player) // empty
                    level.setBlock(this.position, this, true)
                    cauldron.clearCustomColor()
                    level.addLevelEvent(
                        position.add(0.5, 0.375 + fillLevel * 0.125, 0.5),
                        LevelEventPacket.CAULDRON_TAKE_WATER
                    )
                }
            } else if (item.isWater || item.isLava || item.isPowderSnow) {
                if (isFull && !cauldron.isCustomColor() && !cauldron.hasPotion() && item.damage == 8) {
                    return false
                }

                val ev = PlayerBucketEmptyEvent(
                    player!!,
                    this, blockFace,
                    this, item, get(ItemID.BUCKET, 0, 1, item.compoundTag)
                )
                Server.instance.pluginManager.callEvent(ev)
                if (!ev.cancelled) {
                    if (player.isSurvival || player.isAdventure) {
                        replaceBucket(item, player, ev.item)
                    }
                    if (cauldron.hasPotion()) { //if has potion
                        clearWithFizz(cauldron, player)
                    } else if (item.isWater) { //water bucket
                        this.setFillLevel(CommonBlockProperties.FILL_LEVEL.max, player) // fill
                        //default liquid type is water so we don't need to set it
                        cauldron.clearCustomColor()
                        level.setBlock(this.position, this, true)
                        level.addSound(position.add(0.5, 1.0, 0.5), Sound.CAULDRON_FILLWATER)
                    } else if (item.isPowderSnow) { // powder snow bucket
                        this.setFillLevel(CommonBlockProperties.FILL_LEVEL.max, player) //fill
                        this.cauldronLiquid = CauldronLiquid.POWDER_SNOW
                        cauldron.clearCustomColor()
                        level.setBlock(this.position, this, true)
                        // TODO: add the sound of powder snow (I can't find it)
                    } else { // lava bucket
                        if (isEmpty) {
                            this.cauldronLiquid = CauldronLiquid.LAVA
                            this.setFillLevel(CommonBlockProperties.FILL_LEVEL.max, player)
                            level.setBlock(this.position, this, true)
                            cauldron.clearCustomColor()
                            cauldron.type = BlockEntityCauldron.PotionType.LAVA
                            level.addSound(position.add(0.5, 1.0, 0.5), Sound.BUCKET_EMPTY_LAVA)
                        } else {
                            clearWithFizz(cauldron, player)
                        }
                    }
                }
            }
        }

        run switch@{
            when (item.id) {
                ItemID.DYE -> {
                    if (isEmpty || cauldron.hasPotion()) {
                        return@switch
                    }

                    if (player!!.isSurvival || player.isAdventure) {
                        item.setCount(item.getCount() - 1)
                        player.inventory.setItemInHand(item)
                    }

                    val color = ItemDye(item.damage).dyeColor.leatherColor
                    if (!cauldron.isCustomColor()) {
                        cauldron.customColor = color
                    } else {
                        val current = cauldron.customColor
                        val mixed = BlockColor(
                            Math.round(sqrt((color.red * current!!.red).toDouble()) * 0.965).toInt(),
                            Math.round(sqrt((color.green * current.green).toDouble()) * 0.965).toInt(),
                            Math.round(sqrt((color.blue * current.blue).toDouble()) * 0.965).toInt()
                        )
                        cauldron.customColor = mixed
                    }
                    level.addSound(position.add(0.5, 0.5, 0.5), Sound.CAULDRON_ADDDYE)
                }

                ItemID.LEATHER_HELMET, ItemID.LEATHER_CHESTPLATE, ItemID.LEATHER_LEGGINGS, ItemID.LEATHER_BOOTS, ItemID.LEATHER_HORSE_ARMOR -> {
                    if (isEmpty || cauldron.hasPotion()) {
                        return@switch
                    }

                    if (cauldron.isCustomColor()) {
                        val compoundTag = if (item.hasCompoundTag()) item.namedTag else CompoundTag()
                        compoundTag!!.putInt("customColor", cauldron.customColor!!.rgb)
                        item.setCompoundTag(compoundTag)
                        player!!.inventory.setItemInHand(item)
                        setFillLevel(
                            (fillLevel - 2).coerceIn(
                                CommonBlockProperties.FILL_LEVEL.min,
                                CommonBlockProperties.FILL_LEVEL.max
                            ), player
                        )
                        level.setBlock(this.position, this, direct = true, update = true)
                        level.addSound(position.add(0.5, 0.5, 0.5), Sound.CAULDRON_DYEARMOR)
                    } else {
                        if (!item.hasCompoundTag()) {
                            return@switch
                        }

                        val compoundTag = item.namedTag
                        if (!compoundTag!!.exist("customColor")) {
                            return@switch
                        }

                        compoundTag.remove("customColor")
                        item.setCompoundTag(compoundTag)
                        player!!.inventory.setItemInHand(item)

                        setFillLevel(
                            (fillLevel - 2).coerceIn(
                                CommonBlockProperties.FILL_LEVEL.min,
                                CommonBlockProperties.FILL_LEVEL.max
                            ), player
                        )
                        level.setBlock(this.position, this, direct = true, update = true)
                        level.addSound(position.add(0.5, 1.0, 0.5), Sound.CAULDRON_TAKEWATER)
                    }
                }

                ItemID.POTION, ItemID.SPLASH_POTION, ItemID.LINGERING_POTION -> {
                    if (!isEmpty && (if (cauldron.hasPotion()) cauldron.potionId != item.damage else item.damage != 0)) {
                        clearWithFizz(cauldron, player)
                        consumePotion(item, player!!)
                        return@switch
                    }
                    if (isFull) {
                        return@switch
                    }

                    if (item.damage != 0 && isEmpty) {
                        cauldron.potionId = item.damage
                    }

                    cauldron.type =
                        if (item.id == ItemID.POTION) BlockEntityCauldron.PotionType.NORMAL else if (item.id == ItemID.SPLASH_POTION) BlockEntityCauldron.PotionType.SPLASH else BlockEntityCauldron.PotionType.LINGERING

                    cauldron.spawnToAll()

                    setFillLevel(
                        (fillLevel + 2).coerceIn(
                            CommonBlockProperties.FILL_LEVEL.min,
                            CommonBlockProperties.FILL_LEVEL.max
                        ), player
                    )
                    level.setBlock(this.position, this, true)

                    consumePotion(item, player!!)

                    level.addLevelEvent(
                        position.add(0.5, 0.375 + fillLevel * 0.125, 0.5),
                        LevelEventPacket.CAULDRON_FILL_POTION
                    )
                }

                ItemID.GLASS_BOTTLE -> {
                    if (isEmpty) {
                        return@switch
                    }

                    val meta = if (cauldron.hasPotion()) cauldron.potionId else 0
                    val potion = if (meta == 0) {
                        ItemPotion()
                    } else {
                        when (cauldron.type) {
                            BlockEntityCauldron.PotionType.SPLASH -> ItemSplashPotion(meta)
                            BlockEntityCauldron.PotionType.LINGERING -> ItemLingeringPotion(meta)
                            else -> ItemPotion(meta)
                        }
                    }

                    setFillLevel(
                        (fillLevel - 2).coerceIn(
                            CommonBlockProperties.FILL_LEVEL.min,
                            CommonBlockProperties.FILL_LEVEL.max
                        ), player
                    )
                    if (isEmpty) {
                        cauldron.potionId = -1 //reset potion
                        cauldron.clearCustomColor()
                    }
                    level.setBlock(this.position, this, true)

                    val consumeBottle = player!!.isSurvival || player.isAdventure
                    if (consumeBottle && item.getCount() == 1) {
                        player.inventory.setItemInHand(potion)
                    } else if (item.getCount() > 1) {
                        if (consumeBottle) {
                            item.setCount(item.getCount() - 1)
                            player.inventory.setItemInHand(item)
                        }

                        if (player.inventory.canAddItem(potion)) {
                            player.inventory.addItem(potion)
                        } else {
                            player.level!!.dropItem(
                                player.position.add(0.0, 1.3, 0.0),
                                potion,
                                player.getDirectionVector().multiply(0.4)
                            )
                        }
                    }

                    level.addLevelEvent(
                        position.add(0.5, 0.375 + fillLevel * 0.125, 0.5),
                        LevelEventPacket.CAULDRON_TAKE_POTION
                    )
                }

                ItemID.BANNER -> {
                    if (isEmpty || cauldron.isCustomColor() || cauldron.hasPotion()) {
                        return@switch
                    }

                    val banner = item as ItemBanner
                    if (!banner.hasPattern()) {
                        return@switch
                    }

                    banner.removePattern(banner.patternsSize - 1)
                    val consumeBanner = player!!.isSurvival || player.isAdventure
                    if (consumeBanner && item.getCount() < item.maxStackSize) {
                        player.inventory.setItemInHand(banner)
                    } else {
                        if (consumeBanner) {
                            item.setCount(item.getCount() - 1)
                            player.inventory.setItemInHand(item)
                        }

                        if (player.inventory.canAddItem(banner)) {
                            player.inventory.addItem(banner)
                        } else {
                            player.level!!.dropItem(
                                player.position.add(0.0, 1.3, 0.0),
                                banner,
                                player.getDirectionVector().multiply(0.4)
                            )
                        }
                    }

                    setFillLevel(
                        (fillLevel - 2).coerceIn(
                            CommonBlockProperties.FILL_LEVEL.min,
                            CommonBlockProperties.FILL_LEVEL.max
                        ), player
                    )
                    level.setBlock(this.position, this, direct = true, update = true)
                    level.addSound(position.add(0.5, 1.0, 0.5), Sound.CAULDRON_TAKEWATER)
                }

                else -> if (item is ItemDye) {
                    if (isEmpty || cauldron.hasPotion()) {
                        return@switch
                    }

                    if (player!!.isSurvival || player.isAdventure) {
                        item.setCount(item.getCount() - 1)
                        player.inventory.setItemInHand(item)
                    }

                    color = item.dyeColor.color
                    if (!cauldron.isCustomColor()) {
                        cauldron.customColor = color
                    } else {
                        val current = cauldron.customColor
                        val mixed = BlockColor(
                            current!!.red + (color.red - current.red) / 2,
                            current.green + (color.green - current.green) / 2,
                            current.blue + (color.blue - current.blue) / 2
                        )
                        cauldron.customColor = mixed
                    }
                    level.addSound(position.add(0.5, 0.5, 0.5), Sound.CAULDRON_ADDDYE)
                } else {
                    return true
                }
            }
        }

        level.updateComparatorOutputLevel(this.position)
        return true
    }

    fun onLavaActivate(item: Item, player: Player?, blockFace: BlockFace, fx: Float, fy: Float, fz: Float): Boolean {
        val be =
            level.getBlockEntity(this.position) as? BlockEntityCauldron ?: return false

        run switch@{
            when (item.id) {
                ItemID.BUCKET -> {
                    val bucket = item as ItemBucket
                    if (bucket.fishEntityId != null) {
                        return@switch
                    }
                    if (item.damage == 0) { //empty
                        if (!isFull || be.isCustomColor() || be.hasPotion()) {
                            return@switch
                        }

                        val ev = PlayerBucketFillEvent(
                            player!!,
                            this, blockFace,
                            this, item, get(ItemID.LAVA_BUCKET, 0, 1, bucket.compoundTag)
                        )
                        Server.instance.pluginManager.callEvent(ev)
                        if (!ev.cancelled) {
                            replaceBucket(bucket, player, ev.item)
                            this.setFillLevel(CommonBlockProperties.FILL_LEVEL.min, player) //empty
                            level.setBlock(this.position, BlockCauldron(), true)
                            be.clearCustomColor()
                            level.addSound(position.add(0.5, 1.0, 0.5), Sound.BUCKET_FILL_LAVA)
                        }
                    } else if (bucket.isWater || bucket.isLava) { //water or lava bucket
                        if (isFull && !be.isCustomColor() && !be.hasPotion() && item.damage == 10) {
                            return@switch
                        }

                        val ev = PlayerBucketEmptyEvent(
                            player!!,
                            this, blockFace,
                            this, item, get(ItemID.BUCKET, 0, 1, bucket.compoundTag)
                        )
                        Server.instance.pluginManager.callEvent(ev)
                        if (!ev.cancelled) {
                            replaceBucket(bucket, player, ev.item)

                            if (be.hasPotion()) { //if has potion
                                clearWithFizz(be)
                            } else if (bucket.isLava) { //lava bucket
                                this.setFillLevel(CommonBlockProperties.FILL_LEVEL.max, player) //fill
                                be.clearCustomColor()
                                level.setBlock(this.position, this, true)
                                level.addSound(position.add(0.5, 1.0, 0.5), Sound.BUCKET_EMPTY_LAVA)
                            } else {
                                if (isEmpty) {
                                    val blockCauldron = BlockCauldron()
                                    blockCauldron.fillLevel = 6
                                    level.setBlock(this.position, blockCauldron, direct = true, update = true)
                                    be.clearCustomColor()
                                    level.addSound(position.add(0.5, 1.0, 0.5), Sound.CAULDRON_FILLWATER)
                                } else {
                                    clearWithFizz(be)
                                }
                            }
                        }
                    }
                }

                ItemID.POTION, ItemID.SPLASH_POTION, ItemID.LINGERING_POTION -> {
                    if (!isEmpty && (if (be.hasPotion()) be.potionId != item.damage else item.damage != 0)) {
                        clearWithFizz(be)
                        return@switch
                    }
                    return super.onActivate(item, player, blockFace, fx, fy, fz)
                }

                ItemID.GLASS_BOTTLE -> {
                    if (!isEmpty && be.hasPotion()) {
                        return super.onActivate(item, player, blockFace, fx, fy, fz)
                    }
                    return true
                }

                else -> return true
            }
        }


        level.updateComparatorOutputLevel(this.position)
        return true
    }

    protected fun replaceBucket(oldBucket: Item, player: Player, newBucket: Item) {
        if (player.isSurvival || player.isAdventure) {
            if (oldBucket.getCount() == 1) {
                player.inventory.setItemInHand(newBucket)
            } else {
                oldBucket.setCount(oldBucket.getCount() - 1)
                if (player.inventory.canAddItem(newBucket)) {
                    player.inventory.addItem(newBucket)
                } else {
                    player.level!!.dropItem(
                        player.position.add(0.0, 1.3, 0.0),
                        newBucket,
                        player.getDirectionVector().multiply(0.4)
                    )
                }
            }
        }
    }

    private fun consumePotion(item: Item, player: Player) {
        if (player.isSurvival || player.isAdventure) {
            if (item.getCount() == 1) {
                player.inventory.setItemInHand(Item.AIR)
            } else if (item.getCount() > 1) {
                item.setCount(item.getCount() - 1)
                player.inventory.setItemInHand(item)

                val bottle: Item = ItemGlassBottle()
                if (player.inventory.canAddItem(bottle)) {
                    player.inventory.addItem(bottle)
                } else {
                    player.level!!.dropItem(
                        player.position.add(0.0, 1.3, 0.0),
                        bottle,
                        player.getDirectionVector().multiply(0.4)
                    )
                }
            }
        }
    }

    @JvmOverloads
    fun clearWithFizz(cauldron: BlockEntityCauldron, player: Player? = null) {
        this.setFillLevel(CommonBlockProperties.FILL_LEVEL.min, player) //empty
        cauldron.potionId = -1 //reset potion
        cauldron.type = BlockEntityCauldron.PotionType.NORMAL
        cauldron.clearCustomColor()
        level.setBlock(this.position, BlockCauldron(), true)
        level.addSound(position.add(0.5, 0.0, 0.5), Sound.RANDOM_FIZZ)
        for (i in 0..7) {
            level.addParticle(SmokeParticle(position.add(Math.random(), 1.2, Math.random())))
        }
    }

    override fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        val nbt = CompoundTag()
            .putShort("PotionId", -1)
            .putByte("SplashPotion", 0)

        if (item!!.hasCustomBlockData()) {
            val customData = item.customBlockData!!.tags
            for ((key, value) in customData) {
                nbt.put(key, value)
            }
        }

        return BlockEntityHolder.setBlockAndCreateEntity(this, direct = true, update = true, initialData = nbt) != null
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() = fillLevel

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override val isTransparent: Boolean
        get() = true

    override val lightFilter: Int
        get() = 3

    override val lightLevel: Int
        get() = if (cauldronLiquid == CauldronLiquid.LAVA) 15 else 0

    override fun hasEntityCollision(): Boolean {
        return true
    }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB {
        return shrink(0.3, 0.3, 0.3)
    }

    override fun onEntityCollide(entity: Entity) {
        val ev = EntityCombustByBlockEvent(this, entity, 15)
        Server.instance.pluginManager.callEvent(ev)
        if (!ev.cancelled) {
            // Making sure the entity is actually alive and not invulnerable.
            if (cauldronLiquid == CauldronLiquid.LAVA && entity.isAlive() && entity.noDamageTicks == 0) {
                entity.setOnFire(ev.duration)
                if (!entity.hasEffect(EffectType.FIRE_RESISTANCE)) {
                    entity.attack(EntityDamageByBlockEvent(this, entity, DamageCause.LAVA, 4f))
                }
            } else if (entity.isAlive() && entity.isOnFire()) {
                entity.setOnFire(0)
            }
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.CAULDRON, CommonBlockProperties.CAULDRON_LIQUID, CommonBlockProperties.FILL_LEVEL)
    }
}
