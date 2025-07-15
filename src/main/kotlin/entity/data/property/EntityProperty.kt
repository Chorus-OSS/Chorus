package org.chorus_oss.chorus.entity.data.property

import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.protocol.packets.SyncActorPropertyPacket
import java.io.IOException

abstract class EntityProperty(private val identifier: String) {
    fun getIdentifier(): String {
        return identifier
    }

    abstract fun populateTag(tag: CompoundTag)

    companion object {
        private const val PLAYER_KEY: String = "minecraft:player"
        private const val PROPERTIES_KEY: String = "properties"

        private val entityPropertyMap: MutableMap<String, MutableList<EntityProperty>> = HashMap()
        private val nbtCache: MutableList<CompoundTag> = ArrayList()
        private var playerPropertyCache: CompoundTag = CompoundTag()

        @JvmStatic
        fun init() {
            try {
                EntityProperty::class.java.getClassLoader().getResourceAsStream("entity_properties.nbt").use { stream ->
                    val root: CompoundTag = NBTIO.readCompressed(stream)
                    root.tags.values.forEach({ uncast ->
                        if (uncast is CompoundTag) {
                            val properties: ListTag<CompoundTag> = uncast.getList("properties", CompoundTag::class.java)
                            for (property: CompoundTag in properties.all) {
                                val name: String = property.getString("name")
                                val data: EntityProperty = when (val type: Int = property.getInt("type")) {
                                    0 -> IntEntityProperty(
                                        name,
                                        property.getInt("min"),
                                        property.getInt("max"),
                                        property.getInt("min")
                                    )

                                    1 -> FloatEntityProperty(
                                        name,
                                        property.getInt("min").toFloat(),
                                        property.getInt("max").toFloat(),
                                        property.getInt("min").toFloat()
                                    )

                                    2 -> BooleanEntityProperty(name, false)
                                    3 -> {
                                        val enums: MutableList<String> = ArrayList()
                                        for (entry: StringTag in property.getList("enum", StringTag::class.java)
                                            .all) enums.add(entry.data)
                                        EnumEntityProperty(
                                            name,
                                            enums.toTypedArray(),
                                            enums.first()
                                        )
                                    }

                                    else -> throw IllegalArgumentException("Unknown EntityProperty type " + type)
                                }
                                register(uncast.getString("type"), data)
                            }
                        }
                    })
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        fun register(entityIdentifier: String, property: EntityProperty): Boolean {
            val entityProperties: MutableList<EntityProperty> =
                entityPropertyMap.getOrDefault(entityIdentifier, ArrayList())
            for (entityProperty: EntityProperty in entityProperties) {
                if (entityProperty.identifier == property.identifier) return false
            }
            entityProperties.add(property)
            entityPropertyMap.put(entityIdentifier, entityProperties)
            return true
        }

        @JvmStatic
        fun buildPacketData() {
            for (entry: Map.Entry<String, List<EntityProperty>> in entityPropertyMap.entries) {
                val listProperty: ListTag<CompoundTag> = buildPropertyList(entry.value)
                val tag: CompoundTag = CompoundTag().putList(PROPERTIES_KEY, listProperty).putString("type", entry.key)
                nbtCache.add(tag)
            }
        }

        @JvmStatic
        fun buildPlayerProperty() {
            val properties: List<EntityProperty>? = entityPropertyMap[PLAYER_KEY]
            if (properties == null) {
                playerPropertyCache = CompoundTag()
                return
            }
            val listProperty: ListTag<CompoundTag> = buildPropertyList(properties)
            playerPropertyCache = CompoundTag().putList(PROPERTIES_KEY, listProperty).putString("type", PLAYER_KEY)
        }

        @JvmStatic
        fun getPacketCache(): List<SyncActorPropertyPacket> {
            return nbtCache.map { SyncActorPropertyPacket(org.chorus_oss.nbt.tags.CompoundTag(it)) }
        }

        @JvmStatic
        fun getPlayerPropertyCache(): CompoundTag {
            return playerPropertyCache
        }

        fun getEntityProperty(identifier: String): List<EntityProperty> {
            return entityPropertyMap.getOrDefault(identifier, ArrayList())
        }

        private fun buildPropertyList(properties: List<EntityProperty>): ListTag<CompoundTag> {
            val listProperty: ListTag<CompoundTag> = ListTag()
            for (entityProperty: EntityProperty in properties) {
                val propertyTag: CompoundTag = CompoundTag()
                propertyTag.putString("name", entityProperty.getIdentifier())
                entityProperty.populateTag(propertyTag)
                listProperty.add(propertyTag)
            }
            return listProperty
        }
    }
}
