package org.chorus_oss.chorus.network

import org.chorus_oss.chorus.experimental.network.MigrationPacket

interface PacketHandler {
    fun handle(pk: MigrationPacket<*>) {}
}