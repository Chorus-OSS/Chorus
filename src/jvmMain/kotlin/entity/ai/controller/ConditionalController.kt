package org.chorus_oss.chorus.entity.ai.controller

import org.chorus_oss.chorus.entity.mob.EntityMob
import java.util.function.Predicate

class ConditionalController(vararg controllers: Pair<Predicate<EntityMob>, IController>) :
    IController {
    private val controllers: MutableMap<Predicate<EntityMob>, IController> = mutableMapOf()

    init {
        controllers.forEach { pair ->
            this.controllers[pair.first] = pair.second
        }
    }

    override fun control(entity: EntityMob): Boolean {
        var successful = false
        for ((key, value) in controllers.entries) {
            if (key.test(entity)) {
                if (value.control(entity)) {
                    successful = true
                }
            }
        }
        return successful
    }
}
