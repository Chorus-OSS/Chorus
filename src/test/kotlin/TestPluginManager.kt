package org.chorus_oss.chorus

import com.google.common.base.Preconditions
import org.chorus_oss.chorus.command.SimpleCommandMap
import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.plugin.PluginManager

class TestPluginManager(server: Server, commandMap: SimpleCommandMap) :
    PluginManager(server, commandMap) {
    override fun callEvent(event: Event) {
        val i = counts.computeIfAbsent(event.javaClass) { 0 }
        counts[event.javaClass] = i + 1
        val testEventHandler = handlers[event.javaClass] as TestEventHandler<Event>? ?: return
        val castEvent = testEventHandler.eventClass.cast(event) as Event
        testEventHandler.handle(castEvent)
    }

    fun getCount(clazz: Class<in Event>): Int {
        return counts.computeIfAbsent(clazz) { 0 }
    }

    fun resetCount(clazz: Class<in Event>) {
        counts[clazz] = 0
    }

    fun resetAll() {
        counts.clear()
        handlers.clear()
    }

    fun registerTestEventHandler(consumers: List<TestEventHandler<*>>) {
        for (h in consumers) {
            handlers[h.eventClass] = h
        }
    }

    companion object {
        var handlers: MutableMap<Class<*>, TestEventHandler<*>> = HashMap()
        var counts: MutableMap<Class<in Event>, Int> = HashMap()
    }
}
