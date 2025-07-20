package org.chorus_oss.chorus.utils

import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

class OptionalValue<T> internal constructor(private val value: T?) {
    val isPresent: Boolean
        get() = value != null

    fun ifPresent(consumer: Consumer<T>) {
        if (value != null) {
            consumer.accept(value)
        }
    }

    fun get(): T? {
        return value
    }

    fun orElse(other: T): T {
        return value ?: other
    }

    fun orElseGet(other: Supplier<T>): T {
        return value ?: other.get()
    }

    fun <X : Throwable> orElseThrow(exceptionSupplier: Supplier<X>): T {
        if (value != null) {
            return value
        } else {
            throw exceptionSupplier.get()
        }
    }

    override fun toString(): String {
        return if (value == null) "OptionalValue.empty" else "OptionalValue[$value]"
    }

    companion object {
        private val EMPTY: OptionalValue<*> = OptionalValue<Any?>(null)

        fun <T> of(value: T): OptionalValue<T> {
            return OptionalValue(requireNotNull(value))
        }

        fun <T> ofNullable(value: T?): OptionalValue<T> {
            return if (value == null) empty() else of(value)
        }

        fun <T> empty(): OptionalValue<T> {
            return EMPTY as OptionalValue<T>
        }
    }
}
