package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.inventory.BeaconInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockEntityBeacon(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt),
    BlockEntityInventoryHolder {
    override var inventory: Inventory = BeaconInventory(this)

    override fun initBlockEntity() {
        super.initBlockEntity()
        scheduleUpdate()
    }

    override fun loadNBT() {
        super.loadNBT()
        if (!namedTag.contains("Lock")) {
            namedTag.putString("Lock", "")
        }

        if (!namedTag.contains("Levels")) {
            namedTag.putInt("Levels", 0)
        }

        if (!namedTag.contains("Primary")) {
            namedTag.putInt("Primary", 0)
        }

        if (!namedTag.contains("Secondary")) {
            namedTag.putInt("Secondary", 0)
        }
    }

    override val isBlockEntityValid: Boolean
        get() {
            val blockId = block.id
            return blockId === BlockID.BEACON
        }

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound
            .putString("Lock", namedTag.getString("Lock"))
            .putInt("Levels", namedTag.getInt("Levels"))
            .putInt("primary", namedTag.getInt("Primary"))
            .putInt("secondary", namedTag.getInt("Secondary"))

    private var currentTick: Long = 0

    override fun onUpdate(): Boolean {
        //Only apply effects every 4 secs
        if (currentTick++ % 80 != 0L) {
            return true
        }

        val oldPowerLevel = this.powerLevel
        //Get the power level based on the pyramid
        powerLevel = calculatePowerLevel()
        val newPowerLevel = this.powerLevel

        //Skip beacons that do not have a pyramid or sky access
        if (newPowerLevel < 1 || !hasSkyAccess()) {
            if (oldPowerLevel > 0) {
                level.addSound(this.position, Sound.BEACON_DEACTIVATE)
            }
            return true
        } else if (oldPowerLevel < 1) {
            level.addSound(this.position, Sound.BEACON_ACTIVATE)
        } else {
            level.addSound(this.position, Sound.BEACON_AMBIENT)
        }

        //Get all players in game
        val players = level.players

        //Calculate vars for beacon power
        val range = 10 + powerLevel * 10
        val duration = 9 + powerLevel * 2

        if (!isPrimaryAllowed(primaryPower, powerLevel)) {
            return true
        }

        for ((_, p) in players) {
            //If the player is in range
            if (p.position.distance(this.position) < range) {
                var e: Effect

                if (primaryPower != 0) {
                    //Apply the primary power
                    e = Effect.get(primaryPower)

                    //Set duration
                    e.setDuration(duration * 20)

                    //If secondary is selected as the primary too, apply 2 amplification
                    if (powerLevel == POWER_LEVEL_MAX && secondaryPower == primaryPower) {
                        e.setAmplifier(1)
                    }

                    //Add the effect
                    p.addEffect(e)
                }

                //If we have a secondary power as regen, apply it
                if (powerLevel == POWER_LEVEL_MAX && secondaryPower == EffectType.REGENERATION.id) {
                    //Get the regen effect
                    e = Effect.get(EffectType.REGENERATION)

                    //Set duration
                    e.setDuration(duration * 20)

                    //Add effect
                    p.addEffect(e)
                }
            }
        }

        return true
    }

    private fun hasSkyAccess(): Boolean {
        val tileX = floorX
        val tileY = floorY
        val tileZ = floorZ

        //Check every block from our y coord to the top of the world
        for (y in tileY + 1..255) {
            val test = level.getBlock(tileX, y, tileZ)
            if (!test.isTransparent) {
                //There is no sky access
                return false
            }
        }

        return true
    }

    private fun calculatePowerLevel(): Int {
        val tileX = floorX
        val tileY = floorY
        val tileZ = floorZ

        //The power level that we're testing for
        for (powerLevel in 1..POWER_LEVEL_MAX) {
            val queryY = tileY - powerLevel //Layer below the beacon block

            for (queryX in tileX - powerLevel..tileX + powerLevel) {
                for (queryZ in tileZ - powerLevel..tileZ + powerLevel) {
                    val testBlockID = level.getBlockIdAt(queryX, queryY, queryZ)
                    if (testBlockID !== BlockID.IRON_BLOCK && testBlockID !== BlockID.GOLD_BLOCK && testBlockID !== BlockID.EMERALD_BLOCK && testBlockID !== BlockID.DIAMOND_BLOCK && testBlockID !== BlockID.NETHERITE_BLOCK
                    ) {
                        return powerLevel - 1
                    }
                }
            }
        }

        return POWER_LEVEL_MAX
    }

    var powerLevel: Int = 0
        get() = namedTag.getInt("Levels")
        set(level) {
            val currentLevel = field
            if (level != currentLevel) {
                namedTag.putInt("Levels", level)
                setDirty()
                this.spawnToAll()
            }
        }

    var primaryPower: Int = 0
        get() = namedTag.getInt("Primary")
        set(power) {
            val currentPower = field
            if (power != currentPower) {
                namedTag.putInt("Primary", power)
                setDirty()
                this.spawnToAll()
            }
        }

    var secondaryPower: Int = 0
        get() = namedTag.getInt("Secondary")
        set(power) {
            val currentPower = field
            if (power != currentPower) {
                namedTag.putInt("Secondary", power)
                setDirty()
                this.spawnToAll()
            }
        }

    override fun updateCompoundTag(nbt: CompoundTag, player: Player): Boolean {
        if (nbt.getString("id") != BlockEntityID.BEACON) {
            return false
        }

        val primary = nbt.getInt("primary")
        if (!isPrimaryAllowed(primary, this.powerLevel)) {
            return false
        }

        val secondary = nbt.getInt("secondary")
        if (secondary != 0 && secondary != primary && secondary != EffectType.REGENERATION.id) {
            return false
        }

        this.primaryPower = primary
        this.secondaryPower = secondary

        level.addSound(this.position, Sound.BEACON_POWER)

        inventory.setItem(0, Item.AIR)
        return true
    }

    override var name: String
        get() = if (this.hasName()) namedTag.getString("CustomName") else "Beacon"
        set(name) {
            if (name.isNullOrEmpty()) {
                namedTag.remove("CustomName")
                return
            }

            namedTag.putString("CustomName", name)
        }

    override fun hasName(): Boolean {
        return namedTag.contains("CustomName")
    }

    companion object {
        private const val POWER_LEVEL_MAX = 4

        private fun isPrimaryAllowed(primary: Int, powerLevel: Int): Boolean {
            return ((primary == EffectType.SPEED.id || primary == EffectType.HASTE.id) && powerLevel >= 1) ||
                    ((primary == EffectType.RESISTANCE.id || primary == EffectType.JUMP_BOOST.id) && powerLevel >= 2) ||
                    (primary == EffectType.STRENGTH.id && powerLevel >= 3)
        }
    }
}
