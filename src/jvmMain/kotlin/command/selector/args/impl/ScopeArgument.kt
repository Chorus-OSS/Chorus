package org.chorus_oss.chorus.command.selector.args.impl

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.selector.SelectorType
import org.chorus_oss.chorus.command.selector.args.ISelectorArgument

/**
 * args like dx,dy,dz.
 */
abstract class ScopeArgument : ISelectorArgument {
    override val priority: Int
        get() = 2

    override fun getDefaultValue(
        values: Map<String, List<String>>,
        selectorType: SelectorType,
        sender: CommandSender?
    ): String? {
        if (values.containsKey("dx") || values.containsKey("dy") || values.containsKey("dz")) return "0"
        return null
    }
}
