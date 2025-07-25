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
import java.util.function.Function

/**
 * 与[CachedSimpleSelectorArgument]类似，但是适用于过滤器模式。此处不做过多解释
 *
 *
 * @see CachedSimpleSelectorArgument
 */
abstract class CachedFilterSelectorArgument : ISelectorArgument {
    var cache: Cache<Set<String>, Function<List<Entity>, List<Entity>>>

    init {
        this.cache = provideCacheService()
    }

    @Throws(SelectorSyntaxException::class)
    override fun getFilter(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform,
        vararg arguments: String
    ): Function<List<Entity>, List<Entity>>? {
        var value = cache.getIfPresent(Sets.newHashSet<String>(*arguments))
        if (value == null) {
            value = cache(selectorType, sender, basePos, *arguments)
            cache.put(Sets.newHashSet<String>(*arguments), value)
        }
        return value
    }

    override val isFilter: Boolean
        get() = true

    /**
     * 当未在缓存中找到解析结果时，则调用此方法对参数进行解析
     */
    @Throws(SelectorSyntaxException::class)
    protected abstract fun cache(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform,
        vararg arguments: String
    ): Function<List<Entity>, List<Entity>>

    /**
     * 初始化缓存时调用此方法
     *
     *
     * 若需要自己的缓存实现，则可覆写此方法
     * @return `Cache<Set<String>, Function<List<Entity>, List<Entity>>>`
     */
    protected fun provideCacheService(): Cache<Set<String>, Function<List<Entity>, List<Entity>>> {
        return Caffeine.newBuilder().maximumSize(65535).expireAfterAccess(1, TimeUnit.MINUTES).build()
    }
}
