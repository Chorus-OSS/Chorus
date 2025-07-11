package org.chorus_oss.chorus.network

import org.chorus_oss.chorus.utils.SemVersion

object ProtocolInfo {
    const val GAME_VERSION_NET = "1.21.93"
    const val GAME_VERSION_STR = "v$GAME_VERSION_NET"

    val GAME_VERSION = SemVersion(1, 21, 9, 3, 0)

    val BLOCK_STATE_VERSION_NO_REVISION =
        (GAME_VERSION.major shl 24) or (GAME_VERSION.minor shl 16) or (GAME_VERSION.patch shl 8)

    const val LOGIN_PACKET = 1

    // MIGRATED = 2
    // MIGRATED = 3
    // MIGRATED = 4
    // MIGRATED = 5
    // MIGRATED = 6
    // MIGRATED = 7
    // MIGRATED = 8
    const val TEXT_PACKET = 9

    // MIGRATED = 10
    const val START_GAME_PACKET = 11

    // MIGRATED = 12
    // MIGRATED = 13
    // MIGRATED = 14
    // MIGRATED = 15
    // MIGRATED = 16
    // MIGRATED = 17
    const val MOVE_ENTITY_ABSOLUTE_PACKET = 18
    const val MOVE_PLAYER_PACKET = 19

    // UNUSED = 20
    // MIGRATED = 21

    // MIGRATED = 22
    // UNUSED = 23
    // UNUSED = 24
    const val LEVEL_EVENT_PACKET = 25

    // MIGRATED = 26
    const val ENTITY_EVENT_PACKET = 27
    const val MOB_EFFECT_PACKET = 28
    const val UPDATE_ATTRIBUTES_PACKET = 29
    const val INVENTORY_TRANSACTION_PACKET = 30
    const val MOB_EQUIPMENT_PACKET = 31
    const val MOB_ARMOR_EQUIPMENT_PACKET = 32
    const val INTERACT_PACKET = 33

    // MIGRATED = 34
    // MIGRATED = 35
    const val PLAYER_ACTION_PACKET = 36

    // UNUSED = 37
    // MIGRATED = 38
    // MIGRATED = 39
    // MIGRATED = 40
    // MIGRATED = 41
    // MIGRATED = 42
    // MIGRATED = 43
    const val ANIMATE_PACKET = 44

    // MIGRATED = 45
    // MIGRATED = 46
    // MIGRATED = 47
    // MIGRATED = 48
    // MIGRATED = 49
    // MIGRATED = 50
    // MIGRATED = 51
    const val CRAFTING_DATA_PACKET = 52

    // UNUSED = 53
    // MIGRATED = 54
    // UNUSED = 55
    // MIGRATED = 56
    // UNUSED = 57
    // MIGRATED = 58
    // MIGRATED = 59
    // MIGRATED = 60
    // MIGRATED = 61
    // MIGRATED = 62
    const val PLAYER_LIST_PACKET = 63

    // MIGRATED = 64
    // DEPRECATED = 65
    // DEPRECATED = 66
    // MIGRATED = 67
    // MIGRATED = 68
    // MIGRATED = 69
    // MIGRATED = 70
    // UNUSED = 71
    // MIGRATED = 72
    // MIGRATED = 73
    // MIGRATED = 74
    // MIGRATED = 75
    const val AVAILABLE_COMMANDS_PACKET = 76

    // MIGRATED = 77
    // MIGRATED = 78
    // MIGRATED = 79
    // MIGRATED = 80
    // MIGRATED = 81
    // MIGRATED = 82
    // MIGRATED = 83
    // MIGRATED = 84
    // MIGRATED = 85
    // MIGRATED = 86
    // MIGRATED = 87
    const val SET_TITLE_PACKET = 88

    // MIGRATED = 89
    const val STRUCTURE_BLOCK_UPDATE_PACKET = 90

    // MIGRATED = 91
    // MIGRATED = 92
    const val PLAYER_SKIN_PACKET = 93

    // MIGRATED = 94
    // MIGRATED = 95
    // MIGRATED = 96
    // MIGRATED = 97
    const val NPC_REQUEST_PACKET = 98

    // MIGRATED = 99
    // MIGRATED = 100
    const val MODAL_FORM_RESPONSE_PACKET = 101

    // MIGRATED = 102
    // MIGRATED = 103
    // MIGRATED = 104
    // MIGRATED = 105
    // MIGRATED = 106
    // MIGRATED = 107
    const val SET_SCORE_PACKET = 108

    // MIGRATED = 109
    // MIGRATED = 110
    const val MOVE_ENTITY_DELTA_PACKET = 111

    // MIGRATED = 112
    // MIGRATED = 113
    // MIGRATED = 114
    // DEPRECATED = 115
    // UNUSED = 116
    // UNUSED = 117
    // MIGRATED = 118
    // MIGRATED = 119
    // UNUSED = 120
    // MIGRATED = 121
    // MIGRATED = 122
    const val LEVEL_SOUND_EVENT_PACKET = 123
    const val LEVEL_EVENT_GENERIC_PACKET = 124

    // MIGRATED = 125
    // UNUSED = 126
    // UNUSED = 127
    // UNUSED = 128
    // MIGRATED = 129
    // MIGRATED = 130
    // MIGRATED = 131
    // MIGRATED = 132
    // MIGRATED = 133
    // UNUSED = 134
    // MIGRATED = 135
    // MIGRATED = 136
    // MIGRATED = 137
    // MIGRATED = 138
    // MIGRATED = 139
    // MIGRATED = 140
    // MIGRATED = 141
    // MIGRATED = 142
    // MIGRATED = 143
    const val PLAYER_AUTH_INPUT_PACKET = 144

    // MIGRATED = 145
    const val PLAYER_ENCHANT_OPTIONS_PACKET = 146
    const val ITEM_STACK_REQUEST_PACKET = 147

    // MIGRATED = 148
    // MIGRATED = 149
    // MIGRATED = 150
    // MIGRATED = 151
    // MIGRATED = 152
    // MIGRATED = 153
    // MIGRATED = 154
    // MIGRATED = 155
    // MIGRATED = 156
    // MIGRATED = 157
    // MIGRATED = 158
    // MIGRATED = 159
    // MIGRATED = 160
    // MIGRATED = 161
    // MIGRATED = 162
    // UNUSED = 163
    // MIGRATED = 164
    // MIGRATED = 165
    // MIGRATED = 166
    // MIGRATED = 167
    // MIGRATED = 168
    // MIGRATED = 169
    // MIGRATED = 170
    // MIGRATED = 171
    // MIGRATED = 172
    // UNUSED = 173
    // MIGRATED = 174
    // MIGRATED = 175
    // MIGRATED = 176
    // MIGRATED = 177
    // MIGRATED = 178
    // MIGRATED = 179
    // MIGRATED = 180
    // MIGRATED = 181
    // MIGRATED = 182
    // MIGRATED = 183
    // MIGRATED = 184
    const val REQUEST_PERMISSIONS_PACKET = 185

    // MIGRATED = 186
    // MIGRATED = 187
    // MIGRATED = 188
    // MIGRATED = 189
    // MIGRATED = 190
    // MIGRATED = 191
    // MIGRATED = 192
    // MIGRATED = 193
    // MIGRATED = 194
    // MIGRATED = 195
    // MIGRATED = 196
    // UNUSED = 197
    // MIGRATED = 198
    // MIGRATED = 199
    // UNUSED = 200
    // ...
    // UNUSED = 299
    // MIGRATED = 300
    // UNUSED = 301
    // MIGRATED = 302
    // MIGRATED = 303
    // MIGRATED = 304
    // MIGRATED = 305
    // MIGRATED = 306
    // MIGRATED = 307
    // MIGRATED = 308
    // MIGRATED = 309
    // MIGRATED = 310
    // UNUSED = 311
    // MIGRATED = 312
    // MIGRATED = 313
    // MIGRATED = 314
    // MIGRATED = 315
    // MIGRATED = 316
    // MIGRATED = 317
    // MIGRATED = 318
    // UNUSED = 319
    // MIGRATED = 320
    // MIGRATED = 321
    // MIGRATED = 322
    // MIGRATED = 323
    // MIGRATED = 324
    // MIGRATED = 325
    // MIGRATED = 326
    // MIGRATED = 327
    // MIGRATED = 328
}