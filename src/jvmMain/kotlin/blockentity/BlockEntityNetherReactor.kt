package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.property.enums.NetherReactorState
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

/**
 * This entity allows to manipulate the save state of a nether reactor core, but changing it
 * will cause no visual change. To see the changes in the world it would be necessary to
 * change the block data value to `0 1 or 3` but that is impossible in the recent versions
 * because Minecraft Bedrock Edition has moved from block data to the block property `&` block state
 * system and did not create a block property for the old nether reactor core block, making it
 * impossible for the server to tell the client to render the red and dark versions of the block.
 */
class BlockEntityNetherReactor(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    var reactorState: NetherReactorState? = null
    private var progress = 0

    override val isBlockEntityValid: Boolean
        get() = levelBlock.id === BlockID.NETHERREACTOR

    fun getProgress(): Int {
        return progress
    }

    fun setProgress(progress: Int) {
        this.progress = progress.coerceIn(0, 900)
    }

    override fun loadNBT() {
        super.loadNBT()
        reactorState = NetherReactorState.READY
        if (namedTag.containsShort("Progress")) {
            progress = namedTag.getShort("Progress").toInt()
        }

        reactorState = if (namedTag.containsByte("HasFinished") && namedTag.getBoolean("HasFinished")) {
            NetherReactorState.FINISHED
        } else if (namedTag.containsByte("IsInitialized") && namedTag.getBoolean("IsInitialized")) {
            NetherReactorState.INITIALIZED
        } else {
            NetherReactorState.READY
        }
    }

    override fun saveNBT() {
        super.saveNBT()
        val reactorState = reactorState
        namedTag.putShort("Progress", getProgress())
        namedTag.putBoolean("HasFinished", reactorState == NetherReactorState.FINISHED)
        namedTag.putBoolean("IsInitialized", reactorState == NetherReactorState.INITIALIZED)
    }

    override val spawnCompound: CompoundTag
        get() {
            val reactorState = reactorState
            return super.spawnCompound
                .putShort("Progress", getProgress())
                .putBoolean("HasFinished", reactorState == NetherReactorState.FINISHED)
                .putBoolean("IsInitialized", reactorState == NetherReactorState.INITIALIZED)
        }
}
