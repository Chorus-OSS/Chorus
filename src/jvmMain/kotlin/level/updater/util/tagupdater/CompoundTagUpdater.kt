package org.chorus_oss.chorus.level.updater.util.tagupdater

import java.util.function.Consumer
import java.util.function.Predicate
import java.util.regex.Pattern

class CompoundTagUpdater(val version: Int) : Comparable<CompoundTagUpdater> {
    private val builder: Builder = Builder()
    private val filters: MutableList<Predicate<CompoundTagEditHelper>> = ArrayList()
    private val updaters: MutableList<Consumer<CompoundTagEditHelper>> = ArrayList()

    fun update(tag: MutableMap<String, Any?>): Boolean {
        val filterHelper = CompoundTagEditHelper(tag)
        for (filter in this.filters) {
            if (!filter.test(filterHelper)) {
                return false
            }
        }

        val updaterHelper = CompoundTagEditHelper(tag)
        for (updater in this.updaters) {
            updater.accept(updaterHelper)
        }
        return true
    }

    fun builder(): Builder {
        return builder
    }

    override fun compareTo(o: CompoundTagUpdater): Int {
        return Integer.compare(this.version, o.version)
    }

    private class TagNamePredicate(private val name: String) : Predicate<CompoundTagEditHelper> {
        override fun test(helper: CompoundTagEditHelper): Boolean {
            val tag = helper.tag
            return tag is Map<*, *> && (tag as Map<String?, Any?>).containsKey(name)
        }
    }

    private class TryAddPredicate(private val name: String) : Predicate<CompoundTagEditHelper> {
        override fun test(helper: CompoundTagEditHelper): Boolean {
            val tag = helper.tag
            return tag is Map<*, *> && !(tag as Map<String?, Any?>).containsKey(name)
        }
    }

    inner class Builder {
        fun addByte(name: String, value: Byte): Builder {
            filters.add(COMPOUND_FILTER)
            updaters.add(Consumer { helper: CompoundTagEditHelper ->
                helper.compoundTag[name] = value
            })
            return this
        }

        fun tryAdd(name: String, value: Any?): Builder {
            filters.add(TryAddPredicate(name))
            updaters.add(Consumer { helper: CompoundTagEditHelper ->
                helper.compoundTag[name] = value
            })
            return this
        }

        fun addInt(name: String, value: Int): Builder {
            filters.add(COMPOUND_FILTER)
            updaters.add(Consumer { helper: CompoundTagEditHelper ->
                helper.compoundTag[name] = value
            })
            return this
        }

        fun addCompound(name: String): Builder {
            filters.add(COMPOUND_FILTER)
            updaters.add(Consumer { helper: CompoundTagEditHelper ->
                helper.compoundTag[name] = LinkedHashMap<Any, Any>()
            })
            return this
        }

        fun edit(name: String, function: Consumer<CompoundTagEditHelper>): Builder {
            filters.add(TagNamePredicate(name))
            updaters.add(Consumer { helper: CompoundTagEditHelper -> helper.pushChild(name) })
            updaters.add(function)
            updaters.add(Consumer { obj: CompoundTagEditHelper -> obj.popChild() })
            return this
        }

        fun regex(name: String?, regex: String): Builder {
            return this.match(name, regex, true)
        }

        fun match(name: String?, match: String): Builder {
            return this.match(name, match, false)
        }

        fun match(name: String?, match: String, regex: Boolean): Builder {
            val pattern = if (regex) Pattern.compile(match) else null

            filters.add(Predicate<CompoundTagEditHelper> add@{ helper: CompoundTagEditHelper ->
                val tag = helper.tag as? Map<*, *> ?: return@add false
                val compound = tag as Map<String?, Any>
                if (!compound.containsKey(name)) {
                    return@add false
                }

                var success = match.isEmpty()
                if (success) {
                    return@add true
                }

                val matchTag = compound[name]
                success = if (regex) {
                    pattern!!.matcher(getTagValue(matchTag)).matches()
                } else {
                    match == getTagValue(matchTag)
                }
                success
            })
            return this
        }

        fun popVisit(): Builder {
            filters.add(Predicate<CompoundTagEditHelper> add@{ helper: CompoundTagEditHelper ->
                if (helper.canPopChild()) {
                    helper.popChild()
                    return@add true
                }
                false
            })
            updaters.add(Consumer { obj: CompoundTagEditHelper -> obj.popChild() })
            return this
        }

        fun remove(name: String): Builder {
            filters.add(TagNamePredicate(name))
            updaters.add(Consumer { helper: CompoundTagEditHelper ->
                helper.compoundTag.remove(name)
            })
            return this
        }

        fun rename(from: String, to: String): Builder {
            filters.add(TagNamePredicate(from))
            updaters.add(Consumer { helper: CompoundTagEditHelper ->
                val tag = helper.compoundTag
                tag[to] = tag.remove(from)
            })
            return this
        }

        fun tryEdit(name: String, function: Consumer<CompoundTagEditHelper>): Builder {
            updaters.add(Consumer { helper: CompoundTagEditHelper ->
                val tag = helper.tag
                if (tag is Map<*, *>) {
                    val compoundTag = tag as Map<String, Any>
                    if (compoundTag.containsKey(name)) {
                        helper.pushChild(name)
                        function.accept(helper)
                        helper.popChild()
                    }
                }
            })
            return this
        }

        fun visit(name: String?): Builder {
            filters.add(Predicate<CompoundTagEditHelper> add@{ helper: CompoundTagEditHelper ->
                val tag = helper.tag
                if (tag is Map<*, *> && (tag as Map<String?, Any?>).containsKey(name)) {
                    helper.pushChild(name)
                    return@add true
                }
                false
            })
            updaters.add(Consumer { helper: CompoundTagEditHelper -> helper.pushChild(name) })
            return this
        }

        fun build(): CompoundTagUpdater {
            return this@CompoundTagUpdater
        }
    }

    companion object {
        private val COMPOUND_FILTER =
            Predicate { helper: CompoundTagEditHelper -> helper.tag is Map<*, *> }

        private fun getTagValue(tag: Any?): String {
            return when (tag) {
                null -> "END"
                is Byte, is Short, is Int, is Long, is Float, is Double -> tag.toString()
                is String -> tag
                is Boolean -> if (tag == true) "1" else "0"
                else -> throw IllegalArgumentException("Invalid tag " + tag.javaClass.simpleName)
            }
        }
    }
}
