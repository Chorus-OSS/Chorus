package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.plugin.InternalPlugin

interface ICommandBlock : CommandSender, InventoryHolder {
    override val senderName: String

    fun hasName(): Boolean

    fun setPowered() {
        this.isPowered = true
    }

    var isPowered: Boolean

    fun trigger(chain: Int = 0): Boolean {
        /*if (this.getLevel().getGameRules().getInteger(GameRule.MAX_COMMAND_CHAIN_LENGTH) < chain) {
            return false;
        }*/

        val delay = this.tickDelay
        if (delay > 0) {
            level!!.scheduler.scheduleDelayedTask(InternalPlugin.INSTANCE, CommandBlockTrigger(this, chain), delay)
            return false
        }

        return this.execute(chain)
    }

    fun execute(): Boolean {
        return this.execute(0)
    }

    fun execute(chain: Int): Boolean

    val mode: Int

    var command: String?

    var isAuto: Boolean

    var isConditional: Boolean

    val isConditionMet: Boolean

    fun setConditionMet(): Boolean

    var successCount: Int

    var lastExecution: Long

    val isTrackingOutput: Boolean

    fun setTrackOutput(track: Boolean)

    var lastOutput: String?

    var lastOutputCommandMode: Int

    var isLastOutputCondionalMode: Boolean

    var isLastOutputRedstoneMode: Boolean

    var tickDelay: Int

    var isExecutingOnFirstTick: Boolean

    class CommandBlockTrigger(private val commandBlock: ICommandBlock, private val chain: Int) : Runnable {
        override fun run() {
            commandBlock.execute(this.chain)
        }
    }

    companion object {
        const val CURRENT_VERSION: Int = 10

        // TODO: enum
        const val MODE_NORMAL: Int = 0
        const val MODE_REPEATING: Int = 1
        const val MODE_CHAIN: Int = 2

        const val TAG_CONDITIONAL_MODE: String = "conditionalMode"
        const val TAG_AUTO: String = "auto"
        const val TAG_POWERED: String = "powered"
        const val TAG_CUSTOM_NAME: String = "CustomName"
        const val TAG_COMMAND: String = "Command"
        const val TAG_LAST_EXECUTION: String = "LastExecution"
        const val TAG_TRACK_OUTPUT: String = "TrackOutput"
        const val TAG_LAST_OUTPUT: String = "LastOutput"
        const val TAG_LAST_OUTPUT_PARAMS: String = "LastOutputParams"
        const val TAG_LP_COMMAND_MODE: String = "LPCommandMode"
        const val TAG_LP_CONDIONAL_MODE: String = "LPCondionalMode"
        const val TAG_LP_REDSTONE_MODE: String = "LPRedstoneMode"
        const val TAG_SUCCESS_COUNT: String = "SuccessCount"
        const val TAG_CONDITION_MET: String = "conditionMet"
        const val TAG_VERSION: String = "Version"
        const val TAG_TICK_DELAY: String = "TickDelay"
        const val TAG_EXECUTE_ON_FIRST_TICK: String = "ExecuteOnFirstTick"
    }
}
