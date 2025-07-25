package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes

interface EntityOwnable : EntityComponent {
    fun getOwnerName(): String? {
        return memoryStorage[CoreMemoryTypes.OWNER_NAME]
    }

    fun setOwnerName(playerName: String) {
        memoryStorage[CoreMemoryTypes.OWNER_NAME] = playerName
    }

    val owner: Player?
        get() {
            var owner = memoryStorage[CoreMemoryTypes.OWNER]
            if (owner != null && owner.isOnline) return owner
            else {
                val ownerName: String = getOwnerName() ?: return null
                owner = Server.instance.getPlayerExact(ownerName)
            }
            return owner
        }

    fun hasOwner(checkOnline: Boolean = true): Boolean {
        return if (checkOnline) {
            owner != null
        } else {
            getOwnerName() != null
        }
    }
}
