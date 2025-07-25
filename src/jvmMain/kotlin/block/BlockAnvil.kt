package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.block.property.enums.Damage
import org.chorus_oss.chorus.inventory.AnvilInventory
import org.chorus_oss.chorus.inventory.BlockInventoryHolder
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.utils.Faceable
import java.util.function.Supplier
import kotlin.math.abs

open class BlockAnvil @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFallable(blockstate), Faceable, BlockInventoryHolder {
    var anvilDamage: Damage
        get() = when (this) {
            is BlockChippedAnvil -> Damage.SLIGHTLY_DAMAGED
            is BlockDamagedAnvil -> Damage.VERY_DAMAGED
            else -> Damage.UNDAMAGED
        }
        set(anvilDamage) {
            this.blockState = when (anvilDamage) {
                Damage.UNDAMAGED -> Companion.properties.defaultState
                Damage.SLIGHTLY_DAMAGED -> BlockChippedAnvil.properties.defaultState
                Damage.VERY_DAMAGED -> BlockDamagedAnvil.properties.defaultState
                Damage.BROKEN -> BlockAir.Companion.STATE
            }
        }

    override fun canBeActivated(): Boolean {
        return true
    }

    override val waterloggingLevel: Int
        get() = 1

    override val isTransparent: Boolean
        get() = true

    override val hardness: Double
        get() = 5.0

    override val resistance: Double
        get() = 6000.0

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val name: String
        get() = anvilDamage.name

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
        blockFace = if (player != null) player.getDirection().rotateYCCW() else BlockFace.SOUTH
        level.setBlock(this.position, this, true)
        if (player == null) {
            level.addSound(this.position, Sound.RANDOM_ANVIL_LAND, 1f, 0.8f)
        } else {
            val players = level.getChunkPlayers(
                position.chunkX,
                position.chunkZ
            ).values.toMutableList()
            players.remove(player)
            if (!players.isEmpty()) {
                level.addSound(this.position, Sound.RANDOM_ANVIL_LAND, 1f, 0.8f, players)
            }
        }
        return true
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (isNotActivate(player)) return false
        player?.addWindow(inventory)
        return true
    }

    override fun blockInventorySupplier(): Supplier<Inventory> {
        return Supplier { AnvilInventory(this) }
    }

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override var blockFace: BlockFace
        get() = CommonPropertyMap.CARDINAL_BLOCKFACE[getPropertyValue(
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
        )]!!
        set(face) {
            this.setPropertyValue(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
                CommonPropertyMap.CARDINAL_BLOCKFACE.inverse()[face]!!
            )
        }

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        val face = blockFace.rotateY()
        val xOffset = abs(face.xOffset.toDouble()) * (2 / 16.0)
        val zOffset = abs(face.zOffset.toDouble()) * (2 / 16.0)
        return SimpleAxisAlignedBB(
            position.x + xOffset,
            position.y,
            position.z + zOffset,
            position.x + 1 - xOffset, position.y + 1, position.z + 1 - zOffset
        )
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.ANVIL, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION)
    }
}
