package org.chorus_oss.chorus.event.item

import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.item.Item


abstract class ItemEvent(val item: Item) : Event()
