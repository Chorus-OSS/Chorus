package org.chorus_oss.chorus.network.process

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.network.DataPacket

/**
 * A DataPacketProcessor is used to handle a specific type of DataPacket. <br></br>
 * DataPacketProcessor must be **thread-safe**. <br></br>
 * <hr></hr>
 * Why not interfaces? Hotspot C2 JIT cannot handle so many classes that impl the same interface, it makes the
 * performance lower.
 */
abstract class DataPacketProcessor<T : DataPacket> {
    abstract fun handle(player: Player, pk: T)

    abstract val packetId: Int
}
