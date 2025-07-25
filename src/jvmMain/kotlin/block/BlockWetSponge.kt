package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.particle.CloudParticle
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.protocol.packets.LevelEventPacket
import java.util.concurrent.ThreadLocalRandom

class BlockWetSponge @JvmOverloads constructor(state: BlockState = properties.defaultState) :
    BlockSolid(state) {
    override val hardness: Double
        get() = 0.6

    override val resistance: Double
        get() = 3.0

    override val toolType: Int
        get() = ItemTool.TYPE_HOE

    override val name: String
        get() = "Wet Sponge"

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
        if (level.dimension != Level.DIMENSION_NETHER) return true

        level.setBlock(block.position, BlockSponge(), true, true)
        level.addLevelEvent(block.position.add(0.5, 0.875, 0.5), LevelEventPacket.CAULDRON_EXPLODE)
        val random = ThreadLocalRandom.current()

        for (i in 0..7) {
            level.addParticle(CloudParticle(block.position.add(random.nextDouble(), 1.0, random.nextDouble())))
        }

        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WET_SPONGE)
    }
}
