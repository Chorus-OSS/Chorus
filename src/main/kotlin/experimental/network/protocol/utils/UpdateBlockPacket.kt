package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.protocol.packets.UpdateBlockPacket

val UpdateBlockPacket.Companion.FLAG_ALL: UInt
    get() = UpdateBlockPacket.FLAG_NETWORK or UpdateBlockPacket.FLAG_NEIGHBORS

val UpdateBlockPacket.Companion.FLAG_ALL_PRIORITY: UInt
    get() = UpdateBlockPacket.FLAG_ALL or UpdateBlockPacket.FLAG_PRIORITY