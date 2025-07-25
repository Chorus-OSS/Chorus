package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server.Companion.instance
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.MinecraftCardinalDirection
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.blockentity.BlockEntityCampfire
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.entity.projectile.EntitySmallFireball
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityArrow
import org.chorus_oss.chorus.entity.projectile.throwable.EntitySplashPotion
import org.chorus_oss.chorus.event.entity.EntityCombustByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.inventory.ContainerInventory.Companion.calculateRedstone
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.Loggable


open class BlockCampfire @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), Faceable, BlockEntityHolder<BlockEntityCampfire>, Loggable {
    override fun getBlockEntityType(): String {
        return BlockEntityID.CAMPFIRE
    }

    override fun getBlockEntityClass(): Class<out BlockEntityCampfire> {
        return BlockEntityCampfire::class.java
    }

    override val lightLevel: Int
        get() = if (isExtinguished) 0 else 15

    override val resistance: Double
        get() = 2.0

    override val hardness: Double
        get() = 5.0

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override fun canHarvestWithHand(): Boolean {
        return true
    }

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(get(ItemID.CHARCOAL, 0, 2))
    }

    override fun canSilkTouch(): Boolean {
        return true
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
        if (down().properties === Companion.properties) {
            return false
        }

        val layer0 = level.getBlock(this.position, 0)
        val layer1 = level.getBlock(this.position, 1)

        if (player != null) {
            blockFace = player.getDirection().getOpposite()
        }

        val defaultLayerCheck = (block is BlockFlowingWater && block.isSourceOrFlowingDown) || block is BlockFrostedIce
        val layer1Check = (layer1 is BlockFlowingWater && layer1.isSourceOrFlowingDown) || layer1 is BlockFrostedIce
        if (defaultLayerCheck || layer1Check) {
            isExtinguished = true
            level.addSound(this.position, Sound.RANDOM_FIZZ, 0.5f, 2.2f)
            level.setBlock(
                this.position, 1,
                (if (defaultLayerCheck) block else layer1), direct = false, update = false
            )
        } else {
            level.setBlock(this.position, 1, get(BlockID.AIR), direct = false, update = false)
        }

        level.setBlock(block.position, this, direct = true, update = true)
        try {
            val nbt = CompoundTag()

            if (item!!.hasCustomBlockData()) {
                val customData: Map<String, Tag<*>> = item.customBlockData!!.tags
                for ((key, value) in customData) {
                    nbt.put(key, value)
                }
            }

            createBlockEntity(nbt)
        } catch (e: Exception) {
            log.warn("Failed to create the block entity {} at {}", getBlockEntityType(), locator, e)
            level.setBlock(layer0.position, 0, layer0, true)
            level.setBlock(layer1.position, 0, layer1, true)
            return false
        }

        level.updateAround(this.position)
        return true
    }

    override fun hasEntityCollision(): Boolean {
        return true
    }

    override fun onEntityCollide(entity: Entity) {
        if (isExtinguished) {
            if (entity.isOnFire()) {
                isExtinguished = false
                level.setBlock(this.position, this, true)
            }
            return
        }

        if (entity.hasEffect(EffectType.FIRE_RESISTANCE)
            || entity is EntityProjectile
            || !entity.attack(getDamageEvent(entity)!!) || !entity.isAlive()
        ) {
            return
        }

        val ev = EntityCombustByBlockEvent(this, entity, 8)
        instance.pluginManager.callEvent(ev)
        if (!ev.cancelled && entity.isAlive()) {
            entity.setOnFire(ev.duration)
        }
    }

    protected open fun getDamageEvent(entity: Entity): EntityDamageEvent? {
        return EntityDamageByBlockEvent(this, entity, DamageCause.FIRE, 1f)
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!isExtinguished) {
                val layer1 = getLevelBlockAtLayer(1)
                if (layer1 is BlockFlowingWater || layer1 is BlockFrostedIce) {
                    isExtinguished = true
                    level.setBlock(this.position, this, direct = true, update = true)
                    level.addSound(this.position, Sound.RANDOM_FIZZ, 0.5f, 2.2f)
                }
            }
            return type
        }
        return 0
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (item.isNothing) {
            return false
        }

        val campfire = getOrCreateBlockEntity()

        var itemUsed = false
        if (!isExtinguished) {
            if (item.isShovel) {
                isExtinguished = true
                level.setBlock(this.position, this, direct = true, update = true)
                level.addSound(this.position, Sound.RANDOM_FIZZ, 0.5f, 2.2f)
                itemUsed = true
            }
        } else if (item.id == ItemID.FLINT_AND_STEEL || item.getEnchantment(Enchantment.ID_FIRE_ASPECT) != null) {
            item.useOn(this)
            isExtinguished = false
            level.setBlock(this.position, this, direct = true, update = true)
            campfire.scheduleUpdate()
            level.addSound(this.position, Sound.FIRE_IGNITE)
            itemUsed = true
        }

        val cloned: Item = item.clone()
        cloned.setCount(1)
        val inventory = campfire.inventory
        if (inventory.canAddItem(cloned)) {
            val recipe = instance.recipeRegistry.findCampfireRecipe(cloned)
            if (recipe != null) {
                inventory.addItem(cloned)
                item.setCount(item.getCount() - 1)
                return true
            }
        }

        return itemUsed
    }

    override fun onProjectileHit(projectile: Entity, locator: Locator, motion: Vector3): Boolean {
        if ((projectile is EntitySmallFireball || (projectile.isOnFire() && projectile is EntityArrow)) && isExtinguished) {
            isExtinguished = false
            level.setBlock(this.position, this, true)
            return true
        } else if (projectile is EntitySplashPotion && !isExtinguished && projectile.potionId == 0) {
            isExtinguished = true
            level.setBlock(this.position, this, true)
            return true
        }
        return false
    }

    override val waterloggingLevel: Int
        get() = 1

    override var maxY: Double
        get() = position.y + 0.4371948
        set(maxY) {
            super.maxY = maxY
        }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
        return SimpleAxisAlignedBB(
            position.x,
            position.y,
            position.z, position.x + 1, position.y + 1, position.z + 1
        )
    }

    var isExtinguished: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.EXTINGUISHED)
        set(extinguished) {
            setPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.EXTINGUISHED,
                extinguished
            )
        }

    override var blockFace: BlockFace
        get() = fromHorizontalIndex(
            getPropertyValue(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
            ).ordinal
        )
        set(face) {
            setPropertyValue(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
                MinecraftCardinalDirection.entries[face.getOpposite().horizontalIndex]
            )
        }

    override val name: String
        get() = "Campfire"

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() {
            val blockEntity = blockEntity

            if (blockEntity != null) {
                return calculateRedstone(blockEntity.inventory)
            }

            return super.comparatorInputOverride
        }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun canBePulled(): Boolean {
        return false
    }

    override fun canBePushed(): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.CAMPFIRE,
            CommonBlockProperties.EXTINGUISHED,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
        )
    }
}
