package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server.Companion.instance
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.event.block.LeavesDecayEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.utils.Hash.hashBlock
import java.util.concurrent.ThreadLocalRandom

abstract class BlockLeaves(blockState: BlockState) : BlockTransparent(blockState) {
    override val hardness: Double
        get() = 0.2

    override val toolType: Int
        get() = ItemTool.TYPE_HOE

    abstract fun getType(): WoodType

    override val name: String
        get() = getType().woodName + " Leaves"

    override val burnChance: Int
        get() = 30

    override val waterloggingLevel: Int
        get() = 1

    override val burnAbility: Int
        get() = 60

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
        this.isPersistent = true
        level.setBlock(this.position, this, true)
        return true
    }

    override fun getDrops(item: Item): Array<Item> {
        if (item.isShears) {
            return arrayOf(
                toItem()
            )
        }

        val drops: MutableList<Item> = ArrayList(1)
        val fortuneEnchantment = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)

        val fortune = fortuneEnchantment?.level ?: 0
        val appleOdds: Int
        val stickOdds: Int
        val saplingOdds: Int
        when (fortune) {
            0 -> {
                appleOdds = 200
                stickOdds = 50
                saplingOdds = if (getType() == WoodType.JUNGLE) 40 else 20
            }

            1 -> {
                appleOdds = 180
                stickOdds = 45
                saplingOdds = if (getType() == WoodType.JUNGLE) 36 else 16
            }

            2 -> {
                appleOdds = 160
                stickOdds = 40
                saplingOdds = if (getType() == WoodType.JUNGLE) 32 else 12
            }

            else -> {
                appleOdds = 120
                stickOdds = 30
                saplingOdds = if (getType() == WoodType.JUNGLE) 24 else 10
            }
        }

        val random = ThreadLocalRandom.current()
        if (canDropApple() && random.nextInt(appleOdds) == 0) {
            drops.add(Item.get(ItemID.APPLE))
        }
        if (random.nextInt(stickOdds) == 0) {
            drops.add(Item.get(ItemID.STICK))
        }
        if (random.nextInt(saplingOdds) == 0) {
            drops.add(toSapling())
        }

        return drops.toTypedArray()
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (isCheckDecay) {
                if (isPersistent || findLog(this, 7, null)) {
                    isCheckDecay = false
                    level.setBlock(this.position, this, direct = false, update = false)
                } else {
                    val ev = LeavesDecayEvent(this)
                    instance.pluginManager.callEvent(ev)
                    if (!ev.cancelled) {
                        level.useBreakOn(this.position)
                    }
                }
                return type
            }
        } else if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (!isCheckDecay) {
                isCheckDecay = true
                level.setBlock(this.position, this, direct = false, update = false)
            }

            // Slowly propagates the need to update instead of peaking down the TPS for huge trees
            for (side in BlockFace.entries) {
                val other = getSide(side)
                if (other is BlockLeaves) {
                    if (!other.isCheckDecay) {
                        level.scheduleUpdate(other, 2)
                    }
                }
            }
            return type
        }
        return type
    }

    private fun findLog(current: Block?, distance: Int, visited: MutableMap<Long, Long>?): Boolean {
        var visited1 = visited
        if (visited1 == null) {
            visited1 = mutableMapOf()
        }
        if (current is IBlockWood || current is BlockMangroveRoots) {
            return true
        }
        if (distance == 0 || current !is BlockLeaves) {
            return false
        }
        val hash = hashBlock(current.position)
        if (visited1.getOrDefault(hash, -1) >= distance) {
            return false
        }
        visited1[hash] = distance.toLong()
        for (face in VISIT_ORDER) {
            if (findLog(current.getSide(face), distance - 1, visited1)) {
                return true
            }
        }
        return false
    }

    var isCheckDecay: Boolean
        get() = getPropertyValue(CommonBlockProperties.UPDATE_BIT)
        set(checkDecay) {
            setPropertyValue(
                CommonBlockProperties.UPDATE_BIT,
                checkDecay
            )
        }

    var isPersistent: Boolean
        get() = getPropertyValue(CommonBlockProperties.PERSISTENT_BIT)
        set(persistent) {
            setPropertyValue(
                CommonBlockProperties.PERSISTENT_BIT,
                persistent
            )
        }

    override fun canSilkTouch(): Boolean {
        return true
    }

    protected fun canDropApple(): Boolean {
        return getType() == WoodType.OAK
    }

    override fun diffusesSkyLight(): Boolean {
        return true
    }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    open fun toSapling(): Item {
        return Item.AIR
    }

    companion object {
        private val VISIT_ORDER = arrayOf(
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP
        )
    }
}
