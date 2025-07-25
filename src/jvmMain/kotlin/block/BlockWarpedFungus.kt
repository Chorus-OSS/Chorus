package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.level.StructureGrowEvent
import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.level.generator.`object`.legacytree.LegacyWarpedTree
import org.chorus_oss.chorus.utils.ChorusRandom

class BlockWarpedFungus @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFungus(blockstate) {
    private val feature: LegacyWarpedTree = LegacyWarpedTree()

    override val name: String
        get() = "Warped Fungus"

    override fun canGrowOn(support: Block?): Boolean {
        if (support!!.id == BlockID.WARPED_NYLIUM) {
            for (i in 1..feature.treeHeight) {
                if (!up(i).isAir) {
                    return false
                }
            }
            return true
        }
        return false
    }

    override fun grow(cause: Player?): Boolean {
        val random = ChorusRandom()
        val blockManager: BlockManager = BlockManager(this.level)
        feature.placeObject(
            blockManager,
            position.floorX, position.floorY, position.floorZ, random
        )
        val ev: StructureGrowEvent = StructureGrowEvent(this, blockManager.blocks)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            return false
        }
        blockManager.applySubChunkUpdate(ev.blockList)
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WARPED_FUNGUS)
    }
}