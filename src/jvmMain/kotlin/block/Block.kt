package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.type.BlockPropertyType
import org.chorus_oss.chorus.block.property.type.BlockPropertyType.BlockPropertyValue
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.event.player.PlayerInteractEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.customitem.ItemCustomTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Level.Companion.canRandomTick
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.MovingObjectPosition
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.IVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.tags.BlockTags.getTagSet
import org.chorus_oss.chorus.utils.BlockColor
import org.chorus_oss.chorus.utils.Loggable
import java.util.*
import java.util.function.Predicate
import kotlin.math.pow


abstract class Block(blockState: BlockState) : Locator(0.0, 0.0, 0.0, Server.instance.defaultLevel!!), AxisAlignedBB,
    IVector3, Loggable {

    var color: BlockColor = BlockColor.VOID_BLOCK_COLOR
        get() {
            if (field == BlockColor.VOID_BLOCK_COLOR) {
                field = VANILLA_BLOCK_COLOR_MAP[blockState.blockStateHash().toLong()] ?: BlockColor.VOID_BLOCK_COLOR
                if (field == BlockColor.VOID_BLOCK_COLOR) {
                    log.error("Failed to get color of block $name")
                    log.error("Current block state hash: ${blockState.blockStateHash()}")
                }
            }
            return field
        }

    open val frictionFactor: Double = 0.6

    @JvmField
    var layer: Int = 0

    /**
     * The properties that fully describe all possible and valid states that this block can have.
     */
    abstract val properties: BlockProperties

    var blockState: BlockState

    private val resolvedBlockState: BlockState by lazy {
        blockState.takeIf { properties.containBlockState(it) } ?: properties.defaultState
    }

    init {
        this.blockState = resolvedBlockState
    }

    //http://minecraft.wiki/w/Breaking
    open fun canHarvestWithHand(): Boolean {  //used for calculating breaking time
        return true
    }

    open fun tickRate(): Int {
        return 10
    }

    /**
     * Place and initialize sa this block correctly in the world.
     *
     * The current instance must have level, x, y, z, and layer properties already set before calling this method.
     *
     * @param item   The item being used to place the block. Should be used as an optional reference, may mismatch the block that is being placed depending on plugin implementations.
     * @param block  The current block that is in the world and is getting replaced by this instance. It has the same x, y, z, layer, and level as this block.
     * @param target The block that was clicked to create the place action in this block position.
     * @param face   The face that was clicked in the target block
     * @param fx     The detailed this.position.x coordinate of the clicked target block face
     * @param fy     The detailed this.position.y coordinate of the clicked target block face
     * @param fz     The detailed this.position.z coordinate of the clicked target block face
     * @param player The player that is placing the block. May be null.
     * @return `true` if the block was properly place. The implementation is responsible for reverting any partial change.
     */
    open fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        return level.setBlock(this.position, this, true, true)
    }

    open fun onBreak(item: Item?): Boolean {
        return level.setBlock(this.position, layer, get(BlockID.AIR), true, true)
    }

    /**
     * When the player break block with canSilkTouch enchantment and the canSilkTouch=true,
     * the drop will be set to the original item [Block.toItem]
     */
    open fun canSilkTouch(): Boolean {
        return false
    }

    open fun isSilkTouch(vector: Vector3?, layer: Int, face: BlockFace?, item: Item?, player: Player?): Boolean {
        return false
    }

    open fun onUpdate(type: Int): Int {
        return 0
    }

    open fun onTouch(
        vector: Vector3,
        item: Item,
        face: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float,
        player: Player?,
        action: PlayerInteractEvent.Action
    ) {
        onUpdate(Level.BLOCK_UPDATE_TOUCH)
    }

    open fun onNeighborChange(side: BlockFace) {
    }

    open fun isBreakable(vector: Vector3, layer: Int, face: BlockFace?, item: Item?, player: Player?): Boolean {
        return true
    }

    /**
     * When right-clicked on the block, [.canBeActivated]=true will only be called
     *
     * @param item      used item
     * @param player    player for Activator
     * @param blockFace a direction the player is facing
     * @param fx        the fx at block
     * @param fy        the fy at block
     * @param fz        the fz at block
     * @return the boolean
     */
    open fun onActivate(item: Item, player: Player?, blockFace: BlockFace, fx: Float, fy: Float, fz: Float): Boolean {
        return false
    }

    open fun afterRemoval(newBlock: Block, update: Boolean) {
    }

    open val isSoulSpeedCompatible: Boolean
        get() = false

    open val hardness: Double
        /**
         * Define the block hardness
         */
        get() = 10.0

    open val resistance: Double
        /**
         * Defines the block explosion resistance
         */
        get() = 1.0

    open val burnChance: Int
        /**
         * 这个值越大，这个方块本身越容易起火
         * 返回-1,这个方块不能被点燃
         *
         *
         * The higher this value, the more likely the block itself is to catch fire
         *
         * @return the burn chance
         */
        get() = 0

    open val burnAbility: Int
        /**
         * 这个值越大，越有可能被旁边的火焰引燃
         *
         *
         * The higher this value, the more likely it is to be ignited by the fire next to it
         */
        get() = 0

    open val toolType: Int
        /**
         * 控制挖掘方块的工具类型
         *
         * @return 挖掘方块的工具类型
         */
        get() = ItemTool.TYPE_NONE

    open val passableBlockFrictionFactor: Double
        /**
         * 控制方块的通过阻力因素（0-1）。此值越小阻力越大
         *
         *
         * 对于不可穿过的方块，若未覆写，此值始终为1（无效）
         *
         *
         */
        get() {
            if (!this.canPassThrough()) return 1.0
            return DEFAULT_AIR_FLUID_FRICTION
        }

    open val walkThroughExtraCost: Int
        /**
         * 获取走过这个方块所需要的额外代价，通常用于水、浆果丛等难以让实体经过的方块
         *
         * @return 走过这个方块所需要的额外代价
         */
        get() = 0

    open val lightLevel: Int
        /**
         * 控制方块的发光等级
         *
         * @return 发光等级(0 - 15)
         */
        get() = 0

    open fun canBeReplaced(): Boolean {
        return false
    }

    open val isTransparent: Boolean
        /**
         * Control whether the block is transparent (default is false)
         *
         * @return 方块是否透明
         */
        get() = false

    open val isSolid: Boolean
        get() = true

    /**
     * Check if blocks can be attached in the given side.
     */
    open fun isSolid(side: BlockFace): Boolean {
        return isSideFull(side)
    }

    // https://minecraft.wiki/w/Opacity#Lighting
    open fun diffusesSkyLight(): Boolean {
        return false
    }

    open fun canBeFlowedInto(): Boolean {
        return false
    }

    open val waterloggingLevel: Int
        get() = 0

    fun canWaterloggingFlowInto(): Boolean {
        return canBeFlowedInto() || waterloggingLevel > 1
    }

    open fun canBeActivated(): Boolean {
        return false
    }

    open fun hasEntityCollision(): Boolean {
        return false
    }

    open fun canPassThrough(): Boolean {
        return false
    }

    /**
     * @return 方块是否可以被活塞推动
     */
    open fun canBePushed(): Boolean {
        return true
    }

    /**
     * @return 方块是否可以被活塞拉动
     */
    open fun canBePulled(): Boolean {
        return true
    }

    /**
     * @return 当被活塞移动时是否会被破坏
     */
    open fun breaksWhenMoved(): Boolean {
        return false
    }

    /**
     * @return 是否可以粘在粘性活塞上
     */
    open fun sticksToPiston(): Boolean {
        return true
    }

    /**
     * @return 被活塞移动的时候是否可以粘住其他方块。eg:粘液块，蜂蜜块
     */
    open fun canSticksBlock(): Boolean {
        return false
    }

    open fun hasComparatorInputOverride(): Boolean {
        return false
    }

    open val comparatorInputOverride: Int
        get() = 0

    open fun canHarvest(item: Item): Boolean {
        return (toolTier == 0 || toolType == 0) ||
                (correctTool0(toolType, item, this) && item.tier >= toolTier)
    }

    open val toolTier: Int
        /**
         * 控制挖掘方块的最低工具级别(木质、石质...)
         *
         * @return 挖掘方块的最低工具级别
         */
        get() = 0

    open fun canBeClimbed(): Boolean {
        return false
    }

    open val name: String
        get() {
            val path =
                blockState.identifier.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            val result = StringBuilder()
            val parts =
                path.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (part in parts) {
                if (part.isNotEmpty()) {
                    result.append(part[0].uppercaseChar()).append(part.substring(1)).append(" ")
                }
            }
            return result.toString().trim { it <= ' ' }
        }

    val id: String
        get() = properties.identifier

    open val itemId: String
        get() = id

    val propertyValues: MutableList<BlockPropertyValue<*, *, *>>
        get() = blockState.blockPropertyValues

    val isAir: Boolean
        get() = this.blockState === BlockAir.properties.defaultState

    fun `is`(blockTag: String?): Boolean {
        return getTagSet(this.id).contains(blockTag)
    }

    fun <DATATYPE, PROPERTY : BlockPropertyType<DATATYPE>> getPropertyValue(p: PROPERTY): DATATYPE {
        return blockState.getPropertyValue(p)
    }

    fun <DATATYPE, PROPERTY : BlockPropertyType<DATATYPE>> setPropertyValue(
        property: PROPERTY,
        value: DATATYPE
    ): Block {
        this.blockState = blockState.setPropertyValue(properties, property, value)
        return this
    }

    fun setPropertyValue(propertyValue: BlockPropertyValue<*, *, *>): Block {
        this.blockState = blockState.setPropertyValue(properties, propertyValue)
        return this
    }

    fun setPropertyValues(vararg values: BlockPropertyValue<*, *, *>): Block {
        this.blockState = blockState.setPropertyValues(properties, *values)
        return this
    }

    fun setPropertyValues(values: MutableList<BlockPropertyValue<*, *, *>>): Block {
        this.blockState = blockState.setPropertyValues(
            properties, *values.toTypedArray()
        )
        return this
    }

    val runtimeId: Int
        get() = blockState.blockStateHash()

    open fun addVelocityToEntity(entity: Entity?, vector: Vector3?) {
    }

    fun position(v: Locator) {
        position.x = v.position.x.toInt().toDouble()
        position.y = v.position.y.toInt().toDouble()
        position.z = v.position.z.toInt().toDouble()
        this.level = v.level
    }

    private fun toolBreakTimeBonus0(item: Item): Double {
        return if (item is ItemCustomTool && item.speed != null) {
            customToolBreakTimeBonus(customToolType(item), item.speed)
        } else toolBreakTimeBonus0(
            customToolType(item), item.tier,
            id
        )
    }

    private fun customToolBreakTimeBonus(toolType: Int, speed: Int?): Double {
        if (speed != null) return speed.toDouble()
        else if (toolType == ItemTool.TYPE_SWORD) {
            return when (this) {
                is BlockWeb -> 15.0
                is BlockBamboo -> 30.0
                else -> 1.0
            }
        } else if (toolType == ItemTool.TYPE_SHEARS) {
            return when (this) {
                is BlockWool, is BlockLeaves -> 5.0
                is BlockWeb -> 15.0
                else -> 1.0
            }
        } else if (toolType == ItemTool.TYPE_NONE) return 1.0
        return 0.0
    }

    private fun customToolType(item: Item): Int {
        if (this is BlockLeaves && item.isHoe) return ItemTool.TYPE_SHEARS
        if (item.isSword) return ItemTool.TYPE_SWORD
        if (item.isShovel) return ItemTool.TYPE_SHOVEL
        if (item.isPickaxe) return ItemTool.TYPE_PICKAXE
        if (item.isAxe) return ItemTool.TYPE_AXE
        if (item.isHoe) return ItemTool.TYPE_HOE
        if (item.isShears) return ItemTool.TYPE_SHEARS
        return ItemTool.TYPE_NONE
    }

    private fun toolBreakTimeBonus0(toolType: Int, toolTier: Int, blockId: String): Double {
        if (toolType == ItemTool.TYPE_SWORD) {
            if (blockId == BlockID.WEB) {
                return 15.0
            }
            if (blockId == BlockID.BAMBOO) {
                return 30.0
            }
            return 1.0
        }
        if (toolType == ItemTool.TYPE_SHEARS) {
            if (this is BlockWool || this is BlockLeaves) {
                return 5.0
            } else if (blockId == BlockID.WEB) {
                return 15.0
            }
            return 1.0
        }
        if (toolType == ItemTool.TYPE_NONE) return 1.0
        return when (toolTier) {
            ItemTool.TIER_WOODEN -> 2.0
            ItemTool.TIER_STONE -> 4.0
            ItemTool.TIER_IRON -> 6.0
            ItemTool.TIER_DIAMOND -> 8.0
            ItemTool.TIER_NETHERITE -> 9.0
            ItemTool.TIER_GOLD -> 12.0
            else -> 1.0
        }
    }

    fun getBreakTime(item: Item, player: Player?): Double {
        return this.calculateBreakTime(item, player)
    }

    fun getBreakTime(item: Item): Double {
        return this.calculateBreakTime(item)
    }

    /**
     * @link calculateBreakTime(@ Nonnull Item item, @ Nullable Player player)
     */
    open fun calculateBreakTime(item: Item): Double {
        return calculateBreakTime(item, null)
    }

    /**
     * 计算方块挖掘时间
     *
     * @param item   挖掘该方块的物品
     * @param player 挖掘该方块的玩家
     * @return 方块的挖掘时间
     */
    open fun calculateBreakTime(item: Item, player: Player?): Double {
        var seconds = this.calculateBreakTimeNotInAir(item, player)

        if (player != null) {
            //玩家距离上次在空中过去5tick之后，才认为玩家是在地上挖掘。
            //如果单纯用onGround检测，这个方法返回的时间将会不连续。
            if (player.level!!.tick - player.lastInAirTick < 5) {
                seconds *= 5.0
            }
        }
        return seconds
    }

    /**
     * 忽略玩家在空中时，计算方块的挖掘时间
     *
     * @param item   挖掘该方块的物品
     * @param player 挖掘该方块的玩家
     * @return 方块的挖掘时间
     */
    fun calculateBreakTimeNotInAir(item: Item, player: Player?): Double {
        var seconds: Double
        val blockHardness = hardness
        val canHarvest = canHarvest(item)

        seconds = if (canHarvest) {
            blockHardness * 1.5
        } else {
            blockHardness * 5
        }

        var speedMultiplier = 1.0
        var hasConduitPower = false
        var hasAquaAffinity = false
        var hasteEffectLevel = 0
        var miningFatigueLevel = 0

        if (player != null) {
            hasConduitPower = player.hasEffect(EffectType.CONDUIT_POWER)
            hasAquaAffinity =
                Optional.ofNullable(player.inventory.helmet.getEnchantment(Enchantment.ID_WATER_WORKER))
                    .map(Enchantment::level).map { l: Int -> l >= 1 }
                    .orElse(false)
            hasteEffectLevel = Optional.ofNullable(player.getEffect(EffectType.HASTE))
                .map { obj: Effect -> obj.getAmplifier() }.orElse(0)
            miningFatigueLevel = Optional.ofNullable(player.getEffect(EffectType.MINING_FATIGUE))
                .map { obj: Effect -> obj.getAmplifier() }.orElse(0)
        }

        if (correctTool0(toolType, item, this)) {
            speedMultiplier = toolBreakTimeBonus0(item)

            val efficiencyLevel = Optional.ofNullable(item.getEnchantment(Enchantment.ID_EFFICIENCY))
                .map(Enchantment::level).orElse(0)

            if (canHarvest && efficiencyLevel > 0) {
                speedMultiplier += (efficiencyLevel * efficiencyLevel + 1).toDouble()
            }

            if (hasConduitPower) hasteEffectLevel = Integer.max(hasteEffectLevel, 2)

            if (hasteEffectLevel > 0) {
                speedMultiplier *= 1 + (0.2 * hasteEffectLevel)
            }
        }

        if (miningFatigueLevel > 0) {
            speedMultiplier /= miningFatigueLevel.toDouble().pow(3.0)
        }

        seconds /= speedMultiplier

        if (player != null) {
            if (player.isInsideOfWater() && !hasAquaAffinity) {
                seconds *= if (hasConduitPower && blockHardness >= 0.5) 2.5 else 5.0
            }
        }
        return seconds
    }

    fun canBeBrokenWith(item: Item?): Boolean {
        return this.hardness != -1.0
    }

    fun getTickCachedSide(face: BlockFace): Block {
        return getTickCachedSideAtLayer(layer, face)
    }

    fun getTickCachedSide(face: BlockFace, step: Int): Block {
        return getTickCachedSideAtLayer(layer, face, step)
    }

    fun getTickCachedSideAtLayer(layer: Int, face: BlockFace): Block {
        return level.getTickCachedBlock(
            position.x.toInt() + face.xOffset,
            position.y.toInt() + face.yOffset, position.z.toInt() + face.zOffset, layer
        )
    }

    fun getTickCachedSideAtLayer(layer: Int, face: BlockFace, step: Int): Block {
        if (step == 1) {
            return level.getTickCachedBlock(
                position.x.toInt() + face.xOffset,
                position.y.toInt() + face.yOffset, position.z.toInt() + face.zOffset, layer
            )
        }
        return level.getTickCachedBlock(
            position.x.toInt() + face.xOffset * step,
            position.y.toInt() + face.yOffset * step, position.z.toInt() + face.zOffset * step, layer
        )
    }

    override fun getSide(face: BlockFace): Block {
        return getSideAtLayer(layer, face)
    }

    override fun getSide(face: BlockFace, step: Int): Block {
        return getSideAtLayer(layer, face, step)
    }

    fun getSideAtLayer(layer: Int, face: BlockFace): Block {
        return level.getBlock(
            position.x.toInt() + face.xOffset,
            position.y.toInt() + face.yOffset, position.z.toInt() + face.zOffset, layer
        )
    }

    fun getSideAtLayer(layer: Int, face: BlockFace, step: Int): Block {
        if (step == 1) {
            return level.getBlock(
                position.x.toInt() + face.xOffset,
                position.y.toInt() + face.yOffset, position.z.toInt() + face.zOffset, layer
            )
        }
        return level.getBlock(
            position.x.toInt() + face.xOffset * step,
            position.y.toInt() + face.yOffset * step, position.z.toInt() + face.zOffset * step, layer
        )
    }

    @JvmOverloads
    fun up(step: Int = 1): Block {
        return getSide(BlockFace.UP, step)
    }

    fun up(step: Int, layer: Int): Block {
        return getSideAtLayer(layer, BlockFace.UP, step)
    }

    @JvmOverloads
    fun down(step: Int = 1): Block {
        return getSide(BlockFace.DOWN, step)
    }

    fun down(step: Int, layer: Int): Block {
        return getSideAtLayer(layer, BlockFace.DOWN, step)
    }

    @JvmOverloads
    fun north(step: Int = 1): Block {
        return getSide(BlockFace.NORTH, step)
    }

    fun north(step: Int, layer: Int): Block {
        return getSideAtLayer(layer, BlockFace.NORTH, step)
    }

    @JvmOverloads
    fun south(step: Int = 1): Block {
        return getSide(BlockFace.SOUTH, step)
    }

    fun south(step: Int, layer: Int): Block {
        return getSideAtLayer(layer, BlockFace.SOUTH, step)
    }

    @JvmOverloads
    fun east(step: Int = 1): Block {
        return getSide(BlockFace.EAST, step)
    }

    fun east(step: Int, layer: Int): Block {
        return getSideAtLayer(layer, BlockFace.EAST, step)
    }

    @JvmOverloads
    fun west(step: Int = 1): Block {
        return getSide(BlockFace.WEST, step)
    }

    fun west(step: Int, layer: Int): Block {
        return getSideAtLayer(layer, BlockFace.WEST, step)
    }

    override fun toString(): String {
        return blockState.toString() + " at " + super.toString()
    }

    open fun collidesWithBB(bb: AxisAlignedBB): Boolean {
        return collidesWithBB(bb, false)
    }

    fun collidesWithBB(bb: AxisAlignedBB, collisionBB: Boolean): Boolean {
        val bb1 = if (collisionBB) collisionBoundingBox else boundingBox
        return bb1 != null && bb.intersectsWith(bb1)
    }

    open fun onEntityCollide(entity: Entity) {
    }

    open fun onEntityFallOn(entity: Entity, fallDistance: Float) {
    }

    open fun useDefaultFallDamage(): Boolean {
        return true
    }

    open val boundingBox: AxisAlignedBB?
        get() = this.recalculateBoundingBox()

    open val collisionBoundingBox: AxisAlignedBB?
        get() = this.recalculateCollisionBoundingBox()

    protected open fun recalculateBoundingBox(): AxisAlignedBB? {
        return this
    }

    override var minX: Double
        get() = position.x
        set(minX) {
            throw UnsupportedOperationException("Immutable")
        }

    override var minY: Double
        get() = position.y
        set(minY) {
            throw UnsupportedOperationException("Immutable")
        }

    override var minZ: Double
        get() = position.z
        set(minZ) {
            throw UnsupportedOperationException("Immutable")
        }

    override var maxX: Double
        get() = position.x + 1
        set(maxX) {
            throw UnsupportedOperationException("Immutable")
        }

    override var maxY: Double
        get() = position.y + 1
        set(maxY) {
            throw UnsupportedOperationException("Immutable")
        }

    override var maxZ: Double
        get() = position.z + 1
        set(maxZ) {
            throw UnsupportedOperationException("Immutable")
        }

    protected open fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
        return boundingBox
    }

    override fun calculateIntercept(pos1: Vector3, pos2: Vector3): MovingObjectPosition? {
        val bb = this.boundingBox ?: return null

        var v1 = pos1.getIntermediateWithXValue(pos2, bb.minX)
        var v2 = pos1.getIntermediateWithXValue(pos2, bb.maxX)
        var v3 = pos1.getIntermediateWithYValue(pos2, bb.minY)
        var v4 = pos1.getIntermediateWithYValue(pos2, bb.maxY)
        var v5 = pos1.getIntermediateWithZValue(pos2, bb.minZ)
        var v6 = pos1.getIntermediateWithZValue(pos2, bb.maxZ)

        if (v1 != null && !bb.isVectorInYZ(v1)) {
            v1 = null
        }

        if (v2 != null && !bb.isVectorInYZ(v2)) {
            v2 = null
        }

        if (v3 != null && !bb.isVectorInXZ(v3)) {
            v3 = null
        }

        if (v4 != null && !bb.isVectorInXZ(v4)) {
            v4 = null
        }

        if (v5 != null && !bb.isVectorInXY(v5)) {
            v5 = null
        }

        if (v6 != null && !bb.isVectorInXY(v6)) {
            v6 = null
        }

        var vector = v1

        if (v2 != null && (vector == null || pos1.distanceSquared(v2) < pos1.distanceSquared(vector))) {
            vector = v2
        }

        if (v3 != null && (vector == null || pos1.distanceSquared(v3) < pos1.distanceSquared(vector))) {
            vector = v3
        }

        if (v4 != null && (vector == null || pos1.distanceSquared(v4) < pos1.distanceSquared(vector))) {
            vector = v4
        }

        if (v5 != null && (vector == null || pos1.distanceSquared(v5) < pos1.distanceSquared(vector))) {
            vector = v5
        }

        if (v6 != null && (vector == null || pos1.distanceSquared(v6) < pos1.distanceSquared(vector))) {
            vector = v6
        }

        if (vector == null) {
            return null
        }

        var f: BlockFace? = null

        if (vector === v1) {
            f = BlockFace.WEST
        } else if (vector === v2) {
            f = BlockFace.EAST
        } else if (vector === v3) {
            f = BlockFace.DOWN
        } else if (vector === v4) {
            f = BlockFace.UP
        } else if (vector === v5) {
            f = BlockFace.NORTH
        } else if (vector === v6) {
            f = BlockFace.SOUTH
        }

        return MovingObjectPosition.fromBlock(
            position.x.toInt(),
            position.y.toInt(), position.z.toInt(), f,
            vector.add(position.x, position.y, position.z)
        )
    }

    val saveId: String
        get() {
            val name = javaClass.name
            return name.substring(16)
        }

    override fun clone(): Block {
        return super.clone() as Block
    }

    open fun getWeakPower(face: BlockFace): Int {
        return 0
    }

    /**
     * Gets strong power.
     *
     * @param side the side
     * @return the strong power
     */
    open fun getStrongPower(side: BlockFace): Int {
        return 0
    }

    open val isPowerSource: Boolean
        get() = false

    val locationHash: String
        get() = position.floorX.toString() + ":" + position.floorY + ":" + position.floorZ

    open val dropExp: Int
        get() = 0

    val isNormalBlock: Boolean
        /**
         * Check if the block is not transparent, is solid and can't provide redstone power.
         */
        get() = !isTransparent && isSolid && !isPowerSource

    val isSimpleBlock: Boolean
        /**
         * Check if the block is not transparent, is solid and is a full cube like a stone block.
         */
        get() = !isTransparent && isSolid && isFullBlock

    /**
     * Check if the given face is fully occupied by the block bounding box.
     *
     * @param face The face to be checked
     * @return If and only if the bounding box completely cover the face
     */
    fun isSideFull(face: BlockFace): Boolean {
        val boundingBox = boundingBox ?: return false

        if (face.axis.plane == BlockFace.Plane.HORIZONTAL) {
            if (boundingBox.minY != y || boundingBox.maxY != y + 1) {
                return false
            }
            var offset = face.xOffset
            if (offset < 0) {
                return boundingBox.minX == x && boundingBox.minZ == z && boundingBox.maxZ == z + 1
            } else if (offset > 0) {
                return boundingBox.maxX == x + 1 && boundingBox.maxZ == z + 1 && boundingBox.minZ == z
            }

            offset = face.zOffset
            if (offset < 0) {
                return boundingBox.minZ == z && boundingBox.minX == x && boundingBox.maxX == x + 1
            }

            return boundingBox.maxZ == z + 1 && boundingBox.maxX == x + 1 && boundingBox.minX == x
        }

        if (boundingBox.minX != x || boundingBox.maxX != x + 1 || boundingBox.minZ != z || boundingBox.maxZ != z + 1) {
            return false
        }

        if (face.yOffset < 0) {
            return boundingBox.minY == y
        }

        return boundingBox.maxY == y + 1
    }

    open val isFertilizable: Boolean
        get() = false

    val isFullBlock: Boolean
        /**
         * Check if the block occupies the entire block space, like a stone and normal glass blocks
         */
        get() {
            val boundingBox = boundingBox ?: return false
            return boundingBox.minX == x && boundingBox.maxX == x + 1 && boundingBox.minY == y && boundingBox.maxY == y + 1 && boundingBox.minZ == z && boundingBox.maxZ == z + 1
        }

    /**
     * Compare whether two blocks are the same, this method compares block entities
     *
     * @param obj the obj
     * @return the boolean
     */
    fun equalsBlock(obj: Any): Boolean {
        if (obj is Block) {
            if (this !is BlockEntityHolder<*> && obj !is BlockEntityHolder<*>) {
                return this.id == obj.id && this.blockState === obj.blockState
            }
            if (this is BlockEntityHolder<*> && obj is BlockEntityHolder<*>) {
                val be1: BlockEntity = this.getOrCreateBlockEntity()
                val be2: BlockEntity = obj.getOrCreateBlockEntity()
                return this.id == obj.id && this.blockState === obj.blockState && be1.cleanedNBT!! == be2.cleanedNBT
            }
        }
        return false
    }

    val isDefaultState: Boolean
        get() = this.blockState === properties.defaultState

    open fun toItem(): Item {
        return ItemBlock(properties.defaultState, name)
    }

    /**
     * Control the item dropped when a block is broken normally
     *
     * @return An array of dropped items
     */
    open fun getDrops(item: Item): Array<Item> {
        if (canHarvestWithHand() || canHarvest(item)) {
            return arrayOf(
                this.toItem()
            )
        }
        return Item.EMPTY_ARRAY
    }

    fun firstInLayers(condition: Predicate<Block?>): Optional<Block> {
        return firstInLayers(0, condition)
    }

    fun firstInLayers(startingLayer: Int, condition: Predicate<Block?>): Optional<Block> {
        val maximumLayer = level.requireProvider().maximumLayer
        for (layer in startingLayer..maximumLayer) {
            val block = this.getLevelBlockAtLayer(layer)
            if (condition.test(block)) {
                return Optional.of(block)
            }
        }

        return Optional.empty<Block?>()
    }

    val isBlockChangeAllowed: Boolean
        get() = chunk.isBlockChangeAllowed(
            position.floorX and 0xF,
            position.floorY,
            position.floorZ and 0xF
        )

    fun isBlockChangeAllowed(player: Player?): Boolean {
        if (isBlockChangeAllowed) {
            val height = (this.y - 1).toInt()
            for (i in height downTo level.minHeight) {
                val block = this.down(height - i)
                if (block is BlockAllow) {
                    return true
                } else if (block is BlockDeny) {
                    return player == null || player.isCreative
                }
            }
            if (player == null) return true

            if (player.isAdventure) {
                val itemInHand = player.inventory.itemInHand
                if (itemInHand.isNothing) return false

                val tag = itemInHand.getNamedTagEntry("CanDestroy")
                var canBreak = false
                if (tag is ListTag<*>) {
                    for (v in (tag as ListTag<out Tag<*>>).all) {
                        if (v !is StringTag) {
                            continue
                        }
                        val entry = Item.get(v.data)
                        if (!entry.isNothing &&
                            entry.getSafeBlockState().identifier == this.id
                        ) {
                            canBreak = true
                            break
                        }
                    }
                }

                return canBreak
            } else {
                return true
            }
        }
        return player != null && player.isCreative && player.isOp
    }

    open val lightFilter: Int
        /**
         * 控制方块吸收的光亮
         *
         * @return 方块吸收的光亮
         */
        get() = if (isSolid && !isTransparent) 15 else 1

    fun canRandomTick(): Boolean {
        return canRandomTick(id)
    }

    open fun onProjectileHit(projectile: Entity, locator: Locator, motion: Vector3): Boolean {
        return false
    }

    open val isGettingPower: Boolean
        /**
         * Check if a block is getting powered threw a block or directly.
         *
         * @return if the gets powered.
         */
        get() {
            if (!Server.instance.settings.levelSettings.enableRedstone) return false

            for (side in BlockFace.entries) {
                val b = getSide(side).levelBlock

                if (level.isSidePowered(b.position, side)) {
                    return true
                }
            }
            return level.isBlockPowered(this.position)
        }

    open fun cloneTo(pos: Locator): Boolean {
        return cloneTo(pos, true)
    }

    /**
     * 将方块克隆到指定位置
     *
     *
     * 此方法会连带克隆方块实体
     *
     *
     * 注意，此方法会先清除指定位置的方块为空气再进行克隆
     *
     * @param pos    要克隆到的位置
     * @param update 是否需要更新克隆的方块
     * @return 是否克隆成功
     */
    fun cloneTo(pos: Locator, update: Boolean): Boolean {
        //清除旧方块
        level.setBlock(pos.position, this.layer, get(BlockID.AIR), direct = false, update = false)
        if (this is BlockEntityHolder<*> && this.blockEntity != null) {
            val clonedBlock = this.clone()
            clonedBlock.position(pos)
            val tag: CompoundTag? = this.blockEntity!!.cleanedNBT
            //方块实体要求direct=true
            return BlockEntityHolder.setBlockAndCreateEntity(
                clonedBlock as BlockEntityHolder<*>,
                true,
                update,
                tag
            ) != null
        } else {
            return pos.level.setBlock(pos.position, this.layer, this.clone(), true, update)
        }
    }

    override fun hashCode(): Int {
        return (position.x.toInt() xor (position.z.toInt() shl 12)) xor ((position.y + 64).toInt() shl 23)
    }

    companion object {
        val EMPTY_ARRAY: Array<Block> = emptyArray()
        const val DEFAULT_AIR_FLUID_FRICTION: Double = 0.95
        val VANILLA_BLOCK_COLOR_MAP: HashMap<Long, BlockColor> = HashMap()

        @JvmStatic
        fun isNotActivate(player: Player?): Boolean {
            if (player == null) {
                return true
            }
            val itemInHand = player.inventory.itemInHand
            return (player.isSneaking() || player.isFlySneaking) && !(itemInHand.isTool || itemInHand.isNothing)
        }

        @JvmStatic
        fun get(id: String): Block {
            var id1 = id
            id1 = if (id1.contains(":")) id1 else "minecraft:$id1"
            val block = Registries.BLOCK.get(id1) ?: return BlockAir()
            return block
        }

        @JvmStatic
        fun get(id: String, pos: Locator): Block {
            var id1 = id
            id1 = if (id1.contains(":")) id1 else "minecraft:$id1"
            val block =
                Registries.BLOCK.get(id1, pos.position.floorX, pos.position.floorY, pos.position.floorZ, pos.level)
            if (block == null) {
                val blockAir = BlockAir()
                blockAir.position.x = pos.position.floorX.toDouble()
                blockAir.position.y = pos.position.floorY.toDouble()
                blockAir.position.z = pos.position.floorZ.toDouble()
                blockAir.level = pos.level
                return blockAir
            }
            return block
        }

        fun get(id: String, pos: Locator, layer: Int): Block {
            var id1 = id
            id1 = if (id1.contains(":")) id1 else "minecraft:$id1"
            val block = get(id1, pos)
            block.layer = layer
            return block
        }

        fun get(id: String, level: Level, x: Int, y: Int, z: Int): Block {
            var id1 = id
            id1 = if (id1.contains(":")) id1 else "minecraft:$id1"
            val block = Registries.BLOCK.get(id1, x, y, z, level)
            if (block == null) {
                val blockAir = BlockAir()
                blockAir.position.x = x.toDouble()
                blockAir.position.y = y.toDouble()
                blockAir.position.z = z.toDouble()
                blockAir.level = level
                return blockAir
            }
            return block
        }

        fun get(id: String, level: Level, x: Int, y: Int, z: Int, layer: Int): Block {
            var id1 = id
            id1 = if (id1.contains(":")) id1 else "minecraft:$id1"
            val block = get(id1, level, x, y, z)
            block.layer = layer
            return block
        }

        @JvmStatic
        fun get(blockState: BlockState?): Block {
            val block = Registries.BLOCK[blockState] ?: return BlockAir()
            return block
        }

        @JvmStatic
        fun get(blockState: BlockState, pos: Locator): Block {
            return get(blockState, pos.level, pos.position.floorX, pos.position.floorY, pos.position.floorZ)
        }

        fun get(blockState: BlockState, pos: Locator, layer: Int): Block {
            val block = get(blockState, pos)
            block.layer = layer
            return block
        }

        fun get(blockState: BlockState, level: Level, x: Int, y: Int, z: Int): Block {
            return get(blockState.identifier, level, x, y, z)
        }

        fun get(blockState: BlockState, level: Level, x: Int, y: Int, z: Int, layer: Int): Block {
            val block = get(blockState, level, x, y, z)
            block.layer = layer
            return block
        }

        fun getWithState(id: String, blockState: BlockState): Block {
            var id1 = id
            id1 = if (id1.contains(":")) id1 else "minecraft:$id1"
            val block = get(id1)
            blockState.blockPropertyValues.let { block.setPropertyValues(it) }
            return block
        }

        private fun speedBonusByEfficiencyLore0(efficiencyLoreLevel: Int): Double {
            if (efficiencyLoreLevel == 0) return 0.0
            return (efficiencyLoreLevel * efficiencyLoreLevel + 1).toDouble()
        }

        private fun speedRateByHasteLore0(hasteLoreLevel: Int): Double {
            return 1.0 + (0.2 * hasteLoreLevel)
        }

        private fun correctTool0(blockToolType: Int, item: Item, b: Block): Boolean {
            val block = b.id
            return if (b is BlockLeaves && item.isHoe) {
                (blockToolType == ItemTool.TYPE_SHEARS && item.isHoe)
            } else if (block == BlockID.BAMBOO && item.isSword) {
                (blockToolType == ItemTool.TYPE_AXE && item.isSword)
            } else (blockToolType == ItemTool.TYPE_SWORD && item.isSword) ||
                    (blockToolType == ItemTool.TYPE_SHOVEL && item.isShovel) ||
                    (blockToolType == ItemTool.TYPE_PICKAXE && item.isPickaxe) ||
                    (blockToolType == ItemTool.TYPE_AXE && item.isAxe) ||
                    (blockToolType == ItemTool.TYPE_HOE && item.isHoe) ||
                    (blockToolType == ItemTool.TYPE_SHEARS && item.isShears) || blockToolType == ItemTool.TYPE_NONE ||
                    (block == BlockID.WEB && item.isShears)
        }

        @JvmOverloads
        fun equals(b1: Block?, b2: Block?, checkState: Boolean = true): Boolean {
            if (b1 == null || b2 == null || (b1.id != b2.id)) {
                return false
            }
            if (checkState) {
                val b1Default = b1.isDefaultState
                val b2Default = b2.isDefaultState
                return if (b1Default != b2Default) {
                    false
                } else if (b1Default) { // both are default
                    true
                } else {
                    b1.blockState === b2.blockState
                }
            } else {
                return true
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Block

        if (this.blockState != other.blockState) return false
        if (this.position != other.position) return false
        if (this.level != other.level) return false

        return true
    }
}
