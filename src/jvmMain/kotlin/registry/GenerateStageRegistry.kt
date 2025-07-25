package org.chorus_oss.chorus.registry

import org.chorus_oss.chorus.level.generator.GenerateStage
import org.chorus_oss.chorus.level.generator.stages.FinishedStage
import org.chorus_oss.chorus.level.generator.stages.FlatGenerateStage
import org.chorus_oss.chorus.level.generator.stages.LightPopulationStage
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.atomic.AtomicBoolean

class GenerateStageRegistry :
    IRegistry<String, GenerateStage, Class<out GenerateStage>> {
    override fun init() {
        if (isLoad.getAndSet(true)) return
        try {
            this.register(FinishedStage.NAME, FinishedStage::class.java)
            this.register(FlatGenerateStage.NAME, FlatGenerateStage::class.java)
            this.register(LightPopulationStage.NAME, LightPopulationStage::class.java)
        } catch (e: RegisterException) {
            throw RuntimeException(e)
        }
    }

    fun get(c: Class<out GenerateStage?>): GenerateStage? {
        for ((_, value) in REGISTRY) {
            if (value == c) {
                try {
                    return value.getConstructor().newInstance()
                } catch (e: InstantiationException) {
                    throw RuntimeException(e)
                } catch (e: IllegalAccessException) {
                    throw RuntimeException(e)
                } catch (e: InvocationTargetException) {
                    throw RuntimeException(e)
                } catch (e: NoSuchMethodException) {
                    throw RuntimeException(e)
                }
            }
        }
        return null
    }

    override fun get(key: String): GenerateStage {
        try {
            return REGISTRY[key.lowercase()]!!.getConstructor().newInstance()
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        }
    }

    fun getStageByName(key: String): Class<out GenerateStage>? {
        return REGISTRY[key.lowercase()]
    }

    override fun reload() {
        isLoad.set(false)
        REGISTRY.clear()
        init()
    }

    @Throws(RegisterException::class)
    override fun register(key: String, value: Class<out GenerateStage>) {
        if (REGISTRY.putIfAbsent(key.lowercase(), value) != null) {
            throw RegisterException("This generator has already been registered with the key: $key")
        }
    }

    companion object {
        private val REGISTRY = HashMap<String, Class<out GenerateStage>>()
        private val isLoad = AtomicBoolean(false)
    }
}
