package org.chorus_oss.chorus.lang

/**
 * 一个带有多语言功能的占位符插值文本容器，多语言功能通过[Lang]实现.
 *
 *
 * A placeholder interpolation text container with multi-language functionality,the multilingualism is implemented via [Lang].
 */
class TranslationContainer : TextContainer {
    /**
     * 将参数数组每个下标的参数插入对应占位符`{%0,%1,%2...}`
     *
     *
     * Insert the argument of each index of the params into the corresponding placeholder `{%0,%1,%2...}`
     */
    var parameters: Array<String>

    constructor(text: String) : this(text, *arrayOf<String>())

    constructor(text: String, params: String) : super(text) {
        this.parameters = arrayOf(params)
    }

    constructor(text: String, vararg params: String) : super(text) {
        this.parameters = params.toList().toTypedArray()
    }

    fun getParameter(i: Int): String? {
        return if (i >= 0 && i < parameters.size) parameters[i] else null
    }

    fun setParameter(i: Int, str: String) {
        if (i >= 0 && i < parameters.size) {
            parameters[i] = str
        }
    }

    override fun clone(): TranslationContainer {
        return TranslationContainer(this.text, *parameters.clone())
    }
}
