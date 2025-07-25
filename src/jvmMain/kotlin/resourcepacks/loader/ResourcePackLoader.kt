package org.chorus_oss.chorus.resourcepacks.loader

import org.chorus_oss.chorus.resourcepacks.ResourcePack

/**
 * 描述一个资源包加载器
 */
interface ResourcePackLoader {
    /**
     * 加载资源包并返回结果
     * @return 加载的资源包
     */
    fun loadPacks(): List<ResourcePack>
}
