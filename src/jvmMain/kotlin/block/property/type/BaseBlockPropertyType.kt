package org.chorus_oss.chorus.block.property.type

abstract class BaseBlockPropertyType<DATATYPE> protected constructor(
    override val name: String,
    override val validValues: List<DATATYPE>,
    override val defaultValue: DATATYPE,
    override val bitSize: Byte
) : BlockPropertyType<DATATYPE>
