package org.chorus_oss.chorus.scoreboard.manager

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.event.scoreboard.ScoreboardObjectiveChangeEvent
import org.chorus_oss.chorus.scoreboard.IScoreboard
import org.chorus_oss.chorus.scoreboard.data.DisplaySlot
import org.chorus_oss.chorus.scoreboard.displayer.IScoreboardViewer
import org.chorus_oss.chorus.scoreboard.scorer.EntityScorer
import org.chorus_oss.chorus.scoreboard.scorer.PlayerScorer
import org.chorus_oss.chorus.scoreboard.storage.IScoreboardStorage
import java.util.*
import java.util.function.Consumer

class ScoreboardManager(override var storage: IScoreboardStorage) : IScoreboardManager {
    override var scoreboards: MutableMap<String, IScoreboard> = HashMap()
    override var display: MutableMap<DisplaySlot, IScoreboard?> = EnumMap(DisplaySlot::class.java)
    override var viewers: MutableSet<IScoreboardViewer> = HashSet()

    init {
        read()
    }

    override fun addScoreboard(scoreboard: IScoreboard): Boolean {
        val event = ScoreboardObjectiveChangeEvent(scoreboard, ScoreboardObjectiveChangeEvent.ActionType.ADD)
        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) {
            return false
        }
        scoreboards[scoreboard.objectiveName] = scoreboard
        CommandEnum.SCOREBOARD_OBJECTIVES.updateSoftEnum(
            org.chorus_oss.protocol.packets.UpdateSoftEnumPacket.Companion.ActionType.Add,
            scoreboard.objectiveName
        )
        return true
    }

    override fun removeScoreboard(scoreboard: IScoreboard): Boolean {
        return removeScoreboard(scoreboard.objectiveName)
    }

    override fun removeScoreboard(objectiveName: String): Boolean {
        val removed = scoreboards[objectiveName] ?: return false
        val event = ScoreboardObjectiveChangeEvent(removed, ScoreboardObjectiveChangeEvent.ActionType.REMOVE)
        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) {
            return false
        }
        scoreboards.remove(objectiveName)
        CommandEnum.SCOREBOARD_OBJECTIVES.updateSoftEnum(
            org.chorus_oss.protocol.packets.UpdateSoftEnumPacket.Companion.ActionType.Remove,
            objectiveName
        )
        viewers.forEach(Consumer { viewer: IScoreboardViewer -> viewer.removeScoreboard(removed) })
        display.forEach { (slot, scoreboard) ->
            if (scoreboard != null && scoreboard.objectiveName == objectiveName) {
                display[slot] = null
            }
        }
        return true
    }

    override fun getScoreboard(objectiveName: String?): IScoreboard? {
        return scoreboards[objectiveName]
    }

    override fun containScoreboard(scoreboard: IScoreboard): Boolean {
        return scoreboards.containsKey(scoreboard.objectiveName)
    }

    override fun containScoreboard(name: String?): Boolean {
        return scoreboards.containsKey(name)
    }

    override fun getDisplaySlot(slot: DisplaySlot): IScoreboard? {
        return display[slot]
    }

    override fun setDisplay(slot: DisplaySlot, scoreboard: IScoreboard?) {
        val old = display.put(slot, scoreboard)
        if (old != null) viewers.forEach(Consumer { viewer: IScoreboardViewer -> old.removeViewer(viewer, slot) })
        if (scoreboard != null) viewers.forEach(Consumer { viewer: IScoreboardViewer ->
            scoreboard.addViewer(
                viewer,
                slot
            )
        })
    }

    override fun addViewer(viewer: IScoreboardViewer): Boolean {
        val added = viewers.add(viewer)
        if (added) display.forEach { (slot, scoreboard) ->
            scoreboard?.addViewer(viewer, slot)
        }
        return added
    }

    override fun removeViewer(viewer: IScoreboardViewer): Boolean {
        val removed = viewers.remove(viewer)
        if (removed) display.forEach { (slot, scoreboard) ->
            scoreboard?.removeViewer(viewer, slot)
        }
        return removed
    }

    /**
     * 你可能会对这里的代码感到疑惑
     * 这里其实是为了规避玩家下线导致的“玩家离线”计分项
     * 我们在玩家下线时，删除其他在线玩家显示槽位中的“玩家离线”
     * 并在其重新连接上服务器时加回去
     */
    override fun onPlayerJoin(player: Player) {
        addViewer(player)
        val scorer = PlayerScorer(player)
        scoreboards.values.forEach(Consumer { scoreboard ->
            if (scoreboard!!.containLine(scorer)) {
                viewers.forEach(Consumer { viewer ->
                    viewer.updateScore(
                        scoreboard.getLine(scorer)!!
                    )
                })
            }
        })
    }

    override fun beforePlayerQuit(player: Player) {
        val scorer = PlayerScorer(player)
        scoreboards.values.forEach(Consumer { scoreboard: IScoreboard? ->
            if (scoreboard!!.containLine(scorer)) {
                viewers.forEach(Consumer { viewer: IScoreboardViewer ->
                    viewer.removeLine(
                        scoreboard.getLine(scorer)!!
                    )
                })
            }
        })
        removeViewer(player)
    }

    override fun onEntityDead(entity: EntityLiving) {
        val scorer = EntityScorer(entity)
        scoreboards.forEach { (_, scoreboard) ->
            if (scoreboard.lines.isEmpty()) return@forEach
            scoreboard.removeLine(scorer)
        }
    }

    override fun save() {
        storage.removeAllScoreboard()
        storage.saveScoreboard(scoreboards.values)
        storage.saveDisplay(display)
    }

    override fun read() {
        //新建一个列表避免迭代冲突
        ArrayList(scoreboards.values).forEach(Consumer { scoreboard: IScoreboard? ->
            this.removeScoreboard(
                scoreboard!!
            )
        })
        display.forEach { (slot, _) -> setDisplay(slot, null) }

        scoreboards = storage.readScoreboard()
        storage.readDisplay().forEach { (slot, objectiveName) ->
            val scoreboard = getScoreboard(objectiveName)
            if (scoreboard != null) {
                this.setDisplay(slot, scoreboard)
            }
        }
    }
}
