package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.protocol.packets.LevelEventPacket
import org.chorus_oss.protocol.types.Vector3f
import java.util.*

class BlockSponge @JvmOverloads constructor(state: BlockState = properties.defaultState) :
    BlockSolid(state) {
    override val hardness: Double
        get() = 0.6

    override val resistance: Double
        get() = 3.0

    override val toolType: Int
        get() = ItemTool.TYPE_HOE

    override val name: String
        get() = "Sponge"

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
        if ((block is BlockFlowingWater || block.levelBlockAround.stream()
                .anyMatch { b: Block? -> b is BlockFlowingWater }) && performWaterAbsorb(block)
        ) {
            level.setBlock(block.position, BlockWetSponge(), true, true)

            val packet = LevelEventPacket(
                eventType = LevelEventPacket.PARTICLE_DESTROY_BLOCK,
                position = Vector3f(
                    x = block.x.toFloat() + 0.5f,
                    y = block.y.toFloat() + 1f,
                    z = block.z.toFloat() + 0.5f,
                ),
                eventData = BlockFlowingWater.properties.defaultState.blockStateHash()
            )

            for (i in 0..3) {
                level.addChunkPacket(position.chunkX, position.chunkZ, packet)
            }

            return true
        }

        return super.place(item, block, target, face, fx, fy, fz, player)
    }

    private fun performWaterAbsorb(block: Block): Boolean {
        val entries: Queue<Entry> = ArrayDeque()

        entries.add(Entry(block, 0))

        var entry: Entry? = entries.poll()
        var waterRemoved = 0
        while (waterRemoved < 64 && entry != null) {
            for (face in BlockFace.entries) {
                val layer0 = entry.block.getSideAtLayer(0, face)
                val layer1 = layer0.getLevelBlockAtLayer(1)

                if (layer0 is BlockFlowingWater) {
                    level.setBlockStateAt(
                        layer0.position.floorX,
                        layer0.position.floorY,
                        layer0.position.floorZ,
                        BlockAir.properties.defaultState
                    )
                    level.updateAround(layer0.position)
                    waterRemoved++
                    if (entry.distance < 6) {
                        entries.add(Entry(layer0, entry.distance + 1))
                    }
                } else if (layer1 is BlockFlowingWater) {
                    if (BlockID.KELP == layer0.id ||
                        BlockID.SEAGRASS == layer0.id ||
                        BlockID.SEA_PICKLE == layer0.id || layer0 is BlockCoralFan
                    ) {
                        layer0.level.useBreakOn(layer0.position)
                    }
                    level.setBlockStateAt(
                        layer1.position.floorX,
                        layer1.position.floorY,
                        layer1.position.floorZ,
                        1,
                        BlockAir.properties.defaultState
                    )
                    level.updateAround(layer1.position)
                    waterRemoved++
                    if (entry.distance < 6) {
                        entries.add(Entry(layer1, entry.distance + 1))
                    }
                }
            }
            entry = entries.poll()
        }

        return waterRemoved > 0
    }

    private data class Entry(val block: Block, val distance: Int)

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SPONGE)
    }
}
