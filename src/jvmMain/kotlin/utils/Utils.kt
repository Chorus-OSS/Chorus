package org.chorus_oss.chorus.utils

import io.netty.buffer.ByteBuf
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import java.io.*
import java.lang.management.ManagementFactory
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.*

object Utils : Loggable {
    val random: SplittableRandom = SplittableRandom()


    @Throws(IOException::class)
    fun writeFile(fileName: String, content: InputStream) {
        writeFile(File(fileName), content)
    }

    @Throws(IOException::class)
    fun writeFile(file: File, content: String) {
        writeFile(file, ByteArrayInputStream(content.toByteArray(StandardCharsets.UTF_8)))
    }

    @Throws(IOException::class)
    fun writeFile(file: File, content: InputStream) {
        if (!file.exists()) {
            file.createNewFile()
        }
        content.use {
            FileOutputStream(file).use { stream ->
                val buffer = ByteArray(1024)
                var length: Int
                while ((content.read(buffer).also { length = it }) != -1) {
                    stream.write(buffer, 0, length)
                }
            }
        }
    }

    @Throws(IOException::class)
    fun readFile(file: File): String {
        if (!file.exists() || file.isDirectory) {
            throw FileNotFoundException()
        }
        return readFile(FileInputStream(file))
    }

    @Throws(IOException::class)
    fun readFile(filename: String): String {
        val file = File(filename)
        if (!file.exists() || file.isDirectory) {
            throw FileNotFoundException()
        }
        return readFile(FileInputStream(file))
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readFile(inputStream: InputStream): String {
        return readFile(InputStreamReader(inputStream, StandardCharsets.UTF_8))
    }

    @Throws(IOException::class)
    private fun readFile(reader: Reader): String {
        BufferedReader(reader).use { br ->
            var temp: String?
            val stringBuilder = StringBuilder()
            temp = br.readLine()
            while (temp != null) {
                if (stringBuilder.isNotEmpty()) {
                    stringBuilder.append("\n")
                }
                stringBuilder.append(temp)
                temp = br.readLine()
            }
            return stringBuilder.toString()
        }
    }

    @JvmStatic
    val allThreadDumps: String
        get() {
            val threads =
                ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)
            val builder = StringBuilder()
            for (info in threads) {
                builder.append('\n').append(info)
            }
            return builder.toString()
        }

    @JvmStatic
    fun getExceptionMessage(e: Throwable): String {
        val stringWriter = StringWriter()
        PrintWriter(stringWriter).use { printWriter ->
            e.printStackTrace(printWriter)
            printWriter.flush()
        }
        return stringWriter.toString()
    }

    fun dataToUUID(vararg params: ByteArray): UUID {
        val stream = ByteArrayOutputStream()
        for (param in params) {
            try {
                stream.write(param)
            } catch (_: IOException) {
                break
            }
        }
        return UUID.nameUUIDFromBytes(stream.toByteArray())
    }

    fun toInt(number: Any): Int {
        if (number is Int) {
            return number
        }

        return (number as Double).roundToInt()
    }

    /**
     * 返回介于最小值(包含)和最大值(包含)之间的伪随机数
     *
     *
     * Return a random number between the minimum (inclusive) and maximum (inclusive).
     *
     * @param min the min
     * @param max the max
     * @return the int
     */
    fun rand(min: Int, max: Int): Int {
        if (min == max) {
            return max
        }
        return random.nextInt(max + 1 - min) + min
    }

    fun rand(min: Float, max: Float): Float {
        if (min == max) {
            return max
        }
        return min + random.nextFloat() * (max - min)
    }

    fun rand(min: Double, max: Double): Double {
        if (min == max) {
            return max
        }
        return min + random.nextDouble() * (max - min)
    }

    fun rand(): Boolean {
        return random.nextBoolean()
    }

    /**
     * A way to tell the java compiler to do not replace the users of a `public static final int` constant
     * with the value defined in it, forcing the JVM to get the value directly from the class, preventing
     * binary incompatible changes.
     *
     * @param value The value to be assigned to the field.
     * @return The same value that was passed as parameter
     */
    @JvmStatic
    fun dynamic(value: Int): Int {
        return value
    }

    /**
     * A way to tell the java compiler to do not replace the users of a `public static final` constant
     * with the value defined in it, forcing the JVM to get the value directly from the class, preventing
     * binary incompatible changes.
     *
     * @param value The value to be assigned to the field.
     * @return The same value that was passed as parameter
     */
    fun <T> dynamic(value: T): T {
        return value
    }

    fun getLevelBlocks(level: Level, bb: AxisAlignedBB): Array<Block> {
        val minX = floor(min(bb.minX, bb.maxX)).toInt()
        val minY = floor(min(bb.minY, bb.maxY)).toInt()
        val minZ = floor(min(bb.minZ, bb.maxZ)).toInt()
        val maxX = floor(max(bb.minX, bb.maxX)).toInt()
        val maxY = floor(max(bb.minY, bb.maxY)).toInt()
        val maxZ = floor(max(bb.minZ, bb.maxZ)).toInt()

        val blocks: MutableList<Block> = ArrayList()
        val vec = Vector3()

        for (z in minZ..maxZ) {
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    blocks.add(level.getBlock(vec.setComponents(x.toDouble(), y.toDouble(), z.toDouble()), false))
                }
            }
        }

        return blocks.toTypedArray()
    }

    fun hasCollisionTickCachedBlocks(level: Level, bb: AxisAlignedBB): Boolean {
        val minX = floor(bb.minX).toInt()
        val minY = floor(bb.minY).toInt()
        val minZ = floor(bb.minZ).toInt()
        val maxX = ceil(bb.maxX).toInt()
        val maxY = ceil(bb.maxY).toInt()
        val maxZ = ceil(bb.maxZ).toInt()

        for (z in minZ..maxZ) {
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    val block = level.getTickCachedBlock(x, y, z, false)
                    //判断是否和非空气方块有碰撞
                    if (block.collidesWithBB(bb) && !block.canPassThrough()) {
                        return true
                    }
                }
            }
        }

        return false
    }

    /**
     * @return 0 if no collision, else a byte in the format of 0b 00 xx yy zz <br></br>
     * if xx is 01, then the block at the minX side of the bb has collision <br></br>
     * if xx is 11, then the block at the maxX side of the bb has collision <br></br>
     * if xx is 00, then xx is not used <br></br>
     * if yy is 01, then the block at the minY side of the bb has collision <br></br>
     * if yy is 11, then the block at the maxY side of the bb has collision <br></br>
     * if yy is 00, then yy is not used <br></br>
     * if zz is 01, then the block at the minZ side of the bb has collision <br></br>
     * if zz is 11, then the block at the maxZ side of the bb has collision <br></br>
     * if zz is 00, then zz is not used <br></br>
     */
    fun hasCollisionTickCachedBlocksWithInfo(level: Level, bb: AxisAlignedBB): Byte {
        val minX = floor(bb.minX).toInt()
        val minY = floor(bb.minY).toInt()
        val minZ = floor(bb.minZ).toInt()
        val maxX = ceil(bb.maxX).toInt()
        val maxY = ceil(bb.maxY).toInt()
        val maxZ = ceil(bb.maxZ).toInt()
        val centerX = (maxX + minX).toFloat() / 2
        val centerY = (maxY + minY).toFloat() / 2
        val centerZ = (maxZ + minZ).toFloat() / 2
        var returnValue: Byte = 0

        for (z in minZ..maxZ) {
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    val block = level.getTickCachedBlock(x, y, z, false)
                    //判断是否和非空气方块有碰撞
                    if (block.collidesWithBB(bb) && !block.canPassThrough()) {
                        if (x < centerX) {
                            returnValue = (returnValue.toInt() or 16).toByte()
                        } else if (x > centerX) {
                            returnValue = (returnValue.toInt() or 48).toByte()
                        }
                        if (y < centerY) {
                            returnValue = (returnValue.toInt() or 4).toByte()
                        } else if (y > centerY) {
                            returnValue = (returnValue.toInt() or 12).toByte()
                        }
                        if (z < centerZ) {
                            returnValue = (returnValue.toInt() or 1).toByte()
                        } else if (z > centerZ) {
                            returnValue = (returnValue.toInt() or 3).toByte()
                        }
                        return returnValue
                    }
                }
            }
        }

        return 0
    }

    @JvmStatic
    fun computeRequiredBits(min: Int, max: Int): Byte {
        val value = max - min
        if (value <= 1) return 1
        var bits: Byte = 1
        while (value >= (1 shl bits.toInt())) {
            bits++
        }
        return bits
    }

    fun convertByteBuf2Array(buf: ByteBuf): ByteArray {
        val payload = ByteArray(buf.readableBytes())
        buf.readBytes(payload)
        return payload
    }
}
