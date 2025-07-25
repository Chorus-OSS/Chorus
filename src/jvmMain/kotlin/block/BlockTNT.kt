package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.Entity.Companion.createEntity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.projectile.EntitySmallFireball
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityArrow
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.ChorusRandom
import org.chorus_oss.chorus.utils.RedstoneComponent
import kotlin.math.cos
import kotlin.math.sin

class BlockTNT @JvmOverloads constructor(state: BlockState = properties.defaultState) :
    BlockSolid(state), RedstoneComponent, Natural {
    override val name: String
        get() = "TNT"

    override val hardness: Double
        get() = 0.0

    override val resistance: Double
        get() = 0.0

    override fun canBeActivated(): Boolean {
        return true
    }

    override val burnChance: Int
        get() = 15

    override val burnAbility: Int
        get() = 100

    @JvmOverloads
    fun prime(fuse: Int = 80, source: Entity? = null) {
        level.setBlock(this.position, get(BlockID.AIR), true)
        val mot = (ChorusRandom()).nextFloat() * Math.PI * 2
        val nbt = CompoundTag()
            .putList(
                "Pos", ListTag<FloatTag>()
                    .add(FloatTag(position.x + 0.5))
                    .add(FloatTag(position.y))
                    .add(FloatTag(position.z + 0.5))
            )
            .putList(
                "Motion", ListTag<FloatTag>()
                    .add(FloatTag(-sin(mot) * 0.02))
                    .add(FloatTag(0.2))
                    .add(FloatTag(-cos(mot) * 0.02))
            )
            .putList(
                "Rotation", ListTag<FloatTag>()
                    .add(FloatTag(0f))
                    .add(FloatTag(0f))
            )
            .putShort("Fuse", fuse)
        val tnt = createEntity(
            EntityID.TNT,
            level.getChunk(position.floorX shr 4, position.floorZ shr 4),
            nbt, source
        )
        if (tnt == null) {
            return
        }
        tnt.spawnToAll()
        level.vibrationManager.callVibrationEvent(
            VibrationEvent(
                source ?: this,
                position.add(0.5, 0.5, 0.5), VibrationType.PRIME_FUSE
            )
        )
    }

    override fun onUpdate(type: Int): Int {
        if (!Server.instance.settings.levelSettings.enableRedstone) {
            return 0
        }

        if ((type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) && this.isGettingPower) {
            this.prime()
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
        when (item.id) {
            ItemID.FLINT_AND_STEEL -> {
                item.useOn(this)
                this.prime(80, player)
                return true
            }

            ItemID.FIRE_CHARGE -> {
                if (player != null && !player.isCreative) {
                    item.count--
                }
                this.prime(80, player)
                return true
            }
        }
        if (item.hasEnchantment(Enchantment.ID_FIRE_ASPECT) && item.applyEnchantments()) {
            item.useOn(this)
            this.prime(80, player)
            return true
        }
        return false
    }

    override fun onProjectileHit(projectile: Entity, locator: Locator, motion: Vector3): Boolean {
        //TODO: Wither skull, ghast fireball
        if (projectile is EntitySmallFireball ||
            (projectile.isOnFire() && projectile is EntityArrow)
        ) {
            prime(80, projectile)
            return true
        }
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.TNT, CommonBlockProperties.EXPLODE_BIT)
    }
}
