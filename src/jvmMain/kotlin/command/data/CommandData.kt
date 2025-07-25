package org.chorus_oss.chorus.command.data

import java.util.*

class CommandData : Cloneable {
    @JvmField
    var description: String = "description"

    @JvmField
    var aliases: CommandEnum? = null

    @JvmField
    var overloads: MutableMap<String, CommandOverload> = HashMap()

    @JvmField
    var flags: EnumSet<Flag> = EnumSet.of(Flag.NOT_CHEAT)

    @JvmField
    var permission: Int = 0


    @JvmField
    var subcommands: List<ChainedSubCommandData> = ArrayList()

    public override fun clone(): CommandData {
        return try {
            super.clone() as CommandData
        } catch (e: Exception) {
            CommandData()
        }
    }

    enum class Flag(@JvmField val bit: Int) {
        NONE(0x00),
        TEST_USAGE(0x01),
        HIDDEN_FROM_COMMAND_BLOCK(0x02),
        HIDDEN_FROM_PLAYER(0x04),
        HIDDEN(0x06),
        HIDDEN_FROM_AUTOMATION(0x08),
        REMOVED(0xe),
        LOCAL_SYNC(0x10),
        EXECUTE_DISALLOWED(0x20),
        MESSAGE_TYPE(0x40),
        NOT_CHEAT(0x80),
        ASYNC(0x100),
        EDITOR(0x200)
    }
}
