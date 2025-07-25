package org.chorus_oss.chorus.plugin

import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.event.EventPriority
import org.chorus_oss.chorus.event.Listener
import org.chorus_oss.chorus.utils.EventException


class RegisteredListener(
    val listener: Listener,
    private var executor: EventExecutor,
    val priority: EventPriority?,
    val plugin: Plugin,
    val isIgnoringCancelled: Boolean
) {
    @Throws(EventException::class)
    fun callEvent(event: Event) {
        if (event is Cancellable) {
            if (event.cancelled && isIgnoringCancelled) {
                return
            }
        }
        try {
            executor.execute(listener, event)
        } catch (_: IllegalAccessError) { // 动态编译的字节码调用失败时的逃生门
            if (executor is CompiledExecutor) {
                executor = MethodEventExecutor((executor as CompiledExecutor).originMethod)
                executor.execute(listener, event)
            }
        } catch (_: NoSuchMethodError) {
            if (executor is CompiledExecutor) {
                executor = MethodEventExecutor((executor as CompiledExecutor).originMethod)
                executor.execute(listener, event)
            }
        }
    }

    companion object {
        val EMPTY_ARRAY: Array<RegisteredListener?> = arrayOfNulls(0)
    }
}
