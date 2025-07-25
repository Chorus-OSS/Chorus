package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server.Companion.instance
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.CauldronLiquid
import org.chorus_oss.chorus.block.property.enums.DripstoneThickness
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.event.block.BlockFallEvent
import org.chorus_oss.chorus.event.block.CauldronFilledByDrippingLiquidEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.*

class BlockPointedDripstone @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFallable(blockstate) {
    override val name: String
        get() = "Pointed Drip Stone"

    var isHanging: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.HANGING)
        set(value) {
            setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.HANGING, value)
        }

    var thickness: DripstoneThickness
        get() = getPropertyValue(CommonBlockProperties.DRIPSTONE_THICKNESS)
        set(value) {
            setPropertyValue(
                CommonBlockProperties.DRIPSTONE_THICKNESS,
                value
            )
        }

    override fun canBeActivated(): Boolean {
        return true
    }

    override val hardness: Double
        get() = 1.5

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val resistance: Double
        get() = 3.0

    override val waterloggingLevel: Int
        get() = 1

    override fun canBePushed(): Boolean {
        return false
    }

    override fun canBePulled(): Boolean {
        return false
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_RANDOM && this.thickness == DripstoneThickness.TIP) {
            val rand = Random()
            val nextDouble = rand.nextDouble()
            if (nextDouble <= 0.011377778) {
                this.grow()
            }

            drippingLiquid()
        }
        val hanging = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.HANGING)
        if (!hanging) {
            val down = down()
            if (!down.isSolid) {
                level.useBreakOn(this.position)
            }
        }
        tryDrop(hanging)
        return 0
    }

    fun tryDrop(hanging: Boolean) {
        if (!hanging) return
        var AirUp = false
        var blockUp: Block? = this.clone()
        while (blockUp!!.getSide(BlockFace.UP).id == BlockID.POINTED_DRIPSTONE) {
            blockUp = blockUp.getSide(BlockFace.UP)
        }
        if (!blockUp.getSide(BlockFace.UP).isSolid) AirUp = true
        if (AirUp) {
            val event = BlockFallEvent(this)
            instance.pluginManager.callEvent(event)
            if (event.cancelled) {
                return
            }
            var block = blockUp as BlockPointedDripstone?
            block!!.drop(CompoundTag().putBoolean("BreakOnGround", true))
            while (block!!.getSide(BlockFace.DOWN).id == BlockID.POINTED_DRIPSTONE) {
                block = block.getSide(BlockFace.DOWN) as BlockPointedDripstone?
                block!!.drop(CompoundTag().putBoolean("BreakOnGround", true))
            }
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
        val placeX = block.position.floorX
        val placeY = block.position.floorY
        val placeZ = block.position.floorZ
        val upBlockId = level.getBlockIdAt(placeX, placeY + 1, placeZ)
        val downBlockId = level.getBlockIdAt(placeX, placeY - 1, placeZ)
        if (upBlockId == BlockID.AIR && downBlockId == BlockID.AIR) return false
        /*    "up" define is exist drip stone in block above,"down" is Similarly.
              up   down
          1   yes   yes
          2   yes   no
          3   no    no
          4   no    yes
        */
        val state =
            if (upBlockId == BlockID.POINTED_DRIPSTONE) (if (downBlockId == BlockID.POINTED_DRIPSTONE) 1 else 2) else (if (downBlockId != BlockID.POINTED_DRIPSTONE) 3 else 4
                    )
        var hanging = false
        when (state) {
            1 -> {
                setMergeBlock(placeX, placeY, placeZ, false)
                setBlockThicknessStateAt(placeX, placeY + 1, placeZ, true, DripstoneThickness.MERGE)
            }

            2 -> {
                if (level.getBlockIdAt(placeX, placeY - 1, placeZ) != BlockID.AIR) {
                    if (face == BlockFace.UP) {
                        setBlockThicknessStateAt(placeX, placeY + 1, placeZ, true, DripstoneThickness.MERGE)
                        setMergeBlock(placeX, placeY, placeZ, false)
                    } else {
                        setTipBlock(placeX, placeY, placeZ, true)
                        setAddChange(placeX, placeY, placeZ, true)
                    }
                    return true
                }
                hanging = true
            }

            3 -> {
                setTipBlock(placeX, placeY, placeZ, face != BlockFace.UP)
                return true
            }

            4 -> {
                if (level.getBlockIdAt(placeX, placeY + 1, placeZ) != BlockID.AIR) {
                    if (face == BlockFace.DOWN) {
                        setMergeBlock(placeX, placeY, placeZ, true)
                        setBlockThicknessStateAt(placeX, placeY - 1, placeZ, false, DripstoneThickness.MERGE)
                    } else {
                        setTipBlock(placeX, placeY, placeZ, false)
                        setAddChange(placeX, placeY, placeZ, false)
                    }
                    return true
                }
            }
        }
        setAddChange(placeX, placeY, placeZ, hanging)

        if (state == 1) return true
        setTipBlock(placeX, placeY, placeZ, hanging)
        return true
    }

    override fun onBreak(item: Item?): Boolean {
        val x = position.floorX
        val y = position.floorY
        val z = position.floorZ
        level.setBlock(x, y, z, get(BlockID.AIR), true, true)
        val hanging = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.HANGING)
        val thickness = getPropertyValue(CommonBlockProperties.DRIPSTONE_THICKNESS)
        if (thickness == DripstoneThickness.MERGE) {
            if (!hanging) {
                setBlockThicknessStateAt(x, y + 1, z, true, DripstoneThickness.TIP)
            } else setBlockThicknessStateAt(x, y - 1, z, false, DripstoneThickness.TIP)
        }
        if (!hanging) {
            val length = getPointedDripStoneLength(x, y, z, false)
            if (length > 0) {
                val downBlock = down()
                for (i in 0..<length) {
                    level.setBlock(downBlock.down(i).position, get(BlockID.AIR), false, false)
                }
                for (i in length - 1 downTo 0) {
                    place(null, downBlock.down(i), null, BlockFace.DOWN, 0.0, 0.0, 0.0, null)
                }
            }
        }
        if (hanging) {
            val length = getPointedDripStoneLength(x, y, z, true)
            if (length > 0) {
                val upBlock = up()
                for (i in 0..<length) {
                    level.setBlock(upBlock.up(i).position, get(BlockID.AIR), false, false)
                }
                for (i in length - 1 downTo 0) {
                    place(null, upBlock.up(i), null, BlockFace.DOWN, 0.0, 0.0, 0.0, null)
                }
            }
        }

        return true
    }

    override fun onEntityFallOn(entity: Entity, fallDistance: Float) {
        if (level.gameRules.getBoolean(GameRule.FALL_DAMAGE) && this.getPropertyValue(CommonBlockProperties.DRIPSTONE_THICKNESS) == DripstoneThickness.TIP && !this.getPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.HANGING
            )
        ) {
            val jumpBoost =
                if (entity.hasEffect(EffectType.JUMP_BOOST)) Effect.get(EffectType.JUMP_BOOST).getLevel() else 0
            val damage = (fallDistance - jumpBoost) * 2 - 2
            if (damage > 0) entity.attack(EntityDamageEvent(entity, EntityDamageEvent.DamageCause.FALL, damage))
        }
    }

    override fun useDefaultFallDamage(): Boolean {
        return false
    }

    protected fun setTipBlock(x: Int, y: Int, z: Int, hanging: Boolean) {
        this.setPropertyValue(CommonBlockProperties.DRIPSTONE_THICKNESS, DripstoneThickness.TIP)
        this.setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.HANGING, hanging)
        level.setBlock(x, y, z, this, true, true)
    }

    protected fun setMergeBlock(x: Int, y: Int, z: Int, hanging: Boolean) {
        this.setPropertyValue(CommonBlockProperties.DRIPSTONE_THICKNESS, DripstoneThickness.MERGE)
        this.setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.HANGING, hanging)
        level.setBlock(x, y, z, this, true, true)
    }

    protected fun setBlockThicknessStateAt(x: Int, y: Int, z: Int, hanging: Boolean, thickness: DripstoneThickness) {
        this.setPropertyValue(CommonBlockProperties.DRIPSTONE_THICKNESS, thickness)
        this.setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.HANGING, hanging)
        val blockState = blockState
        level.setBlockStateAt(x, y, z, blockState)
    }

    protected fun getPointedDripStoneLength(x: Int, y: Int, z: Int, hanging: Boolean): Int {
        if (hanging) {
            for (j in y + 1..<level.dimensionData.maxHeight) {
                val blockId = level.getBlockIdAt(x, j, z)
                if (blockId != BlockID.POINTED_DRIPSTONE) {
                    return j - y - 1
                }
            }
        } else {
            for (j in y - 1 downTo level.dimensionData.minHeight + 1) {
                val blockId = level.getBlockIdAt(x, j, z)
                if (blockId != BlockID.POINTED_DRIPSTONE) {
                    return y - j - 1
                }
            }
        }
        return 0
    }

    protected fun setAddChange(x: Int, y: Int, z: Int, hanging: Boolean) {
        val length = getPointedDripStoneLength(x, y, z, hanging)
        val k2 = if (!hanging) -2 else 2
        val k1 = if (!hanging) -1 else 1
        if (length == 1) {
            setBlockThicknessStateAt(x, y + k1, z, hanging, DripstoneThickness.FRUSTUM)
        }
        if (length == 2) {
            setBlockThicknessStateAt(x, y + k2, z, hanging, DripstoneThickness.BASE)
            setBlockThicknessStateAt(x, y + k1, z, hanging, DripstoneThickness.FRUSTUM)
        }
        if (length >= 3) {
            setBlockThicknessStateAt(x, y + k2, z, hanging, DripstoneThickness.MIDDLE)
            setBlockThicknessStateAt(x, y + k1, z, hanging, DripstoneThickness.FRUSTUM)
        }
    }

    fun grow() {
        val face = if (this.isHanging) BlockFace.DOWN else BlockFace.UP
        val target = this.getSide(face)
        if (target.isAir) {
            this.place(null, target, null, face, 0.0, 0.0, 0.0, null)
        }
    }

    fun drippingLiquid() { //features according to https://zh.minecraft.wiki/w/%E6%BB%B4%E6%B0%B4%E7%9F%B3%E9%94%A5
        if (getLevelBlock(1) is BlockLiquid || this.thickness != DripstoneThickness.TIP || !this.isHanging) {
            return
        }
        var highestPDS: Block? = this
        var height = 1
        while (highestPDS!!.getSide(BlockFace.UP) is BlockPointedDripstone) {
            highestPDS = highestPDS.getSide(BlockFace.UP)
            height++
        }

        var isWaterloggingBlock = false
        if (height >= 11 ||
            !(highestPDS.getSide(BlockFace.UP, 2) is BlockLiquid ||
                    highestPDS.getSide(BlockFace.UP, 2).getLevelBlockAtLayer(1) is BlockFlowingWater)
        ) {
            return
        }

        if (highestPDS.getSide(BlockFace.UP, 2).getLevelBlockAtLayer(1) is BlockFlowingWater) {
            isWaterloggingBlock = true
        }

        var tmp: Block? = this
        val cauldron: BlockCauldron?
        while (tmp!!.getSide(BlockFace.DOWN) is BlockAir) {
            tmp = tmp.getSide(BlockFace.DOWN)
        }
        if (tmp.getSide(BlockFace.DOWN) is BlockCauldron) {
            cauldron = tmp.getSide(BlockFace.DOWN) as BlockCauldron?
        } else {
            return
        }

        val rand = Random()
        val nextDouble: Double
        val filledWith = if (isWaterloggingBlock) highestPDS.getSideAtLayer(1, BlockFace.UP, 2) else highestPDS.getSide(
            BlockFace.UP,
            2
        )
        when (filledWith.id) {
            BlockID.FLOWING_LAVA -> {
                nextDouble = rand.nextDouble()
                if ((cauldron!!.cauldronLiquid == CauldronLiquid.LAVA || cauldron.isEmpty) && cauldron.fillLevel < 6 && nextDouble <= 15.0 / 256.0) {
                    val event = CauldronFilledByDrippingLiquidEvent(cauldron, CauldronLiquid.LAVA, 1)
                    instance.pluginManager.callEvent(event)
                    if (event.cancelled) return
                    cauldron.cauldronLiquid = event.liquid
                    cauldron.fillLevel = cauldron.fillLevel + event.liquidLevelIncrement
                    cauldron.level.setBlock(cauldron.position, cauldron, true, true)
                    level.addSound(position.add(0.5, 1.0, 0.5), Sound.CAULDRON_DRIP_LAVA_POINTED_DRIPSTONE)
                }
            }

            BlockID.FLOWING_WATER -> {
                nextDouble = rand.nextDouble()
                if ((cauldron!!.cauldronLiquid == CauldronLiquid.WATER || cauldron.isEmpty) && cauldron.fillLevel < 6 && nextDouble <= 45.0 / 256.0) {
                    val event = CauldronFilledByDrippingLiquidEvent(cauldron, CauldronLiquid.WATER, 1)
                    instance.pluginManager.callEvent(event)
                    if (event.cancelled) return
                    cauldron.cauldronLiquid = event.liquid
                    cauldron.fillLevel = cauldron.fillLevel + event.liquidLevelIncrement
                    cauldron.level.setBlock(cauldron.position, cauldron, true, true)
                    level.addSound(position.add(0.5, 1.0, 0.5), Sound.CAULDRON_DRIP_WATER_POINTED_DRIPSTONE)
                }
            }
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.POINTED_DRIPSTONE,
            CommonBlockProperties.DRIPSTONE_THICKNESS,
            CommonBlockProperties.HANGING
        )
    }
}
