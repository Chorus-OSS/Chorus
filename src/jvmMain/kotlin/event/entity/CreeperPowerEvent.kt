package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.mob.monster.EntityCreeper
import org.chorus_oss.chorus.entity.weather.EntityLightningStrike
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList


class CreeperPowerEvent(
    creeper: EntityCreeper,
    /**
     * Gets the cause of the creeper being (un)powered.
     *
     * @return A PowerCause value detailing the cause of change in power.
     */
    val cause: PowerCause?
) : EntityEvent(), Cancellable {

    /**
     * Gets the lightning bolt which is striking the Creeper.
     *
     * @return The Entity for the lightning bolt which is striking the Creeper
     */
    var lightning: EntityLightningStrike? = null
        private set

    constructor(creeper: EntityCreeper, bolt: EntityLightningStrike?, cause: PowerCause?) : this(creeper, cause) {
        this.lightning = bolt
    }

    override var entity: Entity = creeper

    /**
     * An enum to specify the cause of the change in power
     */
    enum class PowerCause {
        /**
         * Power change caused by a lightning bolt
         *
         *
         * Powered state: true
         */
        LIGHTNING,

        /**
         * Power change caused by something else (probably a plugin)
         *
         *
         * Powered state: true
         */
        SET_ON,

        /**
         * Power change caused by something else (probably a plugin)
         *
         *
         * Powered state: false
         */
        SET_OFF
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
