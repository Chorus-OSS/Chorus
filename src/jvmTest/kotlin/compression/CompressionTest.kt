package org.chorus_oss.chorus.compression

import org.apache.commons.lang3.reflect.FieldUtils
import org.chorus_oss.chorus.GameMockExtension
import org.chorus_oss.chorus.Server
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.io.IOException
import java.nio.ByteBuffer

@ExtendWith(GameMockExtension::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class CompressionTest {
    @Test
    @Order(1)
    @Throws(IllegalAccessException::class)
    fun testDirectBuffer() {
        Server.instance.settings.networkSettings.compressionBufferSize = (CompressionProvider.MAX_INFLATE_LEN)
        val directBuffer = FieldUtils.readDeclaredStaticField(
            LibDeflateThreadLocal::class.java, "DIRECT_BUFFER", true
        ) as ThreadLocal<ByteBuffer>
        val byteBuffer = directBuffer.get()
        Assertions.assertEquals(0, byteBuffer.position())
        Assertions.assertEquals(10485760, byteBuffer.capacity())
        Assertions.assertEquals(10485760, byteBuffer.limit())
    }

    @Test
    @Order(2)
    @Throws(IOException::class)
    fun testLibDeflateInflate() {
        Server.instance.settings.networkSettings.compressionBufferSize = (512)
        ZlibChooser.setProvider(3)
        val currentProvider = ZlibChooser.currentProvider
        val bytes = ByteArray(1024)
        for (i in 0..1023) {
            bytes[i] = i.toByte()
        }
        val deflate = currentProvider.deflate(bytes, 7, false)
        val inflate = currentProvider.inflate(deflate, 1024 * 1024, false)
        Assertions.assertArrayEquals(bytes, inflate)
    }

    @Test
    @Throws(Exception::class)
    fun testZlibOriginal() {
        ZlibChooser.setProvider(0)
        val currentProvider = ZlibChooser.currentProvider
        val deflate = currentProvider.deflate(testInput, 7, true)
        Assertions.assertArrayEquals(testOutput, deflate)
        val inflate = currentProvider.inflate(deflate, testInput.size, true)
        Assertions.assertArrayEquals(testInput, inflate)
    }

    @Test
    @Throws(Exception::class)
    fun testZlibSingleThreadLowMem() {
        ZlibChooser.setProvider(1)
        val currentProvider = ZlibChooser.currentProvider
        val deflate = currentProvider.deflate(testInput, 7, true)
        Assertions.assertArrayEquals(testOutput, deflate)
        val inflate = currentProvider.inflate(deflate, testInput.size, true)
        Assertions.assertArrayEquals(testInput, inflate)
    }

    @Test
    @Throws(Exception::class)
    fun testZlibThreadLocal() {
        ZlibChooser.setProvider(2)
        val currentProvider = ZlibChooser.currentProvider
        val deflate = currentProvider.deflate(testInput, 7, true)
        Assertions.assertArrayEquals(testOutput, deflate)
        val inflate = currentProvider.inflate(deflate, testInput.size, true)
        Assertions.assertArrayEquals(testInput, inflate)
    }


    @Test
    @Throws(Exception::class)
    fun testSnappy() {
        val deflate = CompressionProvider.SNAPPY.compress(testInput, 7)!!
        val decompress = CompressionProvider.SNAPPY.decompress(deflate)
        Assertions.assertArrayEquals(testInput, decompress)
    }

    companion object {
        var testInput: ByteArray = byteArrayOf(
            123, 99, 12, -23, -43, -45, -66, 123, 12, 43, 54, 65, 67, 88,
            -123, 123, 31, 23, -122, 43, 33, 54, 66, 88, 99, 22, -1, 32, -34, 32
        )
        var testOutput: ByteArray = byteArrayOf(
            -85,
            78,
            -26,
            121,
            121,
            -11,
            -14,
            -66,
            106,
            30,
            109,
            51,
            71,
            -25,
            -120,
            -42,
            106,
            121,
            -15,
            54,
            109,
            69,
            51,
            -89,
            -120,
            100,
            -79,
            -1,
            10,
            -9,
            20,
            0
        )
    }
}
