package org.chorus_oss.chorus.utils

import com.google.common.collect.Sets
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.BlockFace

/**
 * Interface, all redstone components implement, containing redstone related methods.
 */
interface RedstoneComponent {
    /**
     * Send a redstone update to all blocks around this block.
     *
     * @param ignoredFaces The faces, that shouldn't get updated.
     */
    fun updateAroundRedstone(vararg ignoredFaces: BlockFace) {
        this.updateAroundRedstone(Sets.newHashSet(*ignoredFaces))
    }

    /**
     * Send a redstone update to all blocks around this block.
     *
     * @param ignoredFaces The faces, that shouldn't get updated.
     */
    fun updateAroundRedstone(ignoredFaces: Set<BlockFace>) {
        if (this is Locator) updateAroundRedstone(this as Locator, ignoredFaces)
    }

    /**
     * Send a redstone update to all blocks around this block.
     *
     * @param ignoredFaces The faces, that shouldn't get updated.
     */
    fun updateAllAroundRedstone(vararg ignoredFaces: BlockFace) {
        this.updateAllAroundRedstone(Sets.newHashSet(*ignoredFaces))
    }

    /**
     * Send a redstone update to all blocks around this block and also around the blocks of those updated blocks.
     *
     * @param ignoredFaces The faces, that shouldn't get updated.
     */
    fun updateAllAroundRedstone(ignoredFaces: Set<BlockFace>) {
        if (this is Locator) updateAllAroundRedstone(this, ignoredFaces)
    }

    companion object {
        /**
         * Send a redstone update to all blocks around the given position.
         *
         * @param pos          The middle of the blocks around.
         * @param ignoredFaces The faces, that shouldn't get updated.
         */
        @JvmStatic
        fun updateAroundRedstone(pos: Locator, vararg ignoredFaces: BlockFace) {
            updateAroundRedstone(pos, Sets.newHashSet(*ignoredFaces))
        }

        /**
         * Send a redstone update to all blocks around the given position.
         *
         * @param pos          The middle of the blocks around.
         * @param ignoredFaces The faces, that shouldn't get updated.
         */
        fun updateAroundRedstone(pos: Locator, ignoredFaces: Set<BlockFace>) {
            for (face in BlockFace.entries) {
                if (ignoredFaces.contains(face)) continue
                pos.levelBlock.getSide(face).onUpdate(Level.BLOCK_UPDATE_REDSTONE)
            }
        }

        /**
         * Send a redstone update to all blocks around the given position and also around the blocks of those updated blocks.
         *
         * @param pos          The middle of the blocks around.
         * @param ignoredFaces The faces, that shouldn't get updated.
         */
        fun updateAllAroundRedstone(pos: Locator, vararg ignoredFaces: BlockFace) {
            updateAllAroundRedstone(pos, Sets.newHashSet<BlockFace>(*ignoredFaces))
        }

        /**
         * Send a redstone update to all blocks around the given position and also around the blocks of those updated blocks.
         *
         * @param pos          The middle of the blocks around.
         * @param ignoredFaces The faces, that shouldn't get updated.
         */
        fun updateAllAroundRedstone(pos: Locator, ignoredFaces: Set<BlockFace>) {
            updateAroundRedstone(pos, ignoredFaces)

            for (face in BlockFace.entries) {
                if (ignoredFaces.contains(face)) continue

                updateAroundRedstone(pos.getSide(face), face.getOpposite())
            }
        }
    }
}
