package org.chorus_oss.chorus.command.selector

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.collect.Maps
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.NPCCommandSender
import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException
import org.chorus_oss.chorus.command.selector.args.ISelectorArgument
import org.chorus_oss.chorus.command.selector.args.impl.*
import org.chorus_oss.chorus.entity.Entity
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * 目标选择器API
 *
 *
 * 通过`getAPI()`方法获取API对象
 */
class EntitySelectorAPI private constructor() {
    var registry: MutableMap<String, ISelectorArgument> =
        HashMap<String, ISelectorArgument>()
    var orderedArgs: MutableList<ISelectorArgument> = ArrayList<ISelectorArgument>()

    /**
     * 通过给定的命令发送者和目标选择器文本匹配实体
     * @param sender 命令发送者
     * @param token 目标选择器文本
     * @return 目标实体
     */
    @Throws(SelectorSyntaxException::class)
    fun matchEntities(sender: CommandSender, token: String): MutableList<Entity> {
        val cachedMatches = MATCHES_CACHE.getIfPresent(token)
        //先从缓存确认不是非法选择器
        if (cachedMatches != null && !cachedMatches) throw SelectorSyntaxException("Malformed entity selector token")
        val matcher = ENTITY_SELECTOR.matcher(token)
        //非法目标选择器文本
        if (!matcher.matches()) {
            //记录非法选择器到缓存
            MATCHES_CACHE.put(token, false)
            throw SelectorSyntaxException("Malformed entity selector token")
        }
        //查询是否存在预解析结果。若不存在则解析
        var arguments = ARGS_CACHE.getIfPresent(token)
        if (arguments == null) {
            arguments = parseArgumentMap(matcher.group(2))
            ARGS_CACHE.put(token, arguments)
        }
        //获取克隆过的执行者位置信息
        val senderLocation = sender.transform
        //获取选择器类型
        val selectorType: SelectorType = SelectorType.parseSelectorType(matcher.group(1))
        //根据选择器类型先确定实体检测范围
        var entities: MutableList<Entity>
        entities = if (selectorType != SelectorType.SELF) {
            senderLocation.level.entities.values.toMutableList()
        } else {
            if (sender.isEntity) mutableListOf(sender.asEntity()!!)
            else return mutableListOf()
        }
        //若是NPC触发选择器，则只处理触发NPC对话的玩家
        if (selectorType == SelectorType.NPC_INITIATOR) {
            if (sender is NPCCommandSender) entities = mutableListOf(sender.initiator)
            else return mutableListOf()
        }
        //对于确定的玩家类型选择器，排除掉不是玩家的实体
        when (selectorType) {
            SelectorType.ALL_PLAYERS, SelectorType.NEAREST_PLAYER -> entities.removeIf { e: Entity? -> e !is Player }
            else -> Unit
        }
        //没符合条件的实体了，return
        if (entities.isEmpty()) return entities
        //参照坐标
        for (arg in orderedArgs) {
            try {
                if (!arg.isFilter) {
                    val defaultValue = arg.getDefaultValue(arguments, selectorType, sender)
                    val predicate = if (arguments.containsKey(arg.keyName)) arg.getPredicate(
                        selectorType, sender, senderLocation, *arguments[arg.keyName]!!
                            .toTypedArray<String>()
                    )
                    else if (defaultValue != null) arg.getPredicate(
                        selectorType,
                        sender,
                        senderLocation,
                        defaultValue
                    )
                    else continue
                    if (predicate == null) continue
                    entities.removeIf { entity: Entity -> !predicate.test(entity) }
                } else {
                    if (arguments.containsKey(arg.keyName)) entities = arg.getFilter(
                        selectorType, sender, senderLocation, *arguments[arg.keyName]!!
                            .toTypedArray<String>()
                    )!!.apply(entities).toMutableList()
                    else continue
                }
            } catch (t: Throwable) {
                throw SelectorSyntaxException("Error while parsing selector argument: " + arg.keyName, t)
            }
            //没符合条件的实体了，return
            if (entities.isEmpty()) return entities
        }
        //随机选择一个
        if (selectorType == SelectorType.RANDOM_PLAYER && entities.isNotEmpty()) {
            val index = ThreadLocalRandom.current().nextInt(entities.size) + 1
            var currentEntity: Entity? = null
            var i = 1
            for (localCurrent in entities) {
                if (i == index) {
                    currentEntity = localCurrent
                    break
                }
                i++
            }
            return if (currentEntity != null) mutableListOf(currentEntity) else emptyList<Entity>().toMutableList()
        }
        //选择最近玩家
        if (selectorType == SelectorType.NEAREST_PLAYER && entities.size != 1) {
            var nearest: Entity? = null
            var min = Double.MAX_VALUE
            for (entity in entities) {
                var distanceSquared: Double
                if ((senderLocation.position.distanceSquared(entity.position).also { distanceSquared = it }) < min) {
                    min = distanceSquared
                    nearest = entity
                }
            }
            entities = if (nearest != null) mutableListOf(nearest) else emptyList<Entity>().toMutableList()
        }
        return entities
    }

    /**
     * 检查给定文本是否是合法目标选择器
     * @param token 给定文本
     * @return 是否是合法目标选择器
     */
    fun checkValid(token: String): Boolean {
        return MATCHES_CACHE[token, { ENTITY_SELECTOR.matcher(token).matches() }]
    }

    /**
     * 注册一个选择器参数
     * @param argument 选择器参数对象
     * @return 是否注册成功（若已存在相同key值的选择器参数则注册失败，返回false）
     */
    fun registerArgument(argument: ISelectorArgument): Boolean {
        if (!registry.containsKey(argument.keyName)) {
            registry[argument.keyName] = argument
            orderedArgs.add(argument)
            orderedArgs.sort()
            return true
        }
        return false
    }

    @Throws(SelectorSyntaxException::class)
    protected fun parseArgumentMap(inputArguments: String?): Map<String, MutableList<String>> {
        val args: MutableMap<String, MutableList<String>> = Maps.newHashMap()

        if (inputArguments != null) {
            for (arg in separateArguments(inputArguments)) {
                val split = arg.split(ARGUMENT_JOINER, limit = 2)
                val argName = split[0]

                if (!registry.containsKey(argName)) {
                    throw SelectorSyntaxException("Unknown selector argument: $argName")
                }

                if (!args.containsKey(argName)) {
                    args[argName] =
                        mutableListOf(if (split.size > 1) split[1] else "")
                } else {
                    args[argName]!!.add(if (split.size > 1) split[1] else "")
                }
            }
        }

        return args
    }

    protected fun separateArguments(inputArguments: String): List<String> {
        var goOn = false
        val result: MutableList<String> = ArrayList()
        var start = 0

        var i = 0
        while (i < inputArguments.length) {
            if (inputArguments[i] == ',' && !goOn) {
                result.add(inputArguments.substring(start, i))
                start = i + 1
            }
            if (inputArguments[i] == '{') {
                goOn = true
            }
            if (inputArguments[i] == '}') {
                goOn = false
                i++
                result.add(inputArguments.substring(start, i))
                start = i + 1
            }
            i++
        }

        if (start < inputArguments.length) result.add(inputArguments.substring(start))

        return result.stream().filter { s: String -> s.isNotEmpty() }.collect(Collectors.toList())
    }

    companion object {
        val api: EntitySelectorAPI = EntitySelectorAPI()

        init {
            registerDefaultArguments()
        }

        val ENTITY_SELECTOR: Pattern = Pattern.compile("^@([aeprs]|initiator)(?:\\[(.*)])?$")
        const val ARGUMENT_JOINER: String = "="

        /**
         * 对目标选择器文本的预解析缓存
         */
        val ARGS_CACHE: Cache<String, Map<String, MutableList<String>>> =
            Caffeine.newBuilder().maximumSize(65535).expireAfterAccess(1, TimeUnit.MINUTES).build()
        val MATCHES_CACHE: Cache<String, Boolean> =
            Caffeine.newBuilder().maximumSize(65535).expireAfterAccess(1, TimeUnit.MINUTES).build()

        private fun registerDefaultArguments() {
            api.registerArgument(X())
            api.registerArgument(Y())
            api.registerArgument(Z())
            api.registerArgument(DX())
            api.registerArgument(DY())
            api.registerArgument(DZ())
            api.registerArgument(C())
            api.registerArgument(R())
            api.registerArgument(RM())
            api.registerArgument(Name())
            api.registerArgument(Tag())
            api.registerArgument(L())
            api.registerArgument(LM())
            api.registerArgument(M())
            api.registerArgument(Type())
            api.registerArgument(RX())
            api.registerArgument(RXM())
            api.registerArgument(RY())
            api.registerArgument(RYM())
            api.registerArgument(Scores())
        }
    }
}
