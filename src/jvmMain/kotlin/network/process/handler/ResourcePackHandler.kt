package org.chorus_oss.chorus.network.process.handler

import kotlinx.io.bytestring.ByteString
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.network.connection.BedrockSession
import org.chorus_oss.chorus.network.process.SessionState
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.packets.ResourcePackChunkDataPacket
import org.chorus_oss.protocol.packets.ResourcePackChunkRequestPacket
import org.chorus_oss.protocol.packets.ResourcePackDataInfoPacket
import org.chorus_oss.protocol.types.StackResourcePack
import org.chorus_oss.protocol.types.TexturePackInfo
import java.util.*
import kotlin.math.ceil
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
class ResourcePackHandler(session: BedrockSession) : BedrockSessionPacketHandler(session) {
    init {
        val infoPacket = org.chorus_oss.protocol.packets.ResourcePacksInfoPacket(
            texturePackRequired = Server.instance.forceResources,
            hasAddons = false,
            hasScripts = false,
            forceDisableVibrantVisuals = false,
            worldTemplateUuid = Uuid.random(),
            worldTemplateVersion = "",
            texturePacks = Server.instance.resourcePackManager.resourceStack.map(TexturePackInfo::invoke)
        )
        session.sendPacket(infoPacket)
    }

    override fun handle(packet: Packet) {
        when (packet) {
            is ResourcePackChunkRequestPacket -> handleRequest(packet)
            is org.chorus_oss.protocol.packets.ResourcePackClientResponsePacket -> handleResponse(packet)
        }
    }

    fun handleResponse(packet: org.chorus_oss.protocol.packets.ResourcePackClientResponsePacket) {
        val server = Server.instance
        when (packet.response) {
            org.chorus_oss.protocol.packets.ResourcePackClientResponsePacket.Companion.Response.Refused -> {
                log.debug("ResourcePackClientResponsePacket STATUS_REFUSED")
                session.close("disconnectionScreen.noReason")
            }

            org.chorus_oss.protocol.packets.ResourcePackClientResponsePacket.Companion.Response.SendPacks -> {
                log.debug("ResourcePackClientResponsePacket STATUS_SEND_PACKS")
                for (entry in packet.packsToDownload) {
                    val packID = UUID.fromString(entry.split("_")[0])
                    val resourcePack = server.resourcePackManager.getPackById(packID)
                    if (resourcePack == null) {
                        session.close("disconnectionScreen.resourcePack")
                        return
                    }

                    val dataInfoPacket = ResourcePackDataInfoPacket(
                        resourceName = "${resourcePack.packId}_${resourcePack.packVersion}",
                        chunkSize = server.resourcePackManager.maxChunkSize.toUInt(),
                        chunkCount = ceil(resourcePack.packSize / server.resourcePackManager.maxChunkSize.toDouble()).toUInt(),
                        fileSize = resourcePack.packSize.toULong(),
                        fileHash = ByteString(resourcePack.sha256),
                        premium = false,
                        type = ResourcePackDataInfoPacket.Companion.Type.Resource
                    )
                    session.sendPacket(dataInfoPacket)
                }
            }

            org.chorus_oss.protocol.packets.ResourcePackClientResponsePacket.Companion.Response.AllPacksDownloaded -> {
                log.debug("ResourcePackClientResponsePacket STATUS_HAVE_ALL_PACKS")
                val stackPacket = org.chorus_oss.protocol.packets.ResourcePackStackPacket(
                    texturePackRequired = server.forceResources && !server.forceResourcesAllowOwnPacks,
                    behaviourPacks = emptyList(),
                    texturePacks = server.resourcePackManager.resourceStack.map(StackResourcePack::invoke),
                    baseGameVersion = "*",
                    experiments = listOf(
                        org.chorus_oss.protocol.types.ExperimentData("data_driven_items", true),
                        org.chorus_oss.protocol.types.ExperimentData("data_driven_biomes", true),
                        org.chorus_oss.protocol.types.ExperimentData("upcoming_creator_features", true),
                        org.chorus_oss.protocol.types.ExperimentData("gametest", true),
                        org.chorus_oss.protocol.types.ExperimentData("experimental_molang_features", true),
                        org.chorus_oss.protocol.types.ExperimentData("cameras", true)
                    ),
                    experimentsPreviouslyToggled = true,
                    includeEditorPacks = false,
                )
                session.sendPacket(stackPacket)
            }

            org.chorus_oss.protocol.packets.ResourcePackClientResponsePacket.Companion.Response.Completed -> {
                log.debug("ResourcePackClientResponsePacket STATUS_COMPLETED")
                session.machine.fire(SessionState.PreSpawn)
            }
        }
    }

    fun handleRequest(packet: ResourcePackChunkRequestPacket) {
        // TODO: Pack version check
        val mgr = Server.instance.resourcePackManager
        val packID = UUID.fromString(packet.resourceName.split("_", limit = 2)[0])
        val resourcePack = mgr.getPackById(packID)
        if (resourcePack == null) {
            session.close("disconnectionScreen.resourcePack")
            return
        }
        val maxChunkSize = mgr.maxChunkSize
        val dataPacket = ResourcePackChunkDataPacket(
            resourceName = "${resourcePack.packId}_${resourcePack.packVersion}",
            chunkID = packet.chunkID,
            byteOffset = (maxChunkSize.toUInt() * packet.chunkID).toULong(),
            chunkData = ByteString(resourcePack.getPackChunk(maxChunkSize * packet.chunkID.toInt(), maxChunkSize)),
        )
        session.sendPacket(dataPacket)
    }

    companion object : Loggable
}
