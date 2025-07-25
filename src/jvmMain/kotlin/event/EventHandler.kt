package org.chorus_oss.chorus.event

/**
 * 定义一个事件的处理器的注解。<br></br>
 * Annotation that defines a handler.
 *
 *
 * 一个处理器的重要程度被称作处理器的**优先级**，优先级高的处理器有更多的决定权。参见：[.priority]<br></br>
 * The importance of a handler is called its **priority**, handlers with higher priority speaks louder then
 * lower ones. See: [.priority]
 *
 *
 * 处理器可以选择忽略或不忽略被取消的事件，这种特性可以在[.ignoreCancelled]中定义。<br></br>
 * A handler can choose to ignore a cancelled event or not, that can be defined in [.ignoreCancelled].
 *
 * @see org.chorus_oss.chorus.event.Listener
 *
 * @see org.chorus_oss.chorus.event.Event
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class EventHandler(
    /**
     * 定义这个处理器的优先级。<br></br>
     * Define the priority of the handler.
     *
     *
     * Nukkit调用处理器时会按照优先级从低到高的顺序调用，这样保证了高优先级的监听器能覆盖低优先级监听器做出的处理。
     * 调用的先后顺序如下：<br></br>
     * When Nukkit calls all handlers, ones with lower priority is called earlier,
     * that make handlers with higher priority can replace the decisions made by lower ones.
     * The order that Nukkit call handlers is from the first to the last as:
     *
     *  1. EventPriority.LOWEST
     *  1. EventPriority.LOW
     *  1. EventPriority.NORMAL
     *  1. EventPriority.HIGH
     *  1. EventPriority.HIGHEST
     *  1. EventPriority.MONITOR
     *
     *
     * @return 这个处理器的优先级。<br></br>The priority of this handler.
     * @see org.chorus_oss.chorus.event.EventHandler
     */
    val priority: EventPriority = EventPriority.NORMAL,
    /**
     * 定义这个处理器是否忽略被取消的事件。<br></br>
     * Define if the handler ignores a cancelled event.
     *
     *
     * 如果为`true`而且事件发生，这个处理器不会被调用，反之相反。<br></br>
     * If ignoreCancelled is `true` and the event is cancelled, the method is
     * not called. Otherwise, the method is always called.
     *
     * @return 这个处理器是否忽略被取消的事件。<br></br>Whether cancelled events should be ignored.
     * @see org.chorus_oss.chorus.event.EventHandler
     */
    val ignoreCancelled: Boolean = false
)
