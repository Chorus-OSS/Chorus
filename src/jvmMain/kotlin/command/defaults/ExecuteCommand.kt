package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.ExecutorCommandSender
import org.chorus_oss.chorus.command.data.*
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Transform
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BVector3
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.scoreboard.scorer.EntityScorer
import org.chorus_oss.chorus.scoreboard.scorer.PlayerScorer
import org.chorus_oss.chorus.utils.Utils
import java.util.regex.Pattern
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class ExecuteCommand(name: String) : VanillaCommand(name, "commands.execute.description", "commands.execute.usage") {
    init {
        this.permission = "chorus.command.execute"
        commandParameters.clear()
        this.addCommandParameters(
            "as", arrayOf(
                CommandParameter.Companion.newEnum("subcommand", false, CommandEnum("Option_As", "as")),
                GenericParameter.Companion.ORIGIN.get(false),
                GenericParameter.Companion.CHAINED_COMMAND.get(false)
            )
        )
        this.addCommandParameters(
            "at", arrayOf(
                CommandParameter.Companion.newEnum("subcommand", false, CommandEnum("Option_At", "at")),
                GenericParameter.Companion.ORIGIN.get(false),
                GenericParameter.Companion.CHAINED_COMMAND.get(false)
            )
        )
        this.addCommandParameters(
            "in", arrayOf(
                CommandParameter.Companion.newEnum("subcommand", false, CommandEnum("Option_In", "in")),
                CommandParameter.Companion.newType("dimension", CommandParamType.STRING),
                GenericParameter.Companion.CHAINED_COMMAND.get(false)
            )
        )
        this.addCommandParameters(
            "facing", arrayOf(
                CommandParameter.Companion.newEnum("subcommand", false, CommandEnum("Option_Facing", "facing")),
                CommandParameter.Companion.newType("pos", CommandParamType.POSITION),
                GenericParameter.Companion.CHAINED_COMMAND.get(false)
            )
        )
        this.addCommandParameters(
            "facing-entity", arrayOf(
                CommandParameter.Companion.newEnum("subcommand", false, CommandEnum("Option_Facing", "facing")),
                CommandParameter.Companion.newEnum(
                    "secondary subcommand",
                    false,
                    CommandEnum("Option_Entity", "entity")
                ),
                CommandParameter.Companion.newType("targets", CommandParamType.TARGET),
                CommandParameter.Companion.newEnum("anchor", arrayOf("eyes", "feet")),
                GenericParameter.Companion.CHAINED_COMMAND.get(false)
            )
        )
        this.addCommandParameters(
            "rotated", arrayOf(
                CommandParameter.Companion.newEnum("subcommand", false, CommandEnum("Option_Rotated", "rotated")),
                CommandParameter.Companion.newType("yaw", false, CommandParamType.VALUE),
                CommandParameter.Companion.newType("pitch", false, CommandParamType.VALUE),
                GenericParameter.Companion.CHAINED_COMMAND.get(false)
            )
        )
        this.addCommandParameters(
            "rotated as", arrayOf(
                CommandParameter.Companion.newEnum("subcommand", false, CommandEnum("Option_Rotated", "rotated")),
                CommandParameter.Companion.newEnum("secondary subcommand", false, CommandEnum("Option_As", "as")),
                CommandParameter.Companion.newType("targets", CommandParamType.TARGET),
                GenericParameter.Companion.CHAINED_COMMAND.get(false)
            )
        )
        this.addCommandParameters(
            "align", arrayOf(
                CommandParameter.Companion.newEnum("subcommand", false, CommandEnum("Option_Align", "align")),
                CommandParameter.Companion.newType("axes", CommandParamType.STRING),
                GenericParameter.Companion.CHAINED_COMMAND.get(false)
            )
        )
        this.addCommandParameters(
            "anchored", arrayOf(
                CommandParameter.Companion.newEnum("subcommand", false, CommandEnum("Option_Anchored", "anchored")),
                CommandParameter.Companion.newEnum("anchor", arrayOf("eyes", "feet")),
                GenericParameter.Companion.CHAINED_COMMAND.get(false)
            )
        )
        this.addCommandParameters(
            "positioned", arrayOf(
                CommandParameter.Companion.newEnum("subcommand", false, CommandEnum("Option_Positioned", "positioned")),
                CommandParameter.Companion.newType("position", CommandParamType.POSITION),
                GenericParameter.Companion.CHAINED_COMMAND.get(false)
            )
        )
        this.addCommandParameters(
            "positioned as", arrayOf(
                CommandParameter.Companion.newEnum("subcommand", false, CommandEnum("Option_Positioned", "positioned")),
                CommandParameter.Companion.newEnum("secondary subcommand", false, CommandEnum("Option_As", "as")),
                GenericParameter.Companion.ORIGIN.get(false),
                GenericParameter.Companion.CHAINED_COMMAND.get(false)
            )
        )
        this.addCommandParameters(
            "if-unless-block", arrayOf(
                CommandParameter.Companion.newEnum(
                    "subcommand",
                    false,
                    CommandEnum("Option_If_Unless", "if", "unless")
                ),
                CommandParameter.Companion.newEnum("secondary subcommand", false, CommandEnum("Option_Block", "block")),
                CommandParameter.Companion.newType("position", CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newEnum("block", false, CommandEnum.Companion.ENUM_BLOCK),
                GenericParameter.Companion.CHAINED_COMMAND.get(true)
            )
        )
        /*todo 暂时没实现，因为我也不知道这个blockStates填什么
        this.addCommandParameters("if-unless-block-blockStates", new CommandParameter[]{
                CommandParameter.newEnum("subcommand", false, new CommandEnum("Option_If_Unless", "if", "unless")),
                CommandParameter.newEnum("secondary subcommand", false, new CommandEnum("Option_Block", "block")),
                CommandParameter.newType("position", CommandParamType.BLOCK_POSITION),
                CommandParameter.newEnum("block", false, CommandEnum.ENUM_BLOCK),
                CommandParameter.newType("blockStates", CommandParamType.BLOCK_STATES),
                CommandParameter.newEnum("chainedCommand", false, CHAINED_COMMAND_ENUM, new ChainedCommandNode(),CommandParamOption.ENUM_AS_CHAINED_COMMAND)
        });*/
        this.addCommandParameters(
            "if-unless-block-data", arrayOf(
                CommandParameter.Companion.newEnum(
                    "subcommand",
                    false,
                    CommandEnum("Option_If_Unless", "if", "unless")
                ),
                CommandParameter.Companion.newEnum("secondary subcommand", false, CommandEnum("Option_Block", "block")),
                CommandParameter.Companion.newType("position", CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newEnum("block", false, CommandEnum.Companion.ENUM_BLOCK),
                CommandParameter.Companion.newType("data", CommandParamType.INT),
                GenericParameter.Companion.CHAINED_COMMAND.get(true)
            )
        )
        this.addCommandParameters(
            "if-unless-blocks", arrayOf(
                CommandParameter.Companion.newEnum(
                    "subcommand",
                    false,
                    CommandEnum("Option_If_Unless", "if", "unless")
                ),
                CommandParameter.Companion.newEnum(
                    "secondary subcommand",
                    false,
                    CommandEnum("Option_Blocks", "blocks")
                ),
                CommandParameter.Companion.newType("begin", CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newType("end", CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newType("destination", CommandParamType.BLOCK_POSITION),
                CommandParameter.Companion.newEnum("scan mode", true, arrayOf("all", "masked")),
                GenericParameter.Companion.CHAINED_COMMAND.get(true)
            )
        )
        this.addCommandParameters(
            "if-unless-entity", arrayOf(
                CommandParameter.Companion.newEnum(
                    "subcommand",
                    false,
                    CommandEnum("Option_If_Unless", "if", "unless")
                ),
                CommandParameter.Companion.newEnum(
                    "secondary subcommand",
                    false,
                    CommandEnum("Option_Entity", "entity")
                ),
                CommandParameter.Companion.newType("target", CommandParamType.TARGET),
                GenericParameter.Companion.CHAINED_COMMAND.get(true)
            )
        )
        this.addCommandParameters(
            "if-unless-score", arrayOf(
                CommandParameter.Companion.newEnum(
                    "subcommand",
                    false,
                    CommandEnum("Option_If_Unless", "if", "unless")
                ),
                CommandParameter.Companion.newEnum("secondary subcommand", false, CommandEnum("Option_Score", "score")),
                CommandParameter.Companion.newType("target", CommandParamType.TARGET),
                CommandParameter.Companion.newEnum(
                    "objective",
                    false,
                    CommandEnum("ScoreboardObjectives", listOf<String>(), true)
                ),
                CommandParameter.Companion.newType("operation", CommandParamType.COMPARE_OPERATOR),
                CommandParameter.Companion.newType("source", CommandParamType.TARGET),
                CommandParameter.Companion.newEnum(
                    "objective",
                    false,
                    CommandEnum("ScoreboardObjectives", listOf<String>(), true)
                ),
                GenericParameter.Companion.CHAINED_COMMAND.get(true)
            )
        )
        this.addCommandParameters(
            "if-unless-score-matches", arrayOf(
                CommandParameter.Companion.newEnum(
                    "subcommand",
                    false,
                    CommandEnum("Option_If_Unless", "if", "unless")
                ),
                CommandParameter.Companion.newEnum("secondary subcommand", false, CommandEnum("Option_Score", "score")),
                CommandParameter.Companion.newType("target", CommandParamType.TARGET),
                CommandParameter.Companion.newEnum(
                    "objective",
                    false,
                    CommandEnum("ScoreboardObjectives", listOf<String>(), true)
                ),
                CommandParameter.Companion.newEnum("matches", arrayOf("matches")),
                CommandParameter.Companion.newType("range", CommandParamType.STRING),
                GenericParameter.Companion.CHAINED_COMMAND.get(true)
            )
        )
        this.addCommandParameters(
            "run", arrayOf(
                CommandParameter.Companion.newEnum("subcommand", false, CommandEnum("Option_Run", "run")),
                CommandParameter.Companion.newType(
                    "command",
                    false,
                    CommandParamType.COMMAND,
                    CommandParamOption.HAS_SEMANTIC_CONSTRAINT
                )
            )
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        var num = 0
        val list = result.value
        when (result.key) {
            "run" -> {
                val command = list.getResult<String>(1)
                if (command!!.isBlank()) return 0
                return Server.instance.executeCommand(sender, command)
            }

            "as" -> {
                val executors = list.getResult<List<Entity>>(1)!!
                if (executors.isEmpty()) {
                    log.addNoTargetMatch().output()
                    return 0
                }
                val chainCommand = list.getResult<String>(2)!!
                for (executor in executors) {
                    val executorCommandSender = ExecutorCommandSender(sender, executor, executor.transform)
                    val n = Server.instance.executeCommand(executorCommandSender, chainCommand)
                    if (n == 0) {
                        val names = mutableListOf<String>()
                        val match = ERROR_COMMAND_NAME.matcher(chainCommand)
                        while (match.find()) {
                            names.add(match.group())
                        }
                        names.reverse()
                        for (name in names) {
                            log.addError("commands.execute.failed", name, executor.getEntityName())
                        }
                    } else num += n
                }
                log.output()
                return num
            }

            "at" -> {
                val locations = list.getResult<List<Entity>>(1)!!
                if (locations.isEmpty()) {
                    log.addNoTargetMatch().output()
                    return 0
                }
                val chainCommand = list.getResult<String>(2)!!
                for (transform in locations.stream().map<Transform> { obj: Entity -> obj.transform }.toList()) {
                    val executorCommandSender = ExecutorCommandSender(sender, sender.asEntity(), transform)
                    num += Server.instance.executeCommand(executorCommandSender, chainCommand)
                }
                return num
            }

            "in" -> {
                val levelName = list.getResult<String>(1)!!
                val level = Server.instance.getLevelByName(levelName) ?: return 0
                val chainCommand = list.getResult<String>(2)!!
                val transform = sender.transform
                transform.setLevel(level)
                val executorCommandSender = ExecutorCommandSender(sender, sender.asEntity(), transform)
                return Server.instance.executeCommand(executorCommandSender, chainCommand)
            }

            "facing" -> {
                val pos = list.getResult<Vector3>(1)
                val chainCommand = list.getResult<String>(2)!!
                val source = sender.transform
                val bv =
                    BVector3.fromPos(pos!!.x - source.position.x, pos.y - source.position.y, pos.z - source.position.z)
                source.setPitch(bv.pitch)
                source.setYaw(bv.yaw)
                val executorCommandSender = ExecutorCommandSender(sender, sender.asEntity(), source)
                return Server.instance.executeCommand(executorCommandSender, chainCommand)
            }

            "facing-entity" -> {
                val targets = list.getResult<List<Entity>>(2)!!
                if (targets.isEmpty()) {
                    log.addNoTargetMatch().output()
                    return 0
                }
                val anchor = list.getResult<String>(3)
                val anchorAtEyes = anchor == "eyes"
                val chainCommand = list.getResult<String>(4)!!
                for (target in targets) {
                    val source = sender.transform
                    val bv = BVector3.fromPos(
                        target.position.x - source.position.x,
                        target.position.y + (if (anchorAtEyes) target.getEyeHeight() else 0f) - source.position.y,
                        target.position.z - source.position.z
                    )
                    source.setPitch(bv.pitch)
                    source.setYaw(bv.yaw)
                    val executorCommandSender = ExecutorCommandSender(sender, sender.asEntity(), source)
                    num += Server.instance.executeCommand(executorCommandSender, chainCommand)
                }
                return num
            }

            "rotated" -> {
                var yaw = sender.transform.rotation.yaw
                if (list.hasResult(1)) yaw = list.getResult(1)!!
                var pitch = sender.transform.rotation.pitch
                if (list.hasResult(2)) pitch = list.getResult(2)!!
                val chainCommand = list.getResult<String>(3)!!
                val transform = sender.transform
                transform.setYaw(yaw)
                transform.setPitch(pitch)
                val executorCommandSender = ExecutorCommandSender(sender, sender.asEntity(), transform)
                return Server.instance.executeCommand(executorCommandSender, chainCommand)
            }

            "rotated as" -> {
                val executors = list.getResult<List<Entity>>(2)!!
                if (executors.isEmpty()) {
                    log.addNoTargetMatch().output()
                    return 0
                }
                val chainCommand = list.getResult<String>(3)!!
                for (executor in executors) {
                    val transform = sender.transform
                    transform.setYaw(executor.rotation.yaw)
                    transform.setPitch(executor.rotation.pitch)
                    val executorCommandSender = ExecutorCommandSender(sender, sender.asEntity(), transform)
                    num += Server.instance.executeCommand(executorCommandSender, chainCommand)
                }
                return num
            }

            "align" -> {
                val axes = list.getResult<String>(1)!!
                val chainCommand = list.getResult<String>(2)!!
                val transform = sender.transform
                for (c in axes.toCharArray()) {
                    when (c) {
                        'x' -> transform.position.x = transform.position.floorX.toDouble()
                        'y' -> transform.position.y = transform.position.floorY.toDouble()
                        'z' -> transform.position.z = transform.position.floorZ.toDouble()
                    }
                }
                val executorCommandSender = ExecutorCommandSender(sender, sender.asEntity(), transform)
                return Server.instance.executeCommand(executorCommandSender, chainCommand)
            }

            "anchored" -> {
                if (!sender.isEntity) return 0
                var transform = sender.transform
                val anchor = list.getResult<String>(1)
                val chainCommand = list.getResult<String>(2)!!
                when (anchor) {
                    "feet" -> {
                        //todo do nothing
                    }

                    "eyes" -> transform = transform.add(0.0, sender.asEntity()!!.getEyeHeight().toDouble(), 0.0)
                }
                val executorCommandSender = ExecutorCommandSender(sender, sender.asEntity(), transform)
                return Server.instance.executeCommand(executorCommandSender, chainCommand)
            }

            "positioned" -> {
                val vec = list.getResult<Vector3>(1)!!
                val newLoc = sender.transform
                newLoc.setX(vec.x)
                newLoc.setY(vec.y)
                newLoc.setZ(vec.z)
                val chainCommand = list.getResult<String>(2)!!
                val executorCommandSender = ExecutorCommandSender(sender, sender.asEntity(), newLoc)
                return Server.instance.executeCommand(executorCommandSender, chainCommand)
            }

            "positioned as" -> {
                val targets = list.getResult<List<Entity>>(2)!!
                if (targets.isEmpty()) {
                    log.addNoTargetMatch().output()
                    return 0
                }
                val chainCommand = list.getResult<String>(3)!!
                for (vec in targets.stream().map { e: Entity -> e.position }.toList()) {
                    val newLoc = sender.transform
                    newLoc.setX(vec.x)
                    newLoc.setY(vec.y)
                    newLoc.setZ(vec.z)
                    val executorCommandSender = ExecutorCommandSender(sender, sender.asEntity(), newLoc)
                    num += Server.instance.executeCommand(executorCommandSender, chainCommand)
                }
                return num
            }

            "if-unless-block" -> {
                val pos = list.getResult<Locator>(2)
                val block = pos!!.levelBlock
                val blockName = list.getResult<Block>(3)
                val id = blockName!!.id
                val isIF = list.getResult<String>(0)!!

                val matched = block.id === id
                val shouldMatch = isIF == "if"
                val condition = (matched && shouldMatch) || (!matched && !shouldMatch)

                if (list.hasResult(4) && condition) {
                    val chainCommand = list.getResult<String>(4)!!
                    return Server.instance.executeCommand(sender, chainCommand)
                } else if (condition) {
                    log.addSuccess("commands.execute.trueCondition").output()
                    return 1
                } else {
                    log.addError("commands.execute.falseCondition", isIF, "block")
                    return 0
                }
            }

            "if-unless-block-data" -> {
                val pos = list.getResult<Locator>(2)
                val block = pos!!.levelBlock
                val blockName = list.getResult<Block>(3)
                val id = blockName!!.id
                val data = list.getResult<Int>(4)!!
                val isIF = list.getResult<String>(0)!!

                val matched = id === block.id && (data == -1 || data == block.blockState.specialValue().toInt())
                val shouldMatch = isIF == "if"
                val condition = (matched && shouldMatch) || (!matched && !shouldMatch)

                if (list.hasResult(5) && condition) {
                    val chainCommand = list.getResult<String>(5)!!
                    return Server.instance.executeCommand(sender, chainCommand)
                } else if (condition) {
                    log.addSuccess("commands.execute.trueCondition").output()
                    return 1
                } else {
                    log.addError("commands.execute.falseCondition", isIF, "block")
                    return 0
                }
            }

            "if-unless-blocks" -> {
                val isIF = list.getResult<String>(0)!!
                val shouldMatch = isIF == "if"
                val begin = list.getResult<Locator>(2)
                val end = list.getResult<Locator>(3)
                val destination = list.getResult<Locator>(4)
                var mode = TestForBlocksCommand.TestForBlocksMode.ALL
                if (list.hasResult(5)) {
                    val str5 = list.getResult<String>(5)
                    mode = TestForBlocksCommand.TestForBlocksMode.valueOf(str5!!.uppercase())
                }

                val blocksAABB: AxisAlignedBB = SimpleAxisAlignedBB(
                    min(begin!!.x, end!!.x), min(
                        begin.y, end.y
                    ), min(begin.z, end.z), max(begin.x, end.x), max(
                        begin.y, end.y
                    ), max(begin.z, end.z)
                )
                val size =
                    floor((blocksAABB.maxX - blocksAABB.minX + 1) * (blocksAABB.maxY - blocksAABB.minY + 1) * (blocksAABB.maxZ - blocksAABB.minZ + 1))

                if (size > 16 * 16 * 256 * 8) {
                    log.addError("commands.fill.tooManyBlocks", size.toString(), (16 * 16 * 256 * 8).toString())
                        .addError("Operation will continue, but too many blocks may cause stuttering")
                        .successCount(2).output()
                }

                val to = Vector3(
                    destination!!.x + (blocksAABB.maxX - blocksAABB.minX),
                    destination.y + (blocksAABB.maxY - blocksAABB.minY),
                    destination.z + (blocksAABB.maxZ - blocksAABB.minZ)
                )
                val destinationAABB: AxisAlignedBB = SimpleAxisAlignedBB(
                    min(destination.x, to.x), min(
                        destination.y, to.y
                    ), min(destination.z, to.z), max(
                        destination.x, to.x
                    ), max(destination.y, to.y), max(
                        destination.z, to.z
                    )
                )

                if (blocksAABB.minY < 0 || blocksAABB.maxY > 255 || destinationAABB.minY < 0 || destinationAABB.maxY > 255) {
                    log.addError("commands.testforblock.outOfWorld").output()
                    return 0
                }

                val level = begin.level

                var sourceChunkX = floor(blocksAABB.minX).toInt() shr 4
                var destinationChunkX = floor(destinationAABB.minX).toInt() shr 4
                while (sourceChunkX <= floor(blocksAABB.maxX).toInt() shr 4) {
                    var sourceChunkZ = floor(blocksAABB.minZ).toInt() shr 4
                    var destinationChunkZ = floor(destinationAABB.minZ).toInt() shr 4
                    while (sourceChunkZ <= floor(blocksAABB.maxZ).toInt() shr 4) {
                        if (level.getChunkIfLoaded(sourceChunkX, sourceChunkZ) == null) {
                            log.addError("commands.testforblock.outOfWorld").output()
                            return 0
                        }
                        if (level.getChunkIfLoaded(destinationChunkX, destinationChunkZ) == null) {
                            log.addError("commands.testforblock.outOfWorld").output()
                            return 0
                        }
                        sourceChunkZ++
                        destinationChunkZ++
                    }
                    sourceChunkX++
                    destinationChunkX++
                }

                val blocks = Utils.getLevelBlocks(level, blocksAABB)
                val destinationBlocks = Utils.getLevelBlocks(level, destinationAABB)
                var count = 0

                var matched = true

                when (mode) {
                    TestForBlocksCommand.TestForBlocksMode.ALL -> {
                        var i = 0
                        while (i < blocks.size) {
                            val block = blocks[i]
                            val destinationBlock = destinationBlocks[i]

                            if (block.equalsBlock(destinationBlock)) {
                                ++count
                            } else {
                                log.addError("commands.compare.failed").output()
                                matched = false
                                break
                            }
                            i++
                        }
                    }

                    TestForBlocksCommand.TestForBlocksMode.MASKED -> {
                        var i = 0
                        while (i < blocks.size) {
                            val block = blocks[i]
                            val destinationBlock = destinationBlocks[i]

                            if (block.equalsBlock(destinationBlock)) {
                                ++count
                            } else if (block.id !== BlockID.AIR) {
                                log.addError("commands.compare.failed").output()
                                matched = false
                                break
                            }
                            i++
                        }
                    }

                }

                log.addSuccess("commands.compare.success", count.toString()).output()

                val condition = (matched && shouldMatch) || (!matched && !shouldMatch)
                if (list.hasResult(6) && condition) {
                    val chainCommand = list.getResult<String>(6)!!
                    return Server.instance.executeCommand(sender, chainCommand)
                } else if (condition) {
                    log.addSuccess("commands.execute.trueConditionWithCount", count.toString()).output()
                    return count
                } else {
                    log.addError("commands.execute.falseConditionWithCount", isIF, "blocks", count.toString())
                    return 0
                }
            }

            "if-unless-entity" -> {
                val isIF = list.getResult<String>(0)!!
                val shouldMatch = isIF == "if"
                val targets = list.getResult<List<Entity>>(2)!!
                val found = !targets.isEmpty()
                val condition = (found && shouldMatch) || (!found && !shouldMatch)
                if (list.hasResult(3) && condition) {
                    val chainCommand = list.getResult<String>(3)!!
                    return Server.instance.executeCommand(sender, chainCommand)
                } else if (condition) {
                    log.addSuccess("commands.execute.trueCondition").output()
                    return 1
                } else {
                    log.addError("commands.execute.falseCondition", isIF, "entity")
                    return 0
                }
            }

            "if-unless-score" -> {
                val matched: Boolean
                val isIF = list.getResult<String>(0)!!
                val shouldMatch = isIF == "if"
                val manager = Server.instance.scoreboardManager

                val targets = list.getResult<List<Entity>>(2)!!
                val targetScorers = targets.map { t ->
                    if (t is Player) PlayerScorer(t) else EntityScorer(t)
                }.toSet()
                if (targetScorers.size > 1) {
                    log.addTooManyTargets().output()
                    return 0
                }
                if (targetScorers.isEmpty()) {
                    log.addNoTargetMatch().output()
                    return 0
                }
                val targetScorer = targetScorers.iterator().next()

                val targetObjectiveName = list.getResult<String>(3)!!

                val targetScoreboard = manager.scoreboards[targetObjectiveName] ?: run {
                    log.addError("commands.scoreboard.objectiveNotFound", targetObjectiveName).output()
                    return 0
                }

                val operation = list.getResult<String>(4)
                val scorers = list.getResult<List<Entity>>(5)!!
                val selectorScorers = scorers.map { t ->
                    if (t is Player) PlayerScorer(t) else EntityScorer(t)
                }.toSet()
                if (selectorScorers.size > 1) {
                    log.addTooManyTargets().output()
                    return 0
                }
                if (selectorScorers.isEmpty()) {
                    log.addNoTargetMatch().output()
                    return 0
                }
                val sourceScorer = selectorScorers.iterator().next()

                val sourceObjectiveName = list.getResult<String>(6)!!

                val sourceScoreboard = manager.scoreboards[sourceObjectiveName] ?: run {
                    log.addError("commands.scoreboard.objectiveNotFound", sourceObjectiveName).output()
                    return 0
                }

                if (!sourceScoreboard.lines.containsKey(sourceScorer)) {
                    log.addError(
                        "commands.scoreboard.players.operation.notFound",
                        sourceObjectiveName,
                        sourceScorer.name
                    ).output()
                    return 0
                }

                val targetScore = targetScoreboard.lines[targetScorer]!!.score
                val sourceScore = sourceScoreboard.lines[sourceScorer]!!.score

                matched = when (operation) {
                    "<" -> targetScore < sourceScore
                    "<=" -> targetScore <= sourceScore
                    "=" -> targetScore == sourceScore
                    ">=" -> targetScore >= sourceScore
                    ">" -> targetScore > sourceScore
                    else -> false
                }

                val condition = (matched && shouldMatch) || (!matched && !shouldMatch)
                if (list.hasResult(7) && condition) {
                    val chainCommand = list.getResult<String>(7)!!
                    return Server.instance.executeCommand(sender, chainCommand)
                } else if (condition) {
                    log.addSuccess("commands.execute.trueCondition").output()
                    return 1
                } else {
                    log.addError("commands.execute.falseCondition", isIF, "score")
                    return 0
                }
            }

            "if-unless-score-matches" -> {
                val matched: Boolean
                val isIF = list.getResult<String>(0)!!
                val shouldMatch = isIF == "if"
                val manager = Server.instance.scoreboardManager

                val targets = list.getResult<List<Entity>>(2)!!
                val targetScorers = targets.map { t ->
                    if (t is Player) PlayerScorer(t) else EntityScorer(t)
                }.toSet()

                if (targetScorers.size > 1) {
                    log.addTooManyTargets().output()
                    return 0
                }
                if (targetScorers.isEmpty()) {
                    log.addNoTargetMatch().output()
                    return 0
                }
                val targetScorer = targetScorers.iterator().next()

                val targetObjectiveName = list.getResult<String>(3)!!

                val targetScoreboard = manager.scoreboards[targetObjectiveName] ?: run {
                    log.addError("commands.scoreboard.objectiveNotFound", targetObjectiveName).output()
                    return 0
                }

                val targetScore = targetScoreboard.lines[targetScorer]!!.score
                val range = list.getResult<String>(5)
                if (range!!.contains("..")) {
                    //条件为一个区间
                    var min = Int.MIN_VALUE
                    var max = Int.MAX_VALUE
                    val splittedScoreScope = range.split(SCORE_SCOPE_SEPARATOR)
                    val min_str = splittedScoreScope[0]
                    if (!min_str.isEmpty()) {
                        min = min_str.toInt()
                    }
                    val max_str = splittedScoreScope[1]
                    if (!max_str.isEmpty()) {
                        max = max_str.toInt()
                    }
                    matched = targetScore >= min && targetScore <= max
                } else {
                    //条件为单个数字
                    val score = range.toInt()
                    matched = targetScore == score
                }

                val condition = (matched && shouldMatch) || (!matched && !shouldMatch)
                if (list.hasResult(6) && condition) {
                    val chainCommand = list.getResult<String>(6)!!
                    return Server.instance.executeCommand(sender, chainCommand)
                } else if (condition) {
                    log.addSuccess("commands.execute.trueCondition").output()
                    return 1
                } else {
                    log.addError("commands.execute.falseCondition", isIF, "score")
                    return 0
                }
            }

            else -> {
                return 0
            }
        }
    }

    companion object {
        protected const val SCORE_SCOPE_SEPARATOR: String = ".."

        private val ERROR_COMMAND_NAME: Pattern = Pattern.compile("(?<=run\\s).*?(?=\\s|$)")
    }
}
