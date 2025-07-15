package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.EntityEventPacket
import java.util.concurrent.ThreadLocalRandom


abstract class EntityAnimal(chunk: IChunk?, nbt: CompoundTag) : EntityMob(chunk, nbt) {

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        val superResult = super.onInteract(player, item, clickedPos)
        if (isBreedingItem(item)) {
            return useBreedingItem(player, item) && superResult
        }
        return superResult
    }

    protected open fun useBreedingItem(player: Player, item: Item): Boolean {
        memoryStorage[CoreMemoryTypes.LAST_FEED_PLAYER] = player
        memoryStorage[CoreMemoryTypes.LAST_BE_FEED_TIME] = level!!.tick
        sendBreedingAnimation(item)
        item.count--
        return player.inventory.setItemInHand(item)
    }

    protected fun sendBreedingAnimation(item: Item) {
        val pk = EntityEventPacket()
        pk.event = EntityEventPacket.EATING_ITEM
        pk.eid = this.getRuntimeID()
        pk.data = item.fullId
        Server.broadcastPacket(this.viewers.values, pk)
    }

    /**
     * 可以导致繁殖的喂养物品
     *
     *
     * Feeding items that can lead to reproduction.
     *
     * @param item 物品
     * @return boolean 是否可以导致繁殖<br></br>Whether it can lead to reproduction
     */
    open fun isBreedingItem(item: Item): Boolean {
        return item.id == BlockID.WHEAT //default
    }

    override fun getStepHeight(): Double {
        return 0.5
    }

    override fun getExperienceDrops(): Int {
        return ThreadLocalRandom.current().nextInt(3) + 1
    }
}
