package org.chorus_oss.chorus.level

import org.chorus_oss.chorus.GameMockExtension
import org.chorus_oss.chorus.block.BlockDiamondOre
import org.chorus_oss.chorus.block.BlockGoldOre
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.biome.BiomeID
import org.chorus_oss.chorus.level.format.Chunk
import org.chorus_oss.chorus.level.format.LevelProvider
import org.chorus_oss.chorus.level.format.SubChunk
import org.chorus_oss.chorus.level.format.UnsafeChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.NBTIO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*
import java.util.function.Consumer

@ExtendWith(GameMockExtension::class)
class ChunkTest {
    @Test
    fun testSetBlockState(levelDBProvider: LevelProvider) {
        val chunk = levelDBProvider.getChunk(0, 0)!!
        chunk.setBlockState(0, 100, 0, BlockGoldOre.properties.defaultState)
        Assertions.assertEquals(BlockGoldOre.properties.defaultState, chunk.getBlockState(0, 100, 0))
    }

    @Test
    fun testSetBlockSkyLight(levelDBProvider: LevelProvider) {
        val chunk = levelDBProvider.getChunk(0, 0)!!
        chunk.setBlockSkyLight(0, 100, 0, 10)
        Assertions.assertEquals(10, chunk.getBlockSkyLight(0, 100, 0))
    }

    @Test
    fun testSetBlockLight(levelDBProvider: LevelProvider) {
        val chunk = levelDBProvider.getChunk(0, 0)!!
        chunk.setBlockLight(0, 100, 0, 10)
        Assertions.assertEquals(10, chunk.getBlockLight(0, 100, 0))
    }

    @Test
    fun testSetBiome(levelDBProvider: LevelProvider) {
        val chunk = levelDBProvider.getChunk(0, 0)!!
        chunk.setBiomeId(0, 100, 0, 10)
        Assertions.assertEquals(10, chunk.getBiomeId(0, 100, 0))
    }

    @Test
    fun test_recalculateHeightMap(levelDBProvider: LevelProvider) {
        val chunk = levelDBProvider.getChunk(1000, 1000, true)!!
        levelDBProvider.level.syncGenerateChunk(1000, 1000)
        chunk.recalculateHeightMap()
        Assertions.assertEquals(4, chunk.getHeightMap(0, 0))
    }

    @Test
    fun test_recalculateHeightMapColumn(levelDBProvider: LevelProvider) {
        val chunk = levelDBProvider.getChunk(1000, 1000, true)!!
        levelDBProvider.level.syncGenerateChunk(1000, 1000)
        chunk.recalculateHeightMapColumn(0, 0)
        Assertions.assertEquals(4, chunk.getHeightMap(0, 0))
    }

    @Test
    fun test_populateSkyLight(levelDBProvider: LevelProvider) {
        val chunk = levelDBProvider.getChunk(1000, 1000, true)!!
        levelDBProvider.level.syncGenerateChunk(1000, 1000)
        chunk.populateSkyLight()
        Assertions.assertEquals(15, chunk.getBlockSkyLight(0, 5, 0))
        Assertions.assertEquals(15, chunk.getBlockSkyLight(0, 6, 0))
    }

    @Test
    fun testSaveAndReadChunkEntity(levelDBProvider: LevelProvider) {
        val chunk = levelDBProvider.getChunk(0, 0, true)!!
        val item = Item.get(ItemID.GOLD_INGOT)
        val itemEntity = Entity.createEntity(
            EntityID.ITEM,
            chunk,
            Entity.getDefaultNBT(
                Vector3(0.0, 64.0, 0.0),
                Vector3(0.0, 0.0, 0.0),
                Random().nextFloat() * 360, 0f
            )
                .putShort("Health", 5)
                .putCompound("Item", NBTIO.putItemHelper(item))
                .putShort("PickupDelay", 10)
        ) as EntityItem?
        chunk.addEntity(itemEntity!!)

        val list =
            chunk.entities.values.stream().filter { e: Entity -> e.getEntityIdentifier() == EntityID.ITEM }.toList()
        Assertions.assertFalse(list.isEmpty())
        Assertions.assertEquals(EntityID.ITEM, list[0].getEntityIdentifier())

        levelDBProvider.saveChunk(0, 0, chunk)
        val newChunk = levelDBProvider.getChunk(0, 0)
        Assertions.assertNotNull(newChunk)
        val list2 =
            chunk.entities.values.stream().filter { e: Entity -> e.getEntityIdentifier() == EntityID.ITEM }.toList()
        Assertions.assertFalse(list2.isEmpty())
        Assertions.assertEquals(EntityID.ITEM, list2[0].getEntityIdentifier())
    }

    @Test
    fun test_getOrCreateSection(levelDBProvider: LevelProvider) {
        val chunk = levelDBProvider.getChunk(0, 0)
        val getOrCreateSection =
            Chunk::class.java.getDeclaredMethod("getOrCreateSection", Int::class.javaPrimitiveType)
        getOrCreateSection.isAccessible = true
        val s1 = getOrCreateSection.invoke(chunk, -4) as SubChunk
        Assertions.assertEquals(-4, s1.y.toInt())
        val s2 = getOrCreateSection.invoke(chunk, 19) as SubChunk
        Assertions.assertEquals(19, s2.y.toInt())
    }

    @Test
    fun testMultiThreadOperate(levelDBProvider: LevelProvider) {
        val chunk = levelDBProvider.getChunk(0, 0)!!
        val threadSet: MutableSet<Thread> = HashSet()
        for (i in 0..<Runtime.getRuntime().availableProcessors()) {
            if (i % 2 == 0) {
                val thread = Thread {
                    chunk.batchProcess { unsafeChunk: UnsafeChunk ->
                        for (k in 0..15) {
                            for (l in 0..15) {
                                for (j in unsafeChunk.dimensionData.minHeight..<unsafeChunk.dimensionData.maxHeight) {
                                    unsafeChunk.setBlockState(k, j, l, BlockGoldOre.properties.defaultState, 0)
                                    unsafeChunk.setBiomeId(k, j, l, BiomeID.BIRCH_FOREST_MUTATED)
                                }
                                unsafeChunk.setHeightMap(k, l, 4)
                            }
                        }
                    }
                }
                threadSet.add(thread)
                thread.start()
            } else {
                val thread = Thread {
                    chunk.batchProcess { unsafeChunk: UnsafeChunk ->
                        for (k in 0..15) {
                            for (l in 0..15) {
                                for (j in unsafeChunk.dimensionData.minHeight..<unsafeChunk.dimensionData.maxHeight) {
                                    unsafeChunk.setBlockState(k, j, l, BlockDiamondOre.properties.defaultState, 0)
                                    unsafeChunk.setBiomeId(k, j, l, BiomeID.DEEP_COLD_OCEAN)
                                    unsafeChunk.setBlockSkyLight(k, j, l, 4)
                                }
                            }
                        }
                    }
                }
                threadSet.add(thread)
                thread.start()
            }
        }
        threadSet.forEach(Consumer { t: Thread ->
            try {
                t.join()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        })
    }

    @Test
    fun test_SectionIsEmpty() {
        val subChunk = SubChunk(0.toByte())
        Assertions.assertTrue(subChunk.isEmpty)
    }
}
