package org.chorus_oss.chorus

import com.google.common.base.Preconditions
import org.chorus_oss.chorus.event.Event
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class TestEventHandler<T : Event?> protected constructor() {
    private val type: Type
    val eventClass: Class<T>

    init {
        val parameterizedTypeReferenceSubclass = findParameterizedTypeReferenceSubclass(this.javaClass)
        val type = parameterizedTypeReferenceSubclass.genericSuperclass
        check(type is ParameterizedType) { "Type must be a parameterized type" }
        val parameterizedType = type
        val actualTypeArguments = parameterizedType.actualTypeArguments
        check(actualTypeArguments.size == 1) { "Number of type arguments must be 1" } // 设置结果
        this.type = actualTypeArguments[0]
        val actualTypeArgument = type.actualTypeArguments[0]
        check(actualTypeArgument is Class<*>) { "Type must be a class type" }
        this.eventClass = actualTypeArgument as Class<T>
    }

    abstract fun handle(event: T)

    companion object {
        private fun findParameterizedTypeReferenceSubclass(child: Class<*>): Class<*> {
            val parent = child.superclass
            check(Any::class.java != parent) { "Expected ParameterizedTypeReference superclass" }
            return if (parent == TestEventHandler::class.java) child else findParameterizedTypeReferenceSubclass(parent)
        }
    }
}
