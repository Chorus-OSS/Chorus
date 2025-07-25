package org.chorus_oss.chorus.registry

interface IRegistry<K, V, R> {
    fun init()

    fun get(key: K): V

    fun reload()

    @Throws(RegisterException::class)
    fun register(key: K, value: R)
}
