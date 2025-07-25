package org.chorus_oss.chorus.level.vibration

import java.util.*


enum class VibrationType(frequency: Int) {
    STEP(1),
    FLAP(2),
    SWIM(3),
    ELYTRA_GLIDE(4),
    HIT_GROUND(5),
    TELEPORT(5),
    BLOCK_CHANGE(6),
    ENTITY_SHAKING(6),
    NOTE_BLOCK_PLAY(6),
    SPLASH(6),
    DRINKING(7),
    PRIME_FUSE(7),
    PROJECTILE_SHOOT(7),
    EAT(8),
    ENTITY_DAMAGE(8),
    ENTITY_INTERACT(8),
    PROJECTILE_LAND(8),
    ENTITY_ROAR(9),
    EQUIP(9),
    SHEAR(9),
    BLOCK_CLOSE(10),
    BLOCK_DEACTIVATE(10),
    BLOCK_DETACH(10),
    DISPENSE_FAIL(10),
    BLOCK_ACTIVATE(11),
    BLOCK_ATTACH(11),
    BLOCK_OPEN(11),
    BLOCK_PLACE(12),
    ENTITY_PLACE(12),
    FLUID_PLACE(12),
    BLOCK_DESTROY(13),
    ENTITY_DIE(13),
    FLUID_PICKUP(13),
    CONTAINER_CLOSE(14),
    ITEM_INTERACT_FINISH(14),
    PISTON_CONTRACT(14),
    CONTAINER_OPEN(15),
    EXPLODE(15),
    INSTRUMENT_PLAY(15),
    LIGHTNING_STRIKE(15),
    PISTON_EXTEND(15);

    val identifier: String = "minecraft:" + this.name.lowercase(Locale.ENGLISH)
    val frequency: Int

    init {
        require(!(frequency < 1 || frequency > 15)) { "frequency must between 1 and 15" }
        this.frequency = frequency
    }
}
