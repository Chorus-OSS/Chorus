package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.event.block.BlockExplosionPrimeEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.level.Explosion
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.network.protocol.types.SpawnPointType
import org.chorus_oss.chorus.utils.TextFormat

class BlockRespawnAnchor(blockState: BlockState = properties.defaultState) : Block(blockState) {
    override val name: String
        get() = "Respawn Anchor"

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        val charge = charge
        if (item.blockId == BlockID.GLOWSTONE && charge < CommonBlockProperties.RESPAWN_ANCHOR_CHARGE.max) {
            if (player == null || !player.isCreative) {
                item.count--
            }

            this.charge = charge + 1
            level.setBlock(this.position, this)
            level.addSound(this.position, Sound.RESPAWN_ANCHOR_CHARGE)
            return true
        }

        if (player == null) {
            return false
        }

        return if (charge > 0) {
            attemptToSetSpawn(player)
        } else {
            false
        }
    }

    protected fun attemptToSetSpawn(player: Player): Boolean {
        if (level.dimension != Level.DIMENSION_NETHER) {
            if (level.gameRules.getBoolean(GameRule.TNT_EXPLODES)) {
                explode(player)
            }
            return true
        }

        if (player.spawn.first == this) {
            return false
        }
        player.setSpawn(this, SpawnPointType.BLOCK)
        level.addSound(this.position, Sound.RESPAWN_ANCHOR_SET_SPAWN)
        player.sendMessage(TranslationContainer(TextFormat.GRAY.toString() + "%tile.respawn_anchor.respawnSet"))
        return true
    }

    fun explode(player: Player?) {
        val event = BlockExplosionPrimeEvent(this, player, 5.0)
        event.isIncendiary = true
        if (event.cancelled) {
            return
        }

        level.setBlock(this.position, get(BlockID.AIR))
        val explosion = Explosion(this, event.force, this)
        explosion.fireChance = event.fireChance
        if (event.isBlockBreaking) {
            explosion.explodeA()
        }
        explosion.explodeB()
    }

    var charge: Int
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.RESPAWN_ANCHOR_CHARGE)
        set(charge) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.RESPAWN_ANCHOR_CHARGE, charge)
        }

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_DIAMOND

    override val resistance: Double
        get() = 1200.0

    override val hardness: Double
        get() = 50.0

    override val lightLevel: Int
        get() = when (charge) {
            0 -> 0
            1 -> 3
            2 -> 7
            else -> 15
        }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            level.addSound(this.position, Sound.RESPAWN_ANCHOR_DEPLETE)
            return type
        }
        return super.onUpdate(type)
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun canBePushed(): Boolean {
        return false
    }

    override fun canBePulled(): Boolean {
        return false
    }

    override fun getDrops(item: Item): Array<Item> {
        if (canHarvest(item)) {
            return arrayOf(Item.get(id))
        }
        return Item.EMPTY_ARRAY
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.RESPAWN_ANCHOR, CommonBlockProperties.RESPAWN_ANCHOR_CHARGE)
    }
}
