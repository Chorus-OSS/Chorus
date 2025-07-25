package org.chorus_oss.chorus.entity


interface EntityRideable {
    /**
     * Mount or Dismounts an Entity from a rideable entity
     *
     * @param entity The target Entity
     * @return `true` if the mounting successful
     */
    fun mountEntity(entity: Entity): Boolean

    fun dismountEntity(entity: Entity): Boolean
}
