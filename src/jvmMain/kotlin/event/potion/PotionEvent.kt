package org.chorus_oss.chorus.event.potion

import org.chorus_oss.chorus.entity.effect.PotionType
import org.chorus_oss.chorus.event.Event

abstract class PotionEvent(var potion: PotionType) : Event()
