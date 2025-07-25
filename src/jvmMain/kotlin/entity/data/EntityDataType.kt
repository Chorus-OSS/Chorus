package org.chorus_oss.chorus.entity.data

import java.util.function.Function
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

open class EntityDataType<out T : Any> {
    private val name: String
    private val type: KClass<out T>
    private val value: Int
    private val transformer: Function<out T, *>?
    private val defaultValue: T

    constructor(defaultValue: T, name: String, value: Int) {
        this.name = name
        this.type = defaultValue::class
        this.defaultValue = defaultValue
        this.value = value
        this.transformer = null
    }

    constructor(defaultValue: T, name: String, value: Int, transformer: Function<T, *>) {
        this.name = name
        this.type = defaultValue::class
        this.defaultValue = defaultValue
        this.value = value
        this.transformer = transformer
    }

    open fun isInstance(value: Any?): Boolean {
        return type.isInstance(value)
    }

    fun getTypeName(): String {
        return type.jvmName
    }

    fun getType(): KClass<out T> {
        return type
    }

    fun getDefaultValue(): T {
        return defaultValue
    }

    fun getTransformer(): Function<out T, *>? {
        return transformer
    }

    fun getValue(): Int {
        return value
    }

    override fun toString(): String {
        return name
    }
}
