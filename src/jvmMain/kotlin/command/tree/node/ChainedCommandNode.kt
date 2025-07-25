package org.chorus_oss.chorus.command.tree.node

import com.google.common.collect.Sets
import java.util.*
import java.util.function.Consumer

/**
 * [ExecuteCommand][org.chorus_oss.chorus.command.defaults.ExecuteCommand]命令的链命令节点
 */
class ChainedCommandNode : EnumNode() {
    private var remain = false

    private val TMP: MutableList<String> = ArrayList()

    override fun fill(arg: String) {
        var arg = arg
        if (arg.contains(" ")) {
            arg = "\"" + arg + "\""
        }
        if (!remain) {
            if (!CHAINED.contains(arg)) {
                this.error()
                return
            }
            TMP.add(arg)
            remain = true
        } else {
            if (paramList.index != paramList.paramTree.args.size) TMP.add(arg)
            else {
                TMP.add(arg)
                val join = StringJoiner(" ", "execute ", "")
                TMP.forEach(Consumer { newElement: String? -> join.add(newElement) })
                this.value = join.toString()
            }
        }
    }

    override fun reset() {
        super.reset()
        TMP.clear()
        this.remain = false
    }

    companion object {
        private val CHAINED: HashSet<String> = Sets.newHashSet(
            "run",
            "as",
            "at",
            "positioned",
            "if",
            "unless",
            "in",
            "align",
            "anchored",
            "rotated",
            "facing"
        )
    }
}
