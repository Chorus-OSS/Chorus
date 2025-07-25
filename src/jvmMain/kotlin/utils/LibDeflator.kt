package org.chorus_oss.chorus.utils

import cn.powernukkitx.libdeflate.LibdeflateCompressor
import org.chorus_oss.chorus.Server

class LibDeflator @JvmOverloads constructor(
    level: Int = Server.instance.settings.networkSettings.compressionLevel
) : LibdeflateCompressor(level)
