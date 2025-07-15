package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockFlower.Companion.isSupportValid
import org.chorus_oss.chorus.block.BlockFlowerPot.FlowerPotBlock
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.event.level.StructureGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.generator.`object`.*
import org.chorus_oss.chorus.level.generator.`object`.legacytree.LegacyBigSpruceTree
import org.chorus_oss.chorus.level.generator.`object`.legacytree.LegacyTreeGenerator
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector2
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom
import java.util.concurrent.ThreadLocalRandom

abstract class BlockSapling(blockstate: BlockState) : BlockFlowable(blockstate), FlowerPotBlock {
    abstract fun getWoodType(): WoodType

    var isAged: Boolean
        get() = getPropertyValue(CommonBlockProperties.AGE_BIT)
        set(aged) {
            setPropertyValue(CommonBlockProperties.AGE_BIT, aged)
        }

    override val name: String
        get() = getWoodType().woodName + " Sapling"

    override fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        if (isSupportValid(block.down())) {
            level.setBlock(block.position, this, true, true)
            return true
        }

        return false
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (item.isFertilizer) { // BoneMeal
            if (player != null && !player.isCreative) {
                item.count--
            }

            level.addParticle(BoneMealParticle(this.position))
            if (ThreadLocalRandom.current().nextFloat() >= 0.45) {
                return true
            }

            this.grow()

            return true
        }
        return false
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!isSupportValid(down())) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) { //Growth
            if (ThreadLocalRandom.current().nextInt(1, 8) == 1 && level.getFullLight(
                    position.add(0.0, 1.0, 0.0)
                ) >= BlockCrops.MIN_LIGHT_LEVEL
            ) {
                if (isAged) {
                    this.grow()
                } else {
                    isAged = true
                    level.setBlock(this.position, this, true)
                    return Level.BLOCK_UPDATE_RANDOM
                }
            } else {
                return Level.BLOCK_UPDATE_RANDOM
            }
        }
        return Level.BLOCK_UPDATE_NORMAL
    }

    private fun grow() {
        var generator: ObjectGenerator? = null
        var bigTree = false

        var vector3 = Vector3(
            position.x,
            position.y - 1, position.z
        )

        when (getWoodType()) {
            WoodType.JUNGLE -> {
                val vector2: Vector2?
                if ((findSaplings(WoodType.JUNGLE).also { vector2 = it }) != null) {
                    vector3 = position.add(vector2!!.floorX.toDouble(), 0.0, vector2.floorY.toDouble())
                    generator = ObjectJungleBigTree(
                        10, 20,
                        BlockJungleLog.properties.getBlockState(CommonBlockProperties.PILLAR_AXIS, BlockFace.Axis.Y),
                        BlockJungleLeaves.properties.defaultState
                    )
                    bigTree = true
                }

                if (!bigTree) {
                    generator = NewJungleTree(4, 7)
                    vector3 = position.add(0.0, 0.0, 0.0)
                }
            }

            WoodType.ACACIA -> {
                generator = ObjectSavannaTree()
                vector3 = position.add(0.0, 0.0, 0.0)
            }

            WoodType.DARK_OAK -> {
                val vector2: Vector2?
                if ((findSaplings(WoodType.DARK_OAK).also { vector2 = it }) != null) {
                    vector3 = position.add(vector2!!.floorX.toDouble(), 0.0, vector2.floorY.toDouble())
                    generator = ObjectDarkOakTree()
                    bigTree = true
                }

                if (!bigTree) {
                    return
                }
            }

            WoodType.PALE_OAK -> {
                val vector2: Vector2?
                if ((findSaplings(WoodType.PALE_OAK).also { vector2 = it }) != null) {
                    vector3 = position.add(vector2!!.floorX.toDouble(), 0.0, vector2.floorY.toDouble())
                    generator = ObjectPaleOakTree()
                    bigTree = true
                }

                if (!bigTree) {
                    generator = ObjectSmallPaleOakTree(4, 7)
                    vector3 = position.add(0.0, 0.0, 0.0)
                }
            }

            WoodType.SPRUCE -> {
                val vector2: Vector2?
                if ((findSaplings(WoodType.SPRUCE).also { vector2 = it }) != null) {
                    vector3 = position.add(vector2!!.floorX.toDouble(), 0.0, vector2.floorY.toDouble())
                    generator = object : HugeTreesGenerator(
                        0, 0,
                        BlockSpruceLog.properties.getBlockState(CommonBlockProperties.PILLAR_AXIS, BlockFace.Axis.Y),
                        BlockJungleLeaves.properties.defaultState
                    ) {
                        override fun generate(
                            level: BlockManager,
                            rand: ChorusRandom,
                            position: Vector3
                        ): Boolean {
                            val `object` = LegacyBigSpruceTree(0.75f, 4)
                            `object`.setRandomTreeHeight(rand)
                            if (!this.ensureGrowable(level, rand, position, `object`.treeHeight)) {
                                return false
                            }
                            `object`.placeObject(level, position.floorX, position.floorY, position.floorZ, rand)
                            return true
                        }
                    }
                    bigTree = true
                }

                if (bigTree) {
                    return
                }
                val blockManager = BlockManager(this.level)
                LegacyTreeGenerator.growTree(
                    blockManager,
                    position.floorX,
                    position.floorY, position.floorZ, ChorusRandom.create(),
                    getWoodType(), false
                )
                val ev = StructureGrowEvent(this, blockManager.blocks)
                Server.instance.pluginManager.callEvent(ev)
                if (ev.cancelled) {
                    return
                }
                if (level.getBlock(vector3).id == BlockID.DIRT_WITH_ROOTS) {
                    level.setBlock(vector3, get(BlockID.DIRT))
                }
                blockManager.applySubChunkUpdate(ev.blockList)
                //                for (Block block : ev.getBlockList()) {
//                    this.level.setBlock(block, block);
//                }
                return
            }

            else -> {
                val blockManager = BlockManager(this.level)
                LegacyTreeGenerator.growTree(
                    blockManager,
                    position.floorX,
                    position.floorY, position.floorZ, ChorusRandom.create(),
                    getWoodType(), false
                )
                val ev = StructureGrowEvent(this, blockManager.blocks)
                Server.instance.pluginManager.callEvent(ev)
                if (ev.cancelled) {
                    return
                }
                if (level.getBlock(vector3).id == BlockID.DIRT_WITH_ROOTS) {
                    level.setBlock(vector3, get(BlockID.DIRT))
                }
                blockManager.applySubChunkUpdate(ev.blockList)
                return
            }
        }

        if (bigTree) {
            level.setBlock(vector3, get(BlockID.AIR), direct = true, update = false)
            level.setBlock(vector3.add(1.0, 0.0, 0.0), get(BlockID.AIR), direct = true, update = false)
            level.setBlock(vector3.add(0.0, 0.0, 1.0), get(BlockID.AIR), direct = true, update = false)
            level.setBlock(vector3.add(1.0, 0.0, 1.0), get(BlockID.AIR), direct = true, update = false)
        } else {
            level.setBlock(this.position, get(BlockID.AIR), direct = true, update = false)
        }

        val blockManager = BlockManager(this.level)
        val success: Boolean = generator?.generate(blockManager, ChorusRandom.create(), vector3) == true
        val ev = StructureGrowEvent(this, blockManager.blocks)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled || !success) {
            if (bigTree) {
                level.setBlock(vector3, this, direct = true, update = false)
                level.setBlock(vector3.add(1.0, 0.0, 0.0), this, direct = true, update = false)
                level.setBlock(vector3.add(0.0, 0.0, 1.0), this, direct = true, update = false)
                level.setBlock(vector3.add(1.0, 0.0, 1.0), this, direct = true, update = false)
            } else {
                level.setBlock(this.position, this, direct = true, update = false)
            }
            return
        }

        if (level.getBlock(vector3).id == BlockID.DIRT_WITH_ROOTS) {
            level.setBlock(vector3, get(BlockID.DIRT))
        }
        blockManager.applySubChunkUpdate(ev.blockList)
    }

    private fun findSaplings(type: WoodType): Vector2? {
        val validVectorsList: MutableList<List<Vector2>> = ArrayList()
        validVectorsList.add(listOf(Vector2(0.0, 0.0), Vector2(1.0, 0.0), Vector2(0.0, 1.0), Vector2(1.0, 1.0)))
        validVectorsList.add(
            listOf(
                Vector2(0.0, 0.0),
                Vector2(-1.0, 0.0),
                Vector2(0.0, -1.0),
                Vector2(-1.0, -1.0)
            )
        )
        validVectorsList.add(
            listOf(
                Vector2(0.0, 0.0),
                Vector2(1.0, 0.0),
                Vector2(0.0, -1.0),
                Vector2(1.0, -1.0)
            )
        )
        validVectorsList.add(
            listOf(
                Vector2(0.0, 0.0),
                Vector2(-1.0, 0.0),
                Vector2(0.0, 1.0),
                Vector2(-1.0, 1.0)
            )
        )
        for (validVectors in validVectorsList) {
            var correct = true
            for (vector2 in validVectors) {
                if (!this.isSameType(position.add(vector2.x, 0.0, vector2.y), type)) correct = false
            }
            if (correct) {
                var lowestX = 0
                var lowestZ = 0
                for (vector2 in validVectors) {
                    if (vector2.floorX < lowestX) lowestX = vector2.floorX
                    if (vector2.floorY < lowestZ) lowestZ = vector2.floorY
                }
                return Vector2(lowestX.toDouble(), lowestZ.toDouble())
            }
        }
        return null
    }

    fun isSameType(pos: Vector3, type: WoodType): Boolean {
        val block = level.getBlock(pos)
        return block.id == this.id && (block as BlockSapling).getWoodType() == type
    }

    override val isFertilizable: Boolean
        get() = true
}
