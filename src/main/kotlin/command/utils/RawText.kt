package org.chorus_oss.chorus.command.utils


import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.selector.EntitySelectorAPI
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.scoreboard.scorer.EntityScorer
import org.chorus_oss.chorus.scoreboard.scorer.FakeScorer
import org.chorus_oss.chorus.scoreboard.scorer.IScorer
import org.chorus_oss.chorus.scoreboard.scorer.PlayerScorer
import java.util.stream.Collectors


class RawText private constructor(private var base: Component) {
    fun preParse(sender: CommandSender) {
        preParse(sender, base)
    }

    fun toRawText(): String {
        return Json.encodeToString(base)
    }


    @Serializable
    class Component {
        var text: String? = null
        var selector: String? = null
        var translate: String? = null
        var with: Component? = null
        var score: ScoreComponent? = null
        var rawtext: MutableList<Component>? = null

        @Serializable
        class ScoreComponent {
            val name: String? = null
            val objective: String? = null
            val value: Int? = null
        }

        enum class ComponentType {
            TEXT,
            SELECTOR,
            TRANSLATE,
            TRANSLATE_WITH,
            SCORE,
            RAWTEXT
        }

        val type: ComponentType?
            get() {
                if (text != null) {
                    return ComponentType.TEXT
                }
                if (selector != null) {
                    return ComponentType.SELECTOR
                }
                if (translate != null) {
                    if (with != null) {
                        return ComponentType.TRANSLATE_WITH
                    }
                    return ComponentType.TRANSLATE
                }
                if (score != null) {
                    if (score!!.name != null && score!!.objective != null) {
                        return ComponentType.SCORE
                    }
                }
                if (rawtext != null) {
                    return ComponentType.RAWTEXT
                }
                return null
            }
    }

    override fun toString(): String {
        return Json.encodeToString(this.base)
    }

    companion object {
        fun fromRawText(rawText: String): RawText {
            return RawText(Json.decodeFromString<Component>(rawText))
        }

        private fun preParse(sender: CommandSender, cps: Component) {
            if (cps.type != Component.ComponentType.RAWTEXT) return
            val components = cps.rawtext
            for (component in components!!.toTypedArray<Component>()) {
                if (component.type == Component.ComponentType.SCORE) {
                    val newComponent = preParseScore(component, sender)
                    if (newComponent != null) components[components.indexOf(component)] = newComponent
                    else components.remove(component)
                }
                if (component.type == Component.ComponentType.SELECTOR) {
                    val newComponent = preParseSelector(component, sender)
                    if (newComponent != null) components[components.indexOf(component)] = newComponent
                    else components.remove(component)
                }
                if (component.type == Component.ComponentType.RAWTEXT) {
                    preParse(sender, component)
                }
                if (component.type == Component.ComponentType.TRANSLATE_WITH) {
                    preParse(sender, component.with!!)
                }
            }
        }

        private fun preParseScore(
            component: Component,
            sender: CommandSender
        ): Component? {
            val scoreboard = Server.instance.scoreboardManager.getScoreboard(component.score!!.objective)
                ?: return null
            val name_str = component.score!!.name!!
            var scorer: IScorer? = null
            var value = component.score!!.value

            if (name_str == "*") {
                if (!sender.isEntity) return null
                scorer = if (sender.isPlayer) PlayerScorer(sender.asPlayer()!!) else EntityScorer(sender.asEntity()!!)
            } else if (EntitySelectorAPI.api.checkValid(name_str)) {
                val scorers: List<IScorer> =
                    EntitySelectorAPI.api.matchEntities(sender, name_str)
                        .map { t -> if (t is Player) PlayerScorer(t) else EntityScorer(t) }.toList()
                if (scorers.isEmpty()) return null
                scorer = scorers[0]
            } else if (Server.instance.getPlayer(name_str) != null) {
                scorer = PlayerScorer(Server.instance.getPlayer(name_str)!!)
            } else {
                scorer = FakeScorer(name_str)
            }

            if (value == null) value = scoreboard.getLine(scorer)!!.score
            val newComponent = Component()
            newComponent.text = (value.toString())
            return newComponent
        }

        private fun preParseSelector(
            component: Component,
            sender: CommandSender
        ): Component? {
            val entities: List<Entity>
            try {
                entities = EntitySelectorAPI.Companion.api.matchEntities(sender, component.selector!!)
            } catch (e: Exception) {
                return null
            }
            if (entities.isEmpty()) return null
            val entities_str =
                entities.stream().map { obj: Entity -> obj.getEntityName() }.collect(Collectors.joining(", "))
            val newComponent = Component()
            newComponent.text = (entities_str)
            return newComponent
        }
    }
}