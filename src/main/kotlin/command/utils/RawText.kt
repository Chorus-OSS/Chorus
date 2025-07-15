package org.chorus_oss.chorus.command.utils


import com.google.gson.annotations.SerializedName
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.selector.EntitySelectorAPI
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.scoreboard.scorer.EntityScorer
import org.chorus_oss.chorus.scoreboard.scorer.FakeScorer
import org.chorus_oss.chorus.scoreboard.scorer.IScorer
import org.chorus_oss.chorus.scoreboard.scorer.PlayerScorer
import org.chorus_oss.chorus.utils.JSONUtils
import java.util.stream.Collectors


class RawText private constructor(base: Component) {
    private var base: Component? = null

    init {
        this.base = base
    }

    fun preParse(sender: CommandSender) {
        preParse(sender, base!!)
    }

    fun toRawText(): String {
        return JSONUtils.to(base)
    }


    class Component {
        @SerializedName("text")
        var component_text: String? = null

        @SerializedName("selector")
        var component_selector: String? = null

        @SerializedName("translate")
        var component_translate: String? = null

        @SerializedName("with")
        var component_translate_with: Any? = null

        @SerializedName("score")
        var component_score: ScoreComponent? = null

        @SerializedName("rawtext")
        var component_rawtext: MutableList<Component>? = null

        class ScoreComponent {
            @SerializedName("name")
            val name: String? = null

            @SerializedName("objective")
            val objective: String? = null

            @SerializedName("value")
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
                if (component_text != null) {
                    return ComponentType.TEXT
                }
                if (component_selector != null) {
                    return ComponentType.SELECTOR
                }
                if (component_translate != null) {
                    if (component_translate_with != null) {
                        return ComponentType.TRANSLATE_WITH
                    }
                    return ComponentType.TRANSLATE
                }
                if (component_score != null) {
                    if (component_score!!.name != null && component_score!!.objective != null) {
                        return ComponentType.SCORE
                    }
                }
                if (component_rawtext != null) {
                    return ComponentType.RAWTEXT
                }
                return null
            }
    }

    override fun toString(): String {
        return JSONUtils.to(this.base)
    }

    companion object {
        fun fromRawText(rawText: String?): RawText {
            val base = JSONUtils.from(
                rawText,
                Component::class.java
            )
            return RawText(base)
        }

        private fun preParse(sender: CommandSender, cps: Component) {
            if (cps.type != Component.ComponentType.RAWTEXT) return
            val components = cps.component_rawtext
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
                    if (component.component_translate_with is Map<*, *>) {
                        val cp = JSONUtils.from(
                            JSONUtils.to(component.component_translate_with),
                            Component::class.java
                        )
                        preParse(sender, cp)
                        component.component_translate_with = cp
                    }
                }
            }
        }

        private fun preParseScore(
            component: Component,
            sender: CommandSender
        ): Component? {
            val scoreboard = Server.instance.scoreboardManager.getScoreboard(component.component_score!!.objective)
                ?: return null
            val name_str = component.component_score!!.name!!
            var scorer: IScorer? = null
            var value = component.component_score!!.value

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
            newComponent.component_text = (value.toString())
            return newComponent
        }

        private fun preParseSelector(
            component: Component,
            sender: CommandSender
        ): Component? {
            val entities: List<Entity>
            try {
                entities = EntitySelectorAPI.Companion.api.matchEntities(sender, component.component_selector!!)
            } catch (e: Exception) {
                return null
            }
            if (entities.isEmpty()) return null
            val entities_str =
                entities.stream().map { obj: Entity -> obj.getEntityName() }.collect(Collectors.joining(", "))
            val newComponent = Component()
            newComponent.component_text = (entities_str)
            return newComponent
        }
    }
}