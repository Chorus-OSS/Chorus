package org.chorus_oss.chorus.entity

/**
 * 代表可以攻击其他实体的实体.
 *
 *
 * Represents an entity that can attack other entities.
 */
interface EntityCanAttack {
    /**
     * 得到所有难度下不携带物品能造成的伤害.
     *
     *
     * Get the damage you can do without carrying items on all difficulties.
     *
     * @return 一个包含所有难度下伤害的数组, 0 1 2分别代表简单、普通、困难难度<br></br>An array containing damage on all difficulties, 0 1 2 for easy, normal and hard difficulties respectively
     */
    val diffHandDamage: FloatArray
        get() {
            return EMPTY_FLOAT_ARRAY
        }

    /**
     * 得到指定难度下不携带物品能造成的伤害.
     *
     *
     * Get the damage that can be dealt without carrying the item at the specified difficulty.
     *
     * @param difficulty 难度id<br></br>difficulty id
     * @return 伤害<br></br>damage
     */
    fun getDiffHandDamage(difficulty: Int): Float {
        return if (difficulty != 0) diffHandDamage[difficulty - 1] else 0f
    }

    fun attackTarget(entity: Entity): Boolean

    companion object {
        val EMPTY_FLOAT_ARRAY: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)
    }
}
