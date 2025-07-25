package org.chorus_oss.chorus.compression

import cn.powernukkitx.libdeflate.Libdeflate
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.TextFormat


object ZlibChooser : Loggable {
    private val providers = arrayOfNulls<ZlibProvider>(4)
    var currentProvider: ZlibProvider
        private set

    init {
        providers[2] = ZlibThreadLocal()
        currentProvider = providers[2]!!
    }

    @JvmStatic
    fun setProvider(providerIndex: Int) {
        var providerIndex1 = providerIndex
        val lang = Server.instance.lang
        when (providerIndex1) {
            0 -> if (providers[providerIndex1] == null) providers[providerIndex1] = ZlibKFlate()
            1 -> if (providers[providerIndex1] == null) providers[providerIndex1] = ZlibSingleThreadLowMem()
            2 -> {
                if (providers[providerIndex1] == null) providers[providerIndex1] = ZlibThreadLocal()
                if (Libdeflate.isAvailable()) {
                    log.info("{}{}", TextFormat.WHITE, lang.tr("chorus.zlib.acceleration-can-enable"))
                }
            }

            3 -> if (Libdeflate.isAvailable()) {
                if (providers[providerIndex1] == null) {
                    val libDeflateThreadLocal = LibDeflateThreadLocal(providers[2] as ZlibThreadLocal?)
                    providers[providerIndex1] = libDeflateThreadLocal
                }
            } else {
                log.warn(lang.tr("chorus.zlib.unavailable"))
                providerIndex1 = 2
                if (providers[providerIndex1] == null) providers[providerIndex1] = ZlibThreadLocal()
            }

            else -> throw UnsupportedOperationException("Invalid provider: $providerIndex1")
        }
        if (providerIndex1 < 2) {
            log.warn(lang.tr("chorus.zlib.affect-performance"))
        }
        currentProvider = providers[providerIndex1]!!
        log.info(
            "{}: {} ({})",
            lang.tr("chorus.zlib.selected"),
            providerIndex1,
            currentProvider.javaClass.canonicalName
        )
    }
}
