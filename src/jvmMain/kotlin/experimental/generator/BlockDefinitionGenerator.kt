package org.chorus_oss.chorus.experimental.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.joinToCode
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockProperties
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.block.property.type.EnumPropertyType
import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.registry.BlockRegistry.Companion.CACHE_CONSTRUCTORS
import org.chorus_oss.chorus.registry.BlockRegistry.Companion.PROPERTIES
import org.chorus_oss.chorus.utils.BlockColor
import org.chorus_oss.chorus.utils.Loggable
import java.io.File
import kotlin.reflect.KClass

object BlockDefinitionGenerator : Loggable {
    val properties = mutableMapOf<String, BlockProperties>()
    val components = mutableMapOf<String, MutableList<Pair<String, String>>>()
    val permutations = mutableMapOf<String, MutableList<Pair<String, MutableList<Pair<String, String>>>>>()
    val extraImports = mutableMapOf<String, MutableList<Pair<String, String>>>()

    data class AutogenData(
        val identifier: String,
        val properties: BlockProperties,
        val components: MutableList<Pair<String, String>>,
        val permutations: MutableList<Pair<String, MutableList<Pair<String, String>>>>,
        val extraImports: MutableList<Pair<String, String>>
    )

    fun run() {
        log.info("Starting BlockDefinitionGenerator")
        populateFromProperties(PROPERTIES)
        populateFromConstructors(CACHE_CONSTRUCTORS)
        buildDefinitions()
        log.info("Finished BlockDefinitionGenerator")
    }

    fun populateFromClasses(classes: Map<String, KClass<out Block>>) {

    }

    fun populateFromConstructors(constructors: Map<String, (BlockState?) -> Block>) {
        constructors.forEach {
            val properties = properties[it.key]!!
            val instances = properties.allStates.mapNotNull { s ->
                try {
                    it.value(s)
                } catch (_: Throwable) {
                    log.error("Error occurred while creating instance of ${it.key}")
                    null
                }
            }
            val default: Block = requireNotNull(instances.find { b -> b.isDefaultState }) {
                "Default block instance not found for ${it.key}"
            }
            val permutations = instances.filter { s -> !s.isDefaultState }

            populateFromInstance(default, null)
            permutations.forEach { p -> populateFromInstance(p, default) }
        }
    }

    fun populateFromInstance(instance: Block, default: Block?) {
        val it = instance

        val components = this.components.computeIfAbsent(it.properties.identifier) { mutableListOf() }
        val permutations = this.permutations.computeIfAbsent(it.properties.identifier) { mutableListOf() }
        val extraImports = this.extraImports.computeIfAbsent(it.properties.identifier) { mutableListOf() }

        val isPermutation = default != null

        val componentGenerators: MutableList<Pair<String, () -> String?>> = mutableListOf(
            Pair("SolidComponent") { if (it.isSolid != (default?.isSolid ?: true)) "(solid = false)" else null },
            Pair("TransparentComponent") { if (it.isTransparent != (default?.isTransparent ?: false)) "(transparent = true)" else null },
            Pair("MapColorComponent") {
                if (it.color != (default?.color ?: BlockColor.VOID_BLOCK_COLOR)) {
                    with(it.color) {
                        "(r = $red, g = $green, b = $blue, a = $alpha)"
                    }
                } else null
            },
            Pair("FrictionComponent") {
                if (it.frictionFactor != (default?.frictionFactor ?: 0.6)) "(friction = ${it.frictionFactor}f)" else null
            },
            Pair("InternalFrictionComponent") {
                if (it.passableBlockFrictionFactor != (default?.passableBlockFrictionFactor ?: 1.0)) "(internalFriction = ${it.passableBlockFrictionFactor}f)" else null
            },
            Pair("LightEmissionComponent") {
                if (it.lightLevel != (default?.lightLevel ?: 0)) "(emission = ${it.lightLevel})" else null
            },
            Pair("LightDampeningComponent") {
                if (it.lightFilter != (default?.lightFilter ?: 15)) {
                    "(dampening = ${it.lightFilter})"
                } else null
            },
            Pair("ReplaceableComponent") {
                if (it.canBeReplaced() != (default?.canBeReplaced() ?: false)) "" else null
            },
            Pair("FlammableComponent") {
                if (
                    it.burnChance != (default?.burnChance ?: 0)
                    || it.burnAbility != (default?.burnAbility ?: 0)
                    ) "(catchChance = ${it.burnChance}, destroyChance = ${it.burnAbility})"
                else null
            },
            Pair("MineableComponent") {
                if (it.hardness != (default?.hardness ?: 10.0)) "(hardness = ${it.hardness}f)" else null
            },
            Pair("MoveableComponent") {
                if (
                    it.canBePushed() != (default?.canBePushed() ?: true) ||
                    (
                        it.canBePulled() != (default?.canBePulled() ?: true)
                        || it.sticksToPiston() != (default?.sticksToPiston() ?: true)
                    )
                    || it.breaksWhenMoved() != (default?.breaksWhenMoved() ?: false)
                    || it.canSticksBlock() != (default?.canSticksBlock() ?: false)
                    ) {
                    val push = it.canBePushed()
                    val pull = (it.canBePulled() || it.sticksToPiston())
                    val movement = if (it.breaksWhenMoved()) {
                        "MoveableComponent.Movement.Break"
                    } else if (push && pull) {
                        "MoveableComponent.Movement.Both"
                    } else if (push) {
                        "MoveableComponent.Movement.Push"
                    } else {
                        "MoveableComponent.Movement.None"
                    }

                    val sticky = it.canSticksBlock()

                    "(movement = $movement, sticky = $sticky)"
                } else null
            },
            Pair("CollisionBoxComponent") {
                val collision = it.collisionBoundingBox

                val notDefault = (collision?.maxX != (default?.collisionBoundingBox?.maxX ?: 1.0)
                        || collision.maxY != (default?.collisionBoundingBox?.maxY ?: 1.0)
                        || collision.maxZ != (default?.collisionBoundingBox?.maxZ ?: 1.0)
                        || collision.minX != (default?.collisionBoundingBox?.minX ?: 0.0)
                        || collision.minY != (default?.collisionBoundingBox?.minY ?: 0.0)
                        || collision.minZ != (default?.collisionBoundingBox?.minZ ?: 0.0))

                if (notDefault) {
                    if (collision == null && (default == null || default.collisionBoundingBox != null)) {
                        "(enabled = false)"
                    } else if (collision != null) {
                        val origin = "Vector3f(x = ${collision.minX}f, y = ${collision.minY}f,z = ${collision.minZ}f)"
                        val size = "Vector3f(x = ${collision.maxX - collision.minX}f, y = ${collision.maxY - collision.minY}f, z = ${collision.maxZ - collision.minZ}f)"

                        if (it.canPassThrough()) {
                            "(origin = $origin, size = $size, enabled = false)"
                        } else {
                            "(origin = $origin, size = $size)"
                        }.also {
                            extraImports.add("org.chorus_oss.chorus.math" to "Vector3f")
                        }
                    } else null
                }
                else null
            }
        )

        val componentPairs = mutableListOf<Pair<String, String>>()

        componentGenerators.forEach { gen ->
            val pair = try {
                val generated = gen.second() ?: return@forEach
                gen.first to generated
            } catch (t: Throwable) {
                log.error("Error occurred while generating \"${gen.first}\" for \"${it.properties.identifier}\" from instance, inserting TODO")
                "TODO" to "(\"${gen.first}\")"
            }
            componentPairs.add(pair)
        }

        if (isPermutation && componentPairs.isNotEmpty()) {
            val permutationStates = instance.blockState.blockPropertyValues.associate {
                it.propertyType.name to when(it) {
                    is EnumPropertyType.EnumPropertyValue -> it.getSerializedValue()
                    else -> it.value
                }
            }
            val defaultStates = default.blockState.blockPropertyValues.associate {
                it.propertyType.name to when(it) {
                    is EnumPropertyType.EnumPropertyValue -> it.getSerializedValue()
                    else -> it.value
                }
            }

            val conditions = mutableMapOf<String, String>()

            permutationStates.forEach {
                val default = defaultStates[it.key]
                if (it.value != default) {
                    conditions[it.key] = when (it.value) {
                        is String -> "\"${it.value}\""
                        else -> "${it.value}"
                    }
                }
            }

            val condition = conditions.entries.joinToString(prefix = "{ ", separator = " && ", postfix = " }") {
                "it[\"${it.key}\"] == ${it.value}"
            }

            permutations.add(condition to componentPairs)
        } else {
            components.addAll(componentPairs)
        }
    }

    fun populateFromProperties(properties: Map<String, BlockProperties>) {
        this.properties += properties
    }

    fun buildDefinitions() {
        val autogenData = mutableListOf<AutogenData>()

        this.properties.forEach {
            val components = this.components[it.key] ?: mutableListOf()
            val permutations = this.permutations[it.key] ?: mutableListOf()
            val extraImports = this.extraImports[it.key] ?: mutableListOf()

            autogenData += AutogenData(it.key, it.value, components, permutations, extraImports)
        }

        val objects = autogenData.map { generateDefinition(it) }
        val objectsString = objects.joinToString(", ") { it }

        val listOfObjects = PropertySpec.builder("definitions", List::class.parameterizedBy(BlockDefinition::class))
            .initializer("listOf($objectsString)")
            .build()

        val fileBuilder = FileSpec.builder("org.chorus_oss.chorus.experimental.block.generated", "Definitions")
            .addProperty(listOfObjects)
            .apply {
                for (obj in objects) {
                    addImport("org.chorus_oss.chorus.experimental.block.generated.definitions", obj)
                }
            }

        val path = File(Server.instance.dataPath + "generated/")
        if (!path.exists()) {
            path.mkdirs()
        }

        val fileSpec = fileBuilder.build()
        fileSpec.writeTo(path)
    }

    fun generateDefinition(genData: AutogenData): String {
        val properties = genData.properties
        val components = genData.components
        val permutations = genData.permutations
        val extraImports = genData.extraImports

        val identifier = properties.identifier
        val trimmedIdentifier = identifier.substringAfter(':')
        val pascalIdentifier = trimmedIdentifier.split('_').joinToString("") { it.replaceFirstChar(Char::uppercase)  }

        val states = properties.getPropertyTypeSet().toList().map {
            var autoName = it.name.split("_", ":")
            .mapIndexed { index, word ->
                if (index == 0) word else word.replaceFirstChar(Char::uppercase)
            }
            .joinToString("")

            when (it.name) {
                "age", "rail_direction" -> {
                    autoName += it.validValues.size
                }
            }

            autoName
        }.sorted()


        val hasStates = states.isNotEmpty()
        val hasComponents = components.isNotEmpty()
        val hasPermutations = permutations.isNotEmpty()

        val imports = mutableMapOf<String, String>()

        val formattedStates = states.joinToString(", ") { "CommonStates.$it"}
        val formattedComponents = components.joinToString(", ") {
            "${it.first}${it.second}"
        }
        val formattedPermutations = permutations.joinToString(", ") {
            val formattedSubComponents = it.second.joinToString(", ") { comp ->
                "${comp.first}${comp.second}"
            }
            "Permutation(${it.first}, listOf($formattedSubComponents)) "
        }

        if (hasStates) imports.putIfAbsent("CommonStates", "org.chorus_oss.chorus.experimental.block.state")

        if (hasComponents) {
            components.forEach {
                if (it.first != "TODO") imports.putIfAbsent(it.first, "org.chorus_oss.chorus.experimental.block.components")
            }
        }

        if (hasPermutations) {
            permutations.forEach {
                it.second.forEach { comp ->
                    if (comp.first != "TODO") imports.putIfAbsent(comp.first, "org.chorus_oss.chorus.experimental.block.components")
                }
            }
        }

        extraImports.forEach {
            imports.putIfAbsent(it.second, it.first)
        }

        val blockObject = TypeSpec.objectBuilder(pascalIdentifier)
            .superclass(ClassName("org.chorus_oss.chorus.experimental.block", "BlockDefinition"))
            .addSuperclassConstructorParameter("identifier = %S", identifier)
            .apply {
                if (hasStates) addSuperclassConstructorParameter("states = listOf($formattedStates)")
                if (hasComponents) addSuperclassConstructorParameter("components = listOf($formattedComponents)")
                if (hasPermutations) addSuperclassConstructorParameter("permutations = listOf($formattedPermutations)")
            }
            .build()

        val fileBuilder = FileSpec.builder("org.chorus_oss.chorus.experimental.block.generated.definitions", pascalIdentifier)
            .addType(blockObject)

        imports.forEach {
            fileBuilder.addImport(it.value, it.key)
        }

        val path = File(Server.instance.dataPath + "generated/")
        if (!path.exists()) {
            path.mkdirs()
        }

        val fileSpec = fileBuilder.build()
        fileSpec.writeTo(path)

        return pascalIdentifier
    }
}