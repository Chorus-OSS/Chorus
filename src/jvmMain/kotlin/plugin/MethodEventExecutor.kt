package org.chorus_oss.chorus.plugin

import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.event.Listener
import org.chorus_oss.chorus.utils.EventException
import org.chorus_oss.chorus.utils.Loggable
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


class MethodEventExecutor(val method: Method?) : EventExecutor {
    @Throws(EventException::class)
    override fun execute(listener: Listener?, event: Event) {
        try {
            val params = method!!.parameterTypes as Array<Class<Event>>
            for (param in params) {
                if (param.isAssignableFrom(event.javaClass)) {
                    method.invoke(listener, event)
                    break
                }
            }
        } catch (ex: InvocationTargetException) {
            throw EventException(ex.cause ?: ex)
        } catch (ex: ClassCastException) {
            log.debug("Ignoring a ClassCastException", ex)
            // We are going to ignore ClassCastException because EntityDamageEvent can't be cast to EntityDamageByEntityEvent
        } catch (t: Throwable) {
            throw EventException(t)
        }
    }

    companion object : Loggable
}
