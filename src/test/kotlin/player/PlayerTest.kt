package org.chorus_oss.chorus.player

import org.chorus_oss.chorus.GameMockExtension
import org.chorus_oss.chorus.TestPlayer
import org.chorus_oss.chorus.TestUtils
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.packets.MovePlayerPacket
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

@ExtendWith(GameMockExtension::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class PlayerTest {
    @Test
    @Order(1)
    fun test_player_teleport(player: TestPlayer, level: Level) {
        val p = player
        p.level = level
        p.viewDistance = 4 //view 4

        val loop = TestUtils.gameLoop0(p)

        p.teleport(Vector3(10000.0, 6.0, 10000.0))

        var limit = 100
        while (limit-- != 0) {
            try {
                if (level.isChunkLoaded(10000 shr 4, 10000 shr 4)) {
                    break
                }
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
        loop.stop()
        //verify target chunk is load
        if (limit <= 0) {
            Assertions.fail<Any>("Chunks cannot be successfully loaded in 10s")
        }
        val orderSendPk = Mockito.inOrder(p.session)
        orderSendPk.verify(p.session, Mockito.times(1)).sendPacket(
            ArgumentMatchers.any(
                MovePlayerPacket::class.java
            )
        )
        p.setPosition(Vector3(0.0, 100.0, 0.0))
    }

    @Test
    @Order(2)
    fun test_player_chunk_load(player: TestPlayer) {
        val p = player
        TestUtils.resetPlayerStatus(p)

        p.viewDistance = 4 //view 4
        p.setPosition(Vector3(0.0, 100.0, 0.0))

        val loop = TestUtils.gameLoop0(p)

        var limit = 300
        while (limit-- != 0) {
            try {
                if (49 == p.usedChunks.size) {
                    break
                }
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
        loop.stop()

        if (limit <= 0) {
            TestUtils.resetPlayerStatus(p)
            Assertions.fail<Any>("Chunks cannot be successfully loaded in 30s,the number of chunks that are now loaded: " + p.usedChunks.size)
        }
        TestUtils.resetPlayerStatus(p)
    }

    @Test
    @Order(3)
    fun test_player_chunk_unload(player: TestPlayer, level: Level) {
        TestUtils.resetPlayerStatus(player)

        player.viewDistance = 4 //view 4
        val loop = TestUtils.gameLoop0(player)

        player.setPosition(Vector3(0.0, 100.0, 0.0))
        val thread = Thread { loop.startLoop() }
        thread.start()
        var limit = 100
        while (limit-- != 0) {
            try {
                if (49 == player.usedChunks.size) {
                    break
                }
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
        if (limit <= 0) {
            TestUtils.resetPlayerStatus(player)
            Assertions.fail<Any>("Chunks cannot be successfully loaded in 10s")
        }
        var limit2 = 300
        player.setPosition(Vector3(1000.0, 100.0, 1000.0))
        while (limit2-- != 0) {
            try {
                if (50 == player.usedChunks.size) {
                    break
                }
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
        if (limit2 == 0) {
            TestUtils.resetPlayerStatus(player)
            Assertions.fail<Any>("Chunks cannot be successfully unloaded in 10s, now have chunk ${level.chunks.size}")
        }
        loop.stop()
        Assertions.assertTrue(level.chunks.containsKey(0L), "spawn chunk 0,0 should keep load")
        Assertions.assertTrue(
            player.usedChunks.contains(Level.chunkHash(61, 61)),
            "the chunk 61,61 should be loaded for player"
        )
        Assertions.assertFalse(level.chunks.containsKey(Level.chunkHash(1, 1)), "This chunk 1,1 should not be loaded")

        TestUtils.resetPlayerStatus(player)
    }
}
