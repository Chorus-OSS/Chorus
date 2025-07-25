package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.enums.OxidizationLevel
import org.chorus_oss.chorus.event.block.BlockFadeEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.particle.ScrapeParticle
import org.chorus_oss.chorus.math.BlockFace
import java.util.concurrent.ThreadLocalRandom

interface Oxidizable {
    val locator: Locator

    fun onUpdate(type: Int): Int {
        if (type != Level.BLOCK_UPDATE_RANDOM) {
            return 0
        }
        val random = ThreadLocalRandom.current()
        if (!(random.nextFloat() < 64f / 1125f)) {
            return 0
        }

        val oxiLvl = oxidizationLevel.ordinal
        if (oxiLvl == OxidizationLevel.OXIDIZED.ordinal) {
            return 0
        }

        // Just to make sure we don't accidentally degrade a waxed block.
        if ((this is Waxable) && (this as Waxable).isWaxed) {
            return 0
        }

        val block = if (this is Block) this else locator.levelBlock
        val mutableLocator = block.locator

        var odds = 0
        var cons = 0

        for (x in -4..4) {
            for (y in -4..4) {
                for (z in -4..4) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue
                    }
                    mutableLocator.position.setComponents(
                        block.position.x + x,
                        block.position.y + y,
                        block.position.z + z
                    )
                    if (block.position.distanceManhattan(mutableLocator.position) > 4) {
                        continue
                    }
                    val relative = mutableLocator.levelBlock
                    if (relative !is Oxidizable) {
                        continue
                    }
                    val relOxiLvl = (relative as Oxidizable).oxidizationLevel.ordinal
                    if (relOxiLvl < oxiLvl) {
                        return type
                    }

                    if (relOxiLvl > oxiLvl) {
                        cons++
                    } else {
                        odds++
                    }
                }
            }
        }

        var chance = (cons + 1).toFloat() / (cons + odds + 1).toFloat()
        val multiplier = if (oxiLvl == 0) 0.75f else 1.0f
        chance *= chance * multiplier
        if (random.nextFloat() < chance) {
            val nextBlock = getBlockWithOxidizationLevel(OxidizationLevel.entries[oxiLvl + 1])
            val event = BlockFadeEvent(block, nextBlock)
            Server.instance.pluginManager.callEvent(event)
            if (!event.cancelled) {
                block.level.setBlock(block.position, event.newState)
            }
        }
        return type
    }

    fun onActivate(item: Item, player: Player?, blockFace: BlockFace, fx: Float, fy: Float, fz: Float): Boolean {
        if (!item.isAxe) {
            return false
        }

        var oxidizationLevel = oxidizationLevel
        if (OxidizationLevel.UNAFFECTED == oxidizationLevel) {
            return false
        }

        oxidizationLevel = OxidizationLevel.entries[oxidizationLevel.ordinal - 1]
        if (!setOxidizationLevel(oxidizationLevel)) {
            return false
        }

        val location = if (this is Block) this else locator
        if (player == null || !player.isCreative) {
            item.useOn((if (this is Block) this else location.levelBlock))
        }
        location.level.addParticle(ScrapeParticle(location.position))
        return true
    }

    val oxidizationLevel: OxidizationLevel

    fun setOxidizationLevel(oxidizationLevel: OxidizationLevel): Boolean

    fun getBlockWithOxidizationLevel(oxidizationLevel: OxidizationLevel): Block
}
