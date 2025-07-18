package org.chorus_oss.chorus.experimental.network.protocol.utils

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

const val ENCHANT_RECIPE_ID_START: Int = 100_000
val ENCHANT_RECIPE_MAP: MutableMap<UInt, EnchantmentOptionData> = mutableMapOf()
@OptIn(ExperimentalAtomicApi::class)
val ENCHANT_RECIPE_ID: AtomicInt = AtomicInt(ENCHANT_RECIPE_ID_START)