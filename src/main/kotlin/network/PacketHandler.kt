package org.chorus_oss.chorus.network

import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.protocol.LoginPacket

interface PacketHandler {
    fun handle(pk: MigrationPacket<*>) {}

    fun handle(pk: LoginPacket) {}
}