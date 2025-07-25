package org.chorus_oss.chorus.command.selector.args.impl

import org.chorus_oss.chorus.command.selector.args.ISelectorArgument

/**
 * args like x,y,z.
 */
abstract class CoordinateArgument : ISelectorArgument {
    override val priority: Int
        get() = 1
}
