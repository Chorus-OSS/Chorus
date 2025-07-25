package org.chorus_oss.chorus.event

import org.chorus_oss.chorus.utils.EventException

/**
 * 描述服务器中可能发生的事情的类。<br></br>
 * Describes things that happens in the server.
 *
 *
 * 服务器中可能发生的事情称作**事件**。定义一个需要它在一个事件发生时被运行的过程，这个过程称作**监听器**。<br></br>
 * Things that happens in the server is called a **event**. Define a procedure that should be executed
 * when an event happens, this procedure is called a **listener**.
 *
 *
 * Nukkit调用事件的处理器时，会通过参数的类型判断需要被监听的事件。<br></br>
 * When Nukkit is calling a handler, the event needed to listen is judged by the type of the parameter.
 *
 *
 * 关于监听器的实现，参阅：[Listener] <br></br>
 * For the way to implement a listener, see: [org.chorus_oss.chorus.event.Listener]
 *
 * @see org.chorus_oss.chorus.event.EventHandler
 */
abstract class Event {
    protected var name: String? = null

    protected var cancel = false

    open var cancelled: Boolean
        get() {
            if (this !is Cancellable) {
                throw EventException("Event is not Cancellable")
            }
            return cancel // TODO: field
        }
        set(value) {
            if (this !is Cancellable) {
                throw EventException("Event is not Cancellable")
            }
            cancel = value
        }

    fun getSafeName(): String {
        return name ?: javaClass.name
    }
}
