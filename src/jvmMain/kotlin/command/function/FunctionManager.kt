package org.chorus_oss.chorus.command.function

import org.chorus_oss.chorus.command.data.CommandEnum
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class FunctionManager(private val rootPath: Path) {
    val functions: MutableMap<String, Function> = HashMap()

    init {
        try {
            if (!Files.exists(this.rootPath)) {
                Files.createDirectories(this.rootPath)
            }
            loadFunctions()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    constructor(rootPath: String) : this(Path.of(rootPath))

    fun loadFunctions() {
        try {
            Files.walkFileTree(this.rootPath, object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (file.toString().endsWith(".mcfunction")) {
                        functions[file.toString().replace(rootPath.toString() + "\\", "").replace("\\\\".toRegex(), "/")
                            .replace(".mcfunction", "")] = Function.Companion.fromPath(file)
                    }
                    return FileVisitResult.CONTINUE
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun reload() {
        functions.clear()
        loadFunctions()
        CommandEnum.Companion.FUNCTION_FILE.updateSoftEnum()
    }

    fun containFunction(name: String): Boolean {
        return functions.containsKey(name)
    }

    fun getFunction(name: String): Function? {
        return functions[name]
    }
}
