package org.chorus_oss.chorus.resourcepacks.loader

import com.google.common.io.Files
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.resourcepacks.ResourcePack
import org.chorus_oss.chorus.resourcepacks.ZippedResourcePack
import org.chorus_oss.chorus.utils.Loggable
import java.io.File


class ZippedResourcePackLoader(//资源包文件存放地址
    protected val path: File
) : ResourcePackLoader {
    init {
        if (!path.exists()) {
            path.mkdirs()
        } else require(path.isDirectory) {
            Server.instance.lang
                .tr("chorus.resources.invalid-path", path.name)
        }
    }

    override fun loadPacks(): List<ResourcePack> {
        val baseLang = Server.instance.lang
        val loadedResourcePacks: MutableList<ResourcePack> = ArrayList()
        for (pack in path.listFiles()!!) {
            try {
                var resourcePack: ResourcePack? = null
                val fileExt = Files.getFileExtension(pack.name)
                if (!pack.isDirectory && fileExt != "key") { //directory resource packs temporarily unsupported
                    when (fileExt) {
                        "zip", "mcpack" -> resourcePack = ZippedResourcePack(pack)
                        else -> log.warn(
                            baseLang.tr(
                                "chorus.resources.unknown-format",
                                pack.name
                            )
                        )
                    }
                }
                if (resourcePack != null) {
                    loadedResourcePacks.add(resourcePack)
                    log.info(baseLang.tr("chorus.resources.zip.loaded", pack.name))
                }
            } catch (e: IllegalArgumentException) {
                log.warn(baseLang.tr("chorus.resources.fail", pack.name, e.message!!), e)
            }
        }
        return loadedResourcePacks
    }

    companion object : Loggable
}
