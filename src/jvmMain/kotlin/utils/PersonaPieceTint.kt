package org.chorus_oss.chorus.utils

import com.google.common.collect.ImmutableList

class PersonaPieceTint(val pieceType: String, colors: List<String>) {
    val colors: ImmutableList<String> = ImmutableList.copyOf(colors)
}
