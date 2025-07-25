package org.chorus_oss.chorus.registry

import org.chorus_oss.chorus.level.generator.Flat
import org.chorus_oss.chorus.level.generator.Generator
import java.util.concurrent.atomic.AtomicBoolean

class GeneratorRegistry :
    IRegistry<String, Class<out Generator>?, Class<out Generator>> {
    override fun init() {
        if (isLoad.getAndSet(true)) return
        try {
            register("flat", Flat::class.java)
            register("normal", Flat::class.java)
        } catch (e: RegisterException) {
            throw RuntimeException(e)
        }
    }

    fun getGeneratorName(c: Class<out Generator>): String {
        for ((key, value) in REGISTRY) {
            if (value == c) {
                return key
            }
        }
        return "unknown"
    }

    val generatorList: Set<String>
        get() = REGISTRY.keys

    override operator fun get(key: String): Class<out Generator>? {
        return REGISTRY[key.lowercase()]
    }

    override fun reload() {
        isLoad.set(false)
        REGISTRY.clear()
        init()
    }

    @Throws(RegisterException::class)
    override fun register(key: String, value: Class<out Generator>) {
        if (REGISTRY.putIfAbsent(key.lowercase(), value) != null) {
            throw RegisterException("This generator has already been registered with the key: $key")
        }
    }

    companion object {
        private val REGISTRY = HashMap<String, Class<out Generator>>()
        private val isLoad = AtomicBoolean(false)
    }
}
