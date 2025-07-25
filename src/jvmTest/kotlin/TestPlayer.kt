package org.chorus_oss.chorus

import org.chorus_oss.chorus.network.connection.BedrockSession
import org.chorus_oss.chorus.network.protocol.types.PlayerInfo

class TestPlayer(session: BedrockSession, info: PlayerInfo) : Player(session, info)
