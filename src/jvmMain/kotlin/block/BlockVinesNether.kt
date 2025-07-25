package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server.Companion.instance
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.block.BlockGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.min

abstract class BlockVinesNether
/**
 * Creates a nether vine from a meta compatible with [.getProperties].
 */
    (blockstate: BlockState) : BlockTransparent(blockstate) {
    /**
     * The direction that the vine will grow, vertical direction is expected but future implementations
     * may also add horizontal directions.
     * @return Normally, up or down.
     */
    abstract val growthDirection: BlockFace

    /**
     * The current age of this block.
     */
    abstract var vineAge: Int

    /**
     * The maximum accepted age of this block.
     * @return Positive, inclusive value.
     */
    abstract val maxVineAge: Int

    /**
     * Changes the current vine age to a random new random age.
     *
     * @param pseudorandom If the the randomization should be pseudorandom.
     */
    fun randomizeVineAge(pseudorandom: Boolean) {
        if (pseudorandom) {
            vineAge = ThreadLocalRandom.current().nextInt(maxVineAge)
            return
        }

        var chance = 1.0

        val random = ThreadLocalRandom.current()
        var age = 0
        while (random.nextDouble() < chance) {
            chance *= 0.826
            ++age
        }

        vineAge = age
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
        val support = getSide(growthDirection.getOpposite())
        if (!isSupportValid(support)) {
            return false
        }

        if (support.id == id) {
            vineAge =
                min(maxVineAge.toDouble(), ((support as BlockVinesNether).vineAge + 1).toDouble())
                    .toInt()
        } else {
            randomizeVineAge(true)
        }

        return super.place(item, block, target, face, fx, fy, fz, player)
    }

    override fun onUpdate(type: Int): Int {
        when (type) {
            Level.BLOCK_UPDATE_RANDOM -> {
                val maxVineAge = maxVineAge
                if (vineAge < maxVineAge && ThreadLocalRandom.current().nextInt(10) == 0 && findVineAge(true).orElse(
                        maxVineAge
                    ) < maxVineAge
                ) {
                    grow()
                }
                return Level.BLOCK_UPDATE_RANDOM
            }

            Level.BLOCK_UPDATE_SCHEDULED -> {
                level.useBreakOn(this.position, null, null, true)
                return Level.BLOCK_UPDATE_SCHEDULED
            }

            Level.BLOCK_UPDATE_NORMAL -> {
                if (!isSupportValid) {
                    level.scheduleUpdate(this, 1)
                }
                return Level.BLOCK_UPDATE_NORMAL
            }

            else -> {
                return 0
            }
        }
    }

    /**
     * Grow a single vine if possible. Calls [BlockGrowEvent] passing the positioned new state and the source block.
     * @return If the vine grew successfully.
     */
    fun grow(): Boolean {
        val pos = getSide(growthDirection)
        if (!pos.isAir || pos.position.y < 0 || 255 < pos.position.y) {
            return false
        }

        val growing = clone()
        growing.position.x = pos.position.x
        growing.position.y = pos.position.y
        growing.position.z = pos.position.z
        growing.vineAge = min((vineAge + 1).toDouble(), maxVineAge.toDouble()).toInt()

        val ev = BlockGrowEvent(this, growing)
        instance.pluginManager.callEvent(ev)

        if (ev.cancelled) {
            return false
        }

        if (level.setBlock(pos.position, growing)) {
            increaseRootAge()
            return true
        }
        return false
    }

    /**
     * Grow a random amount of vines.
     * Calls [BlockGrowEvent] passing the positioned new state and the source block for each new vine being added
     * to the world, if one of the events gets cancelled the growth gets interrupted.
     * @return How many vines grew
     */
    fun growMultiple(): Int {
        val growthDirection = growthDirection
        var age = vineAge + 1
        val maxAge = maxVineAge
        val growing = clone()
        growing.randomizeVineAge(false)
        val blocksToGrow = growing.vineAge

        var grew = 0
        for (distance in 1..blocksToGrow) {
            val pos = getSide(growthDirection, distance)
            if (!pos.isAir || pos.position.y < 0 || 255 < pos.position.y) {
                break
            }

            growing.vineAge = min((age++).toDouble(), maxAge.toDouble()).toInt()
            growing.position.x = pos.position.x
            growing.position.y = pos.position.y
            growing.position.z = pos.position.z

            val ev = BlockGrowEvent(this, growing.clone())
            instance.pluginManager.callEvent(ev)

            if (ev.cancelled) {
                break
            }

            if (!level.setBlock(pos.position, ev.newState)) {
                break
            }

            grew++
        }

        if (grew > 0) {
            increaseRootAge()
        }

        return grew
    }

    /**
     * Attempt to get the age of the root or the head of the vine.
     * @param base True to get the age of the base (oldest block), false to get the age of the head (newest block)
     * @return Empty if the target could not be reached. The age of the target if it was found.
     */
    fun findVineAge(base: Boolean): OptionalInt {
        return findVineBlock(base)
            .map { vine: BlockVinesNether ->
                OptionalInt.of(
                    vine.vineAge
                )
            }
            .orElse(OptionalInt.empty())
    }

    /**
     * Attempt to find the root or the head of the vine transversing the growth direction for up to 256 blocks.
     * @param base True to find the base (oldest block), false to find the head (newest block)
     * @return Empty if the target could not be reached or the block there isn't an instance of [BlockVinesNether].
     * The positioned block of the target if it was found.
     */
    fun findVineBlock(base: Boolean): Optional<BlockVinesNether> {
        return findVine(base)
            .map(Locator::levelBlock)
            .filter { obj: Block? -> BlockVinesNether::class.java.isInstance(obj) }
            .map { obj: Block? -> BlockVinesNether::class.java.cast(obj) }
    }

    /**
     * Attempt to find the root or the head of the vine transversing the growth direction for up to 256 blocks.
     * @param base True to find the base (oldest block), false to find the head (newest block)
     * @return Empty if the target could not be reached. The position of the target if it was found.
     */
    fun findVine(base: Boolean): Optional<Locator> {
        var supportFace = growthDirection
        if (base) {
            supportFace = supportFace.getOpposite()
        }
        var result = locator
        val id = id
        var limit = 256
        while (--limit > 0) {
            val next = result.getSide(supportFace)
            if (next.levelBlockState.identifier == id) {
                result = next
            } else {
                break
            }
        }

        return if (limit == -1) Optional.empty() else Optional.of(result)
    }

    /**
     * Attempts to increase the age of the base of the nether vine.
     * @return
     *  * `EMPTY` if the base could not be reached or have an invalid instance type
     *  * `TRUE` if the base was changed successfully
     *  * `FALSE` if the base was already in the max age or the block change was refused
     *
     */
    fun increaseRootAge(): Boolean? {
        val base = findVine(true).map(Locator::levelBlock)
            .orElse(null) as? BlockVinesNether
            ?: return null

        val vineAge = base.vineAge
        if (vineAge < base.maxVineAge) {
            base.vineAge = vineAge + 1
            if (level.setBlock(base.position, base)) {
                return true
            }
        }

        return false
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (!item.isFertilizer) {
            return false
        }

        level.addParticle(BoneMealParticle(this.position))
        findVineBlock(false).ifPresent { obj: BlockVinesNether -> obj.growMultiple() }

        if (player != null && !player.isCreative) {
            item.count--
        }

        return true
    }

    override fun getDrops(item: Item): Array<Item> {
        // They have a 33% (3/9) chance to drop a single weeping vine when broken, 
        // increased to 55% (5/9) with Fortune I, 
        // 77% (7/9) with Fortune II, 
        // and 100% with Fortune III. 
        // 
        // They always drop a single weeping vine when broken with shears or a tool enchanted with Silk Touch.

        var enchantmentLevel = 0
        if (item.isShears || (item.getEnchantmentLevel(Enchantment.ID_FORTUNE_DIGGING)
                .also { enchantmentLevel = it }) >= 3
        ) {
            return arrayOf(toItem())
        }

        val chance = 3 + enchantmentLevel * 2
        if (ThreadLocalRandom.current().nextInt(9) < chance) {
            return arrayOf(toItem())
        }

        return Item.EMPTY_ARRAY
    }

    protected fun isSupportValid(support: Block): Boolean {
        return support.id == id || !support.isTransparent
    }

    val isSupportValid: Boolean
        get() = isSupportValid(getSide(growthDirection.getOpposite()))

    override fun onEntityCollide(entity: Entity) {
        entity.resetFallDistance()
    }

    override fun hasEntityCollision(): Boolean {
        return true
    }

    override val hardness: Double
        get() = 0.0

    override val resistance: Double
        get() = 0.0

    override fun canBeClimbed(): Boolean {
        return true
    }

    override fun canBeFlowedInto(): Boolean {
        return true
    }

    override val isSolid: Boolean
        get() = false

    override var minX: Double
        get() = position.x + (4 / 16.0)
        set(minX) {
            super.minX = minX
        }

    override var minZ: Double
        get() = position.z + (4 / 16.0)
        set(minZ) {
            super.minZ = minZ
        }

    override var maxX: Double
        get() = position.x + (12 / 16.0)
        set(maxX) {
            super.maxX = maxX
        }

    override var maxZ: Double
        get() = position.z + (12 / 16.0)
        set(maxZ) {
            super.maxZ = maxZ
        }

    override var maxY: Double
        get() = position.y + (15 / 16.0)
        set(maxY) {
            super.maxY = maxY
        }

    override fun canPassThrough(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override fun clone(): BlockVinesNether {
        return super.clone() as BlockVinesNether
    }

    override val isFertilizable: Boolean
        get() = true
}
