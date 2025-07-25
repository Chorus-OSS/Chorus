package org.chorus_oss.chorus.event.potion

import org.chorus_oss.chorus.entity.effect.PotionType
import org.chorus_oss.chorus.entity.projectile.throwable.EntitySplashPotion
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PotionCollideEvent(potion: PotionType, val thrownPotion: EntitySplashPotion) : PotionEvent(potion),
    Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
