package org.chorus_oss.chorus.level.format

import org.chorus_oss.chorus.utils.Loggable


object LevelProviderManager : Loggable {
    private val providers: MutableMap<String, Class<out LevelProvider>> = HashMap()

    fun addProvider(name: String, clazz: Class<out LevelProvider>) {
        if (providers.putIfAbsent(name.trim { it <= ' ' }.lowercase(), clazz) != null) {
            log.error("Duplicate registration Level Provider {}", clazz)
        }
    }

    @JvmStatic
    fun getProvider(path: String?): Class<out LevelProvider>? {
        for (provider in providers.values) {
            try {
                if (provider.getMethod("isValid", String::class.java).invoke(null, path) as Boolean) {
                    return provider
                }
            } catch (e: Exception) {
                log.error("An error occurred while getting the provider {}", path, e)
            }
        }
        return null
    }

    fun getProviderName(clazz: Class<out LevelProvider?>): String {
        for ((key, value) in providers) {
            if (clazz == value) {
                return key
            }
        }
        return "unknown"
    }

    @JvmStatic
    fun getProviderByName(name: String): Class<out LevelProvider>? {
        val name1 = name.trim { it <= ' ' }.lowercase()
        return providers.getOrDefault(name1, null)
    }
}
