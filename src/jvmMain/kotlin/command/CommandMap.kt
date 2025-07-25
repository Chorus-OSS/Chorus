package org.chorus_oss.chorus.command

interface CommandMap {
    /**
     * 注册全部命令
     *
     * @param fallbackPrefix 命令标签前缀，当命令label重复时用于区分
     * @param commands       the commands
     */
    fun registerAll(fallbackPrefix: String, commands: List<Command>)

    /**
     * 注册命令
     *
     * @param fallbackPrefix 命令标签前缀，当命令label重复时用于区分
     * @param command        the command
     * @return 当命令label重复时返回false, 此时你无法使用label来获取和执行命令，不过你仍然可以使用fallbackPrefix:label来获取命令
     */
    fun register(fallbackPrefix: String, command: Command): Boolean

    /**
     * 注册命令
     *
     * @param fallbackPrefix 命令标签前缀，当命令label重复时用于区分
     * @param command        the command
     * @param label          the label
     * @return the boolean
     */
    fun register(fallbackPrefix: String, command: Command, label: String?): Boolean

    /**
     * 注册一个基于注解开发的命令
     *
     * @param object the object
     */
    fun registerSimpleCommands(`object`: Any)

    /**
     * 执行命令
     *
     * @param sender  the sender
     * @param cmdLine the cmd line
     * @return the int 返回0代表执行失败, 返回大于等于1代表执行成功<br></br>Returns 0 for failed execution, greater than or equal to 1 for successful execution
     */
    fun executeCommand(sender: CommandSender, cmdLine: String): Int

    /**
     * 清理全部的插件命令
     */
    fun clearCommands()

    /**
     * 从给定命令名称或者别名获取命令对象
     *
     * @param name the name
     * @return the command
     */
    fun getCommand(name: String): Command?
}
