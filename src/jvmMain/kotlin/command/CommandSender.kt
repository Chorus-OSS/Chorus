package org.chorus_oss.chorus.command

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.lang.CommandOutputContainer
import org.chorus_oss.chorus.lang.TextContainer
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Transform
import org.chorus_oss.chorus.permission.Permissible

/**
 * Who sends commands.
 * That can be a player or a console.
 *
 * @see org.chorus_oss.chorus.command.CommandExecutor.onCommand
 */
interface CommandSender : Permissible {
    /**
     * 给命令发送者返回信息.
     *
     *
     * Sends a message to the command sender.
     *
     * @param message 要发送的信息.<br></br>Message to send.
     * @see org.chorus_oss.chorus.utils.TextFormat
     *
     *
     */
    fun sendMessage(message: String)

    /**
     * 给命令发送者返回信息.
     *
     *
     * Sends a message to the command sender.
     *
     * @param message 要发送的信息.<br></br>Message to send.
     *
     */
    fun sendMessage(message: TextContainer)

    /**
     * Send command output.
     *
     * @param container the container
     */
    fun sendCommandOutput(container: CommandOutputContainer)

    /**
     * 返回命令发送者的名称.<br></br>
     * 如果命令发送者是一个玩家，将会返回他的玩家名字(name)不是显示名字(display name).<br></br>
     * 如果命令发送者是控制台，将会返回`"CONSOLE"`.<br></br>
     * 当你需要判断命令的执行者是不是控制台时，可以用这个：<br></br>
     * `if(sender instanceof ConsoleCommandSender) .....;`
     *
     *
     * Returns the name of the command sender.<br></br>
     * If this command sender is a player, will return his/her player name(not display name).<br></br>
     * If it is a console, will return `"CONSOLE"`.<br></br>
     * When you need to determine if the sender is a console, use this:<br></br>
     * `if(sender instanceof ConsoleCommandSender) .....;`
     *
     * @return 命令发送者的名称.<br></br>the name of the command sender.
     * @see org.chorus_oss.chorus.Player.getEntityName
     * @see org.chorus_oss.chorus.command.ConsoleCommandSender.getName
     * @see org.chorus_oss.chorus.plugin.PluginTOML
     *
     *
     */
    val senderName: String

    /**
     * @return 发送者是否为玩家<br></br>whether the sender is a player
     */
    val isPlayer: Boolean

    val isEntity: Boolean
        /**
         * 请使用这个方法来检查发送者是否是一个实体，而不是使用代码`"xxx instanceof Entity"`.<br></br>
         * 因为发送者可能不是`"Entity"`的一个实例，但实际上它是以一个实体的身份执行命令(例如：`"ExecutorCommandSender"`)
         *
         *
         * please use this method to check whether the sender is an entity instead of using code `"xxx instanceof Entity"` <br></br>
         * because the sender may not an instance of `"Entity"` but in fact it is executing commands identity as an entity(eg: `"ExecutorCommandSender"`)
         *
         * @return 发送者是否为实体<br></br>whether the sender is an entity
         */
        get() = false

    /**
     * 如果发送者是一个实体，返回执行该命令的实体.
     *
     *
     * return the entity who execute the command if the sender is a entity.
     *
     * @return 实体对象<br></br>Entity instance
     */
    fun asEntity(): Entity? {
        return null
    }

    /**
     * 如果发送者是一个玩家，返回执行该命令的玩家.
     *
     *
     * return the player who execute the command if the sender is a player.
     *
     * @return 玩家对象<br></br>Player instance
     */
    fun asPlayer(): Player? {
        return null
    }

    val locator: Locator

    val transform: Transform
}
