package org.chorus_oss.chorus.event

import org.chorus_oss.chorus.plugin.Plugin
import org.chorus_oss.chorus.plugin.RegisteredListener
import java.util.*
import kotlin.concurrent.Volatile

class HandlerList {
    @Volatile
    private var handlers: Array<RegisteredListener>? = null

    private val handlerslots =
        EnumMap<EventPriority, ArrayList<RegisteredListener>>(EventPriority::class.java)

    init {
        for (o in EventPriority.entries) {
            handlerslots[o] = ArrayList()
        }
        synchronized(allLists) {
            allLists.add(this)
        }
    }

    @Synchronized
    fun register(listener: RegisteredListener) {
        check(!handlerslots[listener.priority]!!.contains(listener)) { "This listener is already registered to priority " + listener.priority.toString() }
        handlers = null
        handlerslots[listener.priority]!!.add(listener)
    }

    fun registerAll(listeners: Collection<RegisteredListener>) {
        for (listener in listeners) {
            register(listener)
        }
    }

    @Synchronized
    fun unregister(listener: RegisteredListener) {
        if (handlerslots[listener.priority]!!.remove(listener)) {
            handlers = null
        }
    }

    @Synchronized
    fun unregister(plugin: Plugin) {
        var changed = false
        for (list in handlerslots.values) {
            val i = list.listIterator()
            while (i.hasNext()) {
                if (i.next().plugin == plugin) {
                    i.remove()
                    changed = true
                }
            }
        }
        if (changed) handlers = null
    }

    @Synchronized
    fun unregister(listener: Listener) {
        var changed = false
        for (list in handlerslots.values) {
            val i = list.listIterator()
            while (i.hasNext()) {
                if (i.next().listener == listener) {
                    i.remove()
                    changed = true
                }
            }
        }
        if (changed) handlers = null
    }

    @Synchronized
    fun bake() {
        if (handlers != null) return  // don't re-bake when still valid

        val entries: MutableList<RegisteredListener> = ArrayList()
        for ((_, value) in handlerslots) {
            entries.addAll(value)
        }
        handlers = entries.toTypedArray()
    }

    val registeredListeners: Array<RegisteredListener>
        get() {
            var handlers: Array<RegisteredListener>?
            while ((this.handlers.also { handlers = it }) == null) {
                bake()
            } // This prevents fringe cases of returning null

            return handlers!!
        }

    val isEmpty: Boolean
        get() {
            val handlers = this.handlers
            if (handlers != null) {
                return handlers.isEmpty()
            }
            return registeredListeners.isEmpty()
        }

    companion object {
        private val allLists = ArrayList<HandlerList>()

        fun bakeAll() {
            synchronized(allLists) {
                for (h in allLists) {
                    h.bake()
                }
            }
        }

        @JvmStatic
        fun unregisterAll() {
            synchronized(allLists) {
                for (h in allLists) {
                    synchronized(h) {
                        for (list in h.handlerslots.values) {
                            list.clear()
                        }
                        h.handlers = null
                    }
                }
            }
        }

        @JvmStatic
        fun unregisterAll(plugin: Plugin) {
            synchronized(allLists) {
                for (h in allLists) {
                    h.unregister(plugin)
                }
            }
        }

        fun unregisterAll(listener: Listener) {
            synchronized(allLists) {
                for (h in allLists) {
                    h.unregister(listener)
                }
            }
        }

        fun getRegisteredListeners(plugin: Plugin): ArrayList<RegisteredListener> {
            val listeners = ArrayList<RegisteredListener>()
            synchronized(allLists) {
                for (h in allLists) {
                    synchronized(h) {
                        for (list in h.handlerslots.values) {
                            for (listener in list) {
                                if (listener.plugin == plugin) {
                                    listeners.add(listener)
                                }
                            }
                        }
                    }
                }
            }
            return listeners
        }

        val handlerLists: ArrayList<HandlerList>
            get() {
                synchronized(allLists) {
                    return ArrayList(allLists)
                }
            }
    }
}
