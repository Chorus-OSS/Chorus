package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.Vector3

class ItemGoatHorn(aux: Int, count: Int) : Item(ItemID.Companion.GOAT_HORN) {
    protected var coolDownTick: Int = 140

    @JvmOverloads
    constructor(count: Int = 1) : this(0, 1)

    init {
        this.meta = aux
        this.count = count
    }

    override val maxStackSize: Int
        get() = 1

    override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
        if (player.isItemCoolDownEnd(this.identifier)) {
            playSound(player)
            player.setItemCoolDown(coolDownTick, this.identifier)
            return true
        } else return false
    }

    /**
     * Sets cool down tick
     *
     * @param coolDownTick the cool down tick
     */
    fun setCoolDown(coolDownTick: Int) {
        this.coolDownTick = coolDownTick
    }

    fun playSound(player: Player) {
        when (this.meta) {
            0 -> player.level!!.addSound(player.position, Sound.HORN_CALL_0)
            1 -> player.level!!.addSound(player.position, Sound.HORN_CALL_1)
            2 -> player.level!!.addSound(player.position, Sound.HORN_CALL_2)
            3 -> player.level!!.addSound(player.position, Sound.HORN_CALL_3)
            4 -> player.level!!.addSound(player.position, Sound.HORN_CALL_4)
            5 -> player.level!!.addSound(player.position, Sound.HORN_CALL_5)
            6 -> player.level!!.addSound(player.position, Sound.HORN_CALL_6)
            7 -> player.level!!.addSound(player.position, Sound.HORN_CALL_7)
        }
    }
}
