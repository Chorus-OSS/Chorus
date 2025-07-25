package org.chorus_oss.chorus.entity.data.profession

import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag

abstract class Profession(
    private val index: Int,
    private val blockId: String,
    private val name: String,
    private val workSound: Sound
) {
    open fun buildTrades(seed: Int): ListTag<CompoundTag> {
        return ListTag()
    }


    fun getBlockID(): String {
        return this.blockId
    }

    fun getIndex(): Int {
        return this.index
    }

    fun getName(): String {
        return this.name
    }

    fun getWorkSound(): Sound {
        return this.workSound
    }

    companion object {
        private val knownProfessions: HashMap<Int, Profession> = HashMap()

        fun registerProfession(profession: Profession) {
            knownProfessions[profession.getIndex()] = profession
        }

        fun getProfessions(): HashMap<Int, Profession> {
            return knownProfessions.clone() as HashMap<Int, Profession>
        }

        fun getProfession(index: Int): Profession? {
            return knownProfessions[index]
        }

        @JvmStatic
        fun init() {
            registerProfession(ProfessionFarmer())
            registerProfession(ProfessionFisherman())
            registerProfession(ProfessionShepherd())
            registerProfession(ProfessionFletcher())
            registerProfession(ProfessionLibrarian())
            registerProfession(ProfessionCartographer())
            registerProfession(ProfessionCleric())
            registerProfession(ProfessionArmor())
            registerProfession(ProfessionWeapon())
            registerProfession(ProfessionTool())
            registerProfession(ProfessionButcher())
            registerProfession(ProfessionLeather())
            registerProfession(ProfessionMason())
        }
    }
}
