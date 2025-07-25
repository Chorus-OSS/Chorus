package org.chorus_oss.chorus.level.format.leveldb

import org.chorus_oss.chorus.level.format.Chunk
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.format.LevelProvider
import org.iq80.leveldb.CompressionType
import org.iq80.leveldb.DB
import org.iq80.leveldb.Options
import org.iq80.leveldb.WriteOptions
import org.iq80.leveldb.impl.Iq80DBFactory
import java.io.IOException
import java.nio.file.Path

class LevelDBStorage(private var dimSum: Int, private val path: String, options: Options?) {
    private val db: DB

    constructor(dimSum: Int, path: String) : this(
        dimSum, path, Options()
            .createIfMissing(true)
            .compressionType(CompressionType.ZLIB_RAW)
            .blockSize(64 * 1024)
    )

    init {
        val path = Path.of(path)
        val folder = path.toFile()
        if (!folder.exists()) {
            folder.mkdirs()
        }
        require(folder.isDirectory) { "The path must be a folder" }

        val dbFolder = path.resolve("db").toFile()
        if (!dbFolder.exists()) dbFolder.mkdirs()
        db = Iq80DBFactory().open(dbFolder, options)
    }

    @Throws(IOException::class)
    fun readChunk(x: Int, z: Int, levelProvider: LevelProvider): IChunk {
        val builder: Chunk.Builder = Chunk.builder()
            .chunkX(x)
            .chunkZ(z)
            .levelProvider(levelProvider)
        LevelDBChunkSerializer.INSTANCE.deserialize(this.db, builder)
        return builder.build()
    }

    @Throws(IOException::class)
    fun writeChunk(chunk: IChunk) {
        db.createWriteBatch().use { writeBatch ->
            LevelDBChunkSerializer.INSTANCE.serialize(writeBatch, chunk)
            val writeOptions = WriteOptions()
            writeOptions.sync(true)
            db.write(writeBatch, writeOptions)
        }
    }

    @Synchronized
    fun close() {
        dimSum--
        if (dimSum <= 0) {
            try {
                db.close()
                LevelDBProvider.CACHE.remove(path)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }
}
