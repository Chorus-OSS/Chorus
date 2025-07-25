package org.chorus_oss.chorus.event.potion

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.PotionType
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PotionApplyEvent(potion: PotionType, var applyEffects: List<Effect>, val entity: Entity) :
    PotionEvent(potion), Cancellable {
    fun setApplyEffect(applyEffects: List<Effect>) {
        this.applyEffects = applyEffects
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
