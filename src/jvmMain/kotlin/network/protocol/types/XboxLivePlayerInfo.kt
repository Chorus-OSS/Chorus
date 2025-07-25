package org.chorus_oss.chorus.network.protocol.types

import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.utils.ClientChainData
import java.util.*

class XboxLivePlayerInfo(
    username: String,
    uuid: UUID,
    skin: Skin,
    data: ClientChainData,
    val xuid: String
) : PlayerInfo(username, uuid, skin, data)
