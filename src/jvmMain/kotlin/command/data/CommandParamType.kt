package org.chorus_oss.chorus.command.data

enum class CommandParamType(@JvmField val id: Int) {
    INT(1),
    FLOAT(3),
    VALUE(4),
    WILDCARD_INT(5),
    TARGET(8),
    WILDCARD_TARGET(10),
    EQUIPMENT_SLOT(47),
    STRING(56),
    BLOCK_POSITION(64),
    POSITION(65),
    MESSAGE(68),
    RAWTEXT(70),
    JSON(74),
    TEXT(70),  // backwards compatibility
    COMMAND(87),
    FILE_PATH(17),
    OPERATOR(6),
    COMPARE_OPERATOR(7),
    FULL_INTEGER_RANGE(23),
    BLOCK_STATES(84)
}
