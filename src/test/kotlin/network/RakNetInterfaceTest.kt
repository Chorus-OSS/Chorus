package org.chorus_oss.chorus.network

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import kotlinx.io.Buffer
import kotlinx.serialization.builtins.ByteArraySerializer
import org.chorus_oss.chorus.GameMockExtension
import org.chorus_oss.chorus.network.connection.netty.initializer.BedrockChannelInitializer
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.ByteBufVarInt
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.core.PacketRegistry
import org.chorus_oss.protocol.packets.RequestNetworkSettingsPacket
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory
import org.cloudburstmc.netty.channel.raknet.RakChildChannel
import org.cloudburstmc.netty.channel.raknet.RakClientChannel
import org.cloudburstmc.netty.channel.raknet.RakConstants
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ThreadLocalRandom


@ExtendWith(GameMockExtension::class)
class RakNetInterfaceTest {
    @Test
    @Timeout(20)
    fun test(gameMockExtension: GameMockExtension) {
        val mtu = RakConstants.MAXIMUM_MTU_SIZE
        val inetSocketAddress = InetSocketAddress("127.0.0.1", 55555)
        serverBootstrap().childHandler(object : ChannelInitializer<RakChildChannel>() {
            @Throws(Exception::class)
            override fun initChannel(ch: RakChildChannel) {
                println("Server child channel initialized")
                ch.pipeline().addLast(object : SimpleChannelInboundHandler<RakMessage>() {
                    @Throws(Exception::class)
                    override fun channelRead0(ctx: ChannelHandlerContext, msg: RakMessage) {
                        val content = msg.content()
                        val id = content.readUnsignedByte().toInt() //frame id
                        if (id != BedrockChannelInitializer.RAKNET_MINECRAFT_ID) {
                            log.error("Client receive a Invalid packet for frame ID!")
                            System.exit(1)
                        }

                        //batch packet
                        val packetLength = ByteBufVarInt.readUnsignedInt(content)
                        val byteBuf = content.readSlice(packetLength)

                        //decode to game packet
                        val header = ByteBufVarInt.readUnsignedInt(byteBuf)
                        assert(header == org.chorus_oss.protocol.packets.RequestNetworkSettingsPacket.id)
                        val dataPacket = PacketRegistry[header]!!
                        val packet = dataPacket.deserialize(Buffer().apply {
                            val bytes = ByteArray(byteBuf.readableBytes())
                            byteBuf.readBytes(bytes)
                            this.write(bytes)
                        })
                        val target = packet as org.chorus_oss.protocol.packets.RequestNetworkSettingsPacket
                        assert(target.clientProtocol == org.chorus_oss.protocol.ProtocolInfo.VERSION)
                        gameMockExtension.stopNetworkTickLoop()
                    }
                })
            }
        }).bind(inetSocketAddress).syncUninterruptibly()
        clientBootstrap(mtu)
            .handler(object : ChannelInitializer<RakClientChannel>() {
                @Throws(Exception::class)
                override fun initChannel(ch: RakClientChannel) {
                    println("Client channel initialized")
                    //raknet datagram -> channelinboudhander
                    ch.pipeline()
                        .addLast(object : ChannelInboundHandlerAdapter() {
                            @Throws(Exception::class)
                            override fun channelActive(ctx: ChannelHandlerContext) {
                                val buf = ctx.alloc().buffer() //refCnt = 1
                                assert(buf.refCnt() == 1)
                                buf.writeByte(BedrockChannelInitializer.RAKNET_MINECRAFT_ID) //frame id
                                //batch packet
                                ByteBufVarInt.writeUnsignedInt(buf, 6) //packet length
                                //RequestNetworkSettingsPacket
                                ByteBufVarInt.writeUnsignedInt(
                                    buf,
                                    RequestNetworkSettingsPacket.id
                                ) //packet header
                                buf.writeInt(org.chorus_oss.protocol.ProtocolInfo.VERSION)
                                //RequestNetworkSettingsPacket
                                ctx.channel().writeAndFlush(RakMessage(buf))
                            }
                        })
                }
            })
            .connect(inetSocketAddress)
            .awaitUninterruptibly()
            .channel()
        gameMockExtension.mockNetworkTickLoop()
    }

    companion object : Loggable {
        private val ADVERTISEMENT = StringJoiner(";", "", ";")
            .add("MCPE")
            .add("RakNet unit test")
            .add(542.toString())
            .add("1.19.0")
            .add(0.toString())
            .add(4.toString())
            .add(java.lang.Long.toUnsignedString(ThreadLocalRandom.current().nextLong()))
            .add("C")
            .add("Survival")
            .add("1")
            .add("19132")
            .add("19132")
            .toString().toByteArray(StandardCharsets.UTF_8)

        private fun clientBootstrap(mtu: Int): Bootstrap {
            return Bootstrap()
                .channelFactory(RakChannelFactory.client(NioDatagramChannel::class.java))
                .group(NioEventLoopGroup())
                .option(RakChannelOption.RAK_PROTOCOL_VERSION, 11)
                .option(RakChannelOption.RAK_MTU, mtu)
                .option(RakChannelOption.RAK_ORDERING_CHANNELS, 1)
        }

        private fun serverBootstrap(): ServerBootstrap {
            return ServerBootstrap()
                .channelFactory(RakChannelFactory.server(NioDatagramChannel::class.java))
                .group(NioEventLoopGroup())
                .option(RakChannelOption.RAK_SUPPORTED_PROTOCOLS, intArrayOf(11))
                .option(RakChannelOption.RAK_MAX_CONNECTIONS, 1)
                .childOption(RakChannelOption.RAK_ORDERING_CHANNELS, 1)
                .option(RakChannelOption.RAK_GUID, ThreadLocalRandom.current().nextLong())
                .option(RakChannelOption.RAK_ADVERTISEMENT, Unpooled.wrappedBuffer(ADVERTISEMENT))
        }
    }
}
