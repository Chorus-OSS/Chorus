package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.event.block.BlockFormEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Level

class BlockMagma(blockState: BlockState = properties.defaultState) : BlockSolid(blockState) {
    override val name: String
        get() = "Magma Block"

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val hardness: Double
        get() = 0.5

    override val resistance: Double
        get() = 30.0

    override val lightLevel: Int
        get() = 3

    override fun getDrops(item: Item): Array<Item> {
        return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
            arrayOf(
                toItem()
            )
        } else {
            Item.EMPTY_ARRAY
        }
    }

    override fun onEntityCollide(entity: Entity) {
        if (entity.hasEffect(EffectType.FIRE_RESISTANCE)) {
            return
        }

        if (entity is Player) {
            if (entity.inventory.boots.getEnchantment(Enchantment.ID_FROST_WALKER) != null || entity.isCreative || entity.isSpectator || entity.isSneaking() || !entity.level!!.gameRules.getBoolean(
                    GameRule.FIRE_DAMAGE
                )
            ) {
                return
            }
        }

        entity.attack(EntityDamageByBlockEvent(this, entity, EntityDamageEvent.DamageCause.HOT_FLOOR, 1f))
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            val up = up()
            if (up is BlockFlowingWater && (up.liquidDepth == 0 || up.liquidDepth == 8)) {
                val event = BlockFormEvent(
                    up,
                    BlockBubbleColumn().setPropertyValue<Boolean, BooleanPropertyType>(
                        CommonBlockProperties.DRAG_DOWN,
                        true
                    )
                )
                if (!event.cancelled) {
                    if (event.newState.waterloggingLevel > 0) {
                        level.setBlock(up.position, 1, BlockFlowingWater(), true, false)
                    }
                    level.setBlock(up.position, 0, event.newState, true, true)
                }
            }
        }
        return 0
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.MAGMA)
    }
}
