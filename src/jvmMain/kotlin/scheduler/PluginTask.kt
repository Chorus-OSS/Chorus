package org.chorus_oss.chorus.scheduler

import org.chorus_oss.chorus.plugin.Plugin


/**
 * 插件创建的任务。<br></br>Task that created by a plugin.
 *
 *
 * 对于插件作者，通过继承这个类创建的任务，可以在插件被禁用时不被执行。<br></br>
 * For plugin developers: Tasks that extend this class, won't be executed when the plugin is disabled.
 *
 *
 * 另外，继承这个类的任务可以通过[.getOwner]来获得这个任务所属的插件。<br></br>
 * Otherwise, tasks that extend this class can use [.getOwner] to get its owner.
 *
 * 下面是一个插件创建任务的例子：<br></br>An example for plugin create a task:
 * <pre>
 * public class ExampleTask extends PluginTask&lt;ExamplePlugin&gt;{
 * public ExampleTask(ExamplePlugin plugin){
 * super(plugin);
 * }
 *
 * `@Override`
 * public void onRun(int currentTick){
 * getOwner().getLogger().info("Task is executed in tick "+currentTick);
 * }
 * }
</pre> *
 *
 *
 * 如果要让Nukkit能够延时或循环执行这个任务，请使用[ServerScheduler]。<br></br>
 * If you want Nukkit to execute this task with delay or repeat, use [ServerScheduler].
 *
 * @param <T> 这个任务所属的插件。<br></br>The plugin that owns this task.
 *
</T> */
abstract class PluginTask<T : Plugin?>
/**
 * 构造一个插件拥有的任务的方法。<br></br>Constructs a plugin-owned task.
 *
 * @param owner 这个任务的所有者插件。<br></br>The plugin object that owns this task.
 *
 */(
    /**
     * 返回这个任务的所有者插件。<br></br>
     * Returns the owner of this task.
     *
     * @return 这个任务的所有者插件。<br></br>The plugin that owns this task.
     *
     */
    val owner: T
) : Task()
