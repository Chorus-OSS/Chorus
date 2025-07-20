package org.chorus_oss.chorus.resourcepacks.loader

import com.google.common.io.Files
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.resourcepacks.JarPluginResourcePack
import org.chorus_oss.chorus.resourcepacks.ResourcePack
import org.chorus_oss.chorus.utils.Loggable
import java.io.File
import java.util.*


class JarPluginResourcePackLoader(protected val jarPath: File) : ResourcePackLoader {
    override fun loadPacks(): List<ResourcePack> {
        val baseLang = Server.instance.lang
        val loadedResourcePacks: MutableList<ResourcePack> = ArrayList()
        for (jar in requireNotNull(jarPath.listFiles())) {
            try {
                var resourcePack: ResourcePack? = null
                val fileExt = Files.getFileExtension(jar.name)
                if (!jar.isDirectory) {
                    if (fileExt == "jar" && JarPluginResourcePack.hasResourcePack(jar)) {
                        log.info(baseLang.tr("chorus.resources.plugin.loading", jar.name))
                        resourcePack = JarPluginResourcePack(jar)
                    }
                }
                if (resourcePack != null) {
                    loadedResourcePacks.add(resourcePack)
                    log.info(
                        baseLang.tr(
                            "chorus.resources.plugin.loaded",
                            jar.name,
                            resourcePack.packName ?: ""
                        )
                    )
                }
            } catch (e: IllegalArgumentException) {
                log.warn(baseLang.tr("chorus.resources.fail", jar.name, e.message!!), e)
            }
        }
        return loadedResourcePacks
    }

    companion object : Loggable
}
