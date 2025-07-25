package org.chorus_oss.chorus.level

import org.apache.commons.lang3.reflect.FieldUtils
import org.chorus_oss.chorus.GameMockExtension
import org.chorus_oss.chorus.TestPlayer
import org.chorus_oss.chorus.math.Vector3
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(GameMockExtension::class)
class PlayerChunkManagerTest {
    @Test
    @Throws(IllegalAccessException::class)
    fun test_chunkDistanceComparator(player: TestPlayer) {
        val chunkDistanceComparator = FieldUtils.getDeclaredField(
            PlayerChunkManager::class.java, "chunkDistanceComparator", true
        )
        val playerChunkManager = PlayerChunkManager(player)
        player.setPosition(Vector3(0.0, 100.0, 0.0))


        val comparator = chunkDistanceComparator[playerChunkManager] as Comparator<Long>
        val priorityQueue = PriorityQueue(10 * 10, comparator)
        priorityQueue.add(111L)
        priorityQueue.add(222L)
        priorityQueue.add(22L)
        priorityQueue.add(55L)
        val d1 = priorityQueue.poll()
        Assertions.assertEquals(22, d1)
        val d2 = priorityQueue.poll()
        Assertions.assertEquals(55, d2)
        priorityQueue.add(d1)
        Assertions.assertEquals(22, priorityQueue.poll())
    }
}
