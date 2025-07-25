package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntityCommandBlock
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.protocol.packets.CommandBlockUpdatePacket
import org.chorus_oss.protocol.types.CommandBlockMode
import java.util.function.Consumer

class CommandBlockUpdateProcessor : PacketProcessor<CommandBlockUpdatePacket> {
    override fun handle(player: Player, packet: CommandBlockUpdatePacket) {
        if (player.player.isOp && player.player.isCreative) {
            if (packet.isBlock) {
                val commandBlockData =
                    packet.commandBlockHolderData as CommandBlockUpdatePacket.Companion.CommandBlockData

                val blockEntity = player.player.level!!.getBlockEntity(
                    Vector3(commandBlockData.blockPosition)
                )
                if (blockEntity is BlockEntityCommandBlock) {
                    var cmdBlock = blockEntity.levelBlock

                    //change commandblock type
                    when (commandBlockData.commandBlockMode) {
                        CommandBlockMode.Repeating -> if (cmdBlock.id !== BlockID.REPEATING_COMMAND_BLOCK) {
                            cmdBlock = Block.get(BlockID.REPEATING_COMMAND_BLOCK).setPropertyValues(
                                cmdBlock.propertyValues
                            )
                            blockEntity.scheduleUpdate()
                        }

                        CommandBlockMode.Chain -> if (cmdBlock.id !== BlockID.CHAIN_COMMAND_BLOCK) {
                            cmdBlock =
                                Block.get(BlockID.CHAIN_COMMAND_BLOCK).setPropertyValues(cmdBlock.propertyValues)
                        }

                        CommandBlockMode.Normal -> if (cmdBlock.id !== BlockID.COMMAND_BLOCK) {
                            cmdBlock = Block.get(BlockID.COMMAND_BLOCK).setPropertyValues(cmdBlock.propertyValues)
                        }
                    }

                    val conditional = commandBlockData.isConditional
                    cmdBlock.setPropertyValue(CommonBlockProperties.CONDITIONAL_BIT, conditional)

                    blockEntity.command = packet.command
                    blockEntity.senderName = (packet.name)
                    blockEntity.setTrackOutput(packet.trackOutput)
                    blockEntity.isConditional = conditional
                    blockEntity.tickDelay = packet.tickDelay
                    blockEntity.isExecutingOnFirstTick = packet.shouldExecuteOnFirstTick

                    //redstone mode / auto
                    val isRedstoneMode = commandBlockData.redstoneMode
                    blockEntity.isAuto = !isRedstoneMode
                    if (!isRedstoneMode && commandBlockData.commandBlockMode == CommandBlockMode.Normal) {
                        blockEntity.trigger()
                    }
                    blockEntity.levelBlockAround.forEach(Consumer { b: Block -> b.onUpdate(Level.BLOCK_UPDATE_REDSTONE) }) //update redstone
                    player.player.level!!.setBlock(blockEntity.position, cmdBlock, true)
                }
            }
        }
    }

    override val packetID: Int = CommandBlockUpdatePacket.id
}
