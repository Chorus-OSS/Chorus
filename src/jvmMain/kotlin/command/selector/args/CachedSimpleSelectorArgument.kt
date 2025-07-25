package org.chorus_oss.chorus.command.selector.args

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.collect.Sets
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException
import org.chorus_oss.chorus.command.selector.SelectorType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Transform
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

/**
 * 可缓存的目标选择器参数基类
 *
 *
 * 若一个选择器的参数返回的`List<Predicate<Entity>>`不具有时效性，则可继承此类实现对解析结果的缓存，提高性能
 */
abstract class CachedSimpleSelectorArgument : ISelectorArgument {
    var cache: Cache<Set<String>, Predicate<Entity>?>

    init {
        this.cache = provideCacheService()
    }

    @Throws(SelectorSyntaxException::class)
    override fun getPredicate(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform,
        vararg arguments: String
    ): Predicate<Entity>? {
        var value = cache.getIfPresent(Sets.newHashSet(*arguments))
        if (value == null) {
            value = cache(selectorType, sender, basePos, *arguments)
            cache.put(Sets.newHashSet(*arguments), value)
        }
        return value
    }

    /**
     * 当未在缓存中找到解析结果时，则调用此方法对参数进行解析
     */
    @Throws(SelectorSyntaxException::class)
    protected abstract fun cache(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform?,
        vararg arguments: String
    ): Predicate<Entity>

    /**
     * 初始化缓存时调用此方法
     *
     *
     * 若需要自己的缓存实现，则可覆写此方法
     * @return `Cache<Set<String>, Predicate<Entity>>`
     */
    protected fun provideCacheService(): Cache<Set<String>, Predicate<Entity>?> {
        return Caffeine.newBuilder().maximumSize(65535).expireAfterAccess(1, TimeUnit.MINUTES).build()
    }
}