package org.chorus_oss.chorus.utils.exception

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Supports parameter formatting as used in ParameterizedMessage and ReusableParameterizedMessage.
 */
internal object ParameterFormatter {
    /**
     * Prefix for recursion.
     */
    const val RECURSION_PREFIX: String = "[..."

    /**
     * Suffix for recursion.
     */
    const val RECURSION_SUFFIX: String = "...]"

    /**
     * Prefix for errors.
     */
    const val ERROR_PREFIX: String = "[!!!"

    /**
     * Separator for errors.
     */
    const val ERROR_SEPARATOR: String = "=>"

    /**
     * Separator for error messages.
     */
    const val ERROR_MSG_SEPARATOR: String = ":"

    /**
     * Suffix for errors.
     */
    const val ERROR_SUFFIX: String = "!!!]"

    private const val DELIM_START = '{'
    private const val DELIM_STOP = '}'
    private const val ESCAPE_CHAR = '\\'

    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    /**
     * Counts the number of unescaped placeholders in the given messagePattern.
     *
     * @param messagePattern the message pattern to be analyzed.
     * @return the number of unescaped placeholders.
     */
    fun countArgumentPlaceholders(messagePattern: String?): Int {
        if (messagePattern == null) {
            return 0
        }
        val length = messagePattern.length
        var result = 0
        var isEscaped = false
        var i = 0
        while (i < length - 1) {
            val curChar = messagePattern[i]
            if (curChar == ESCAPE_CHAR) {
                isEscaped = !isEscaped
            } else if (curChar == DELIM_START) {
                if (!isEscaped && messagePattern[i + 1] == DELIM_STOP) {
                    result++
                    i++
                }
                isEscaped = false
            } else {
                isEscaped = false
            }
            i++
        }
        return result
    }

    /**
     * Counts the number of unescaped placeholders in the given messagePattern.
     *
     * @param messagePattern the message pattern to be analyzed.
     * @return the number of unescaped placeholders.
     */
    fun countArgumentPlaceholders2(messagePattern: String?, indices: IntArray): Int {
        if (messagePattern == null) {
            return 0
        }
        val length = messagePattern.length
        var result = 0
        var isEscaped = false
        var i = 0
        while (i < length - 1) {
            val curChar = messagePattern[i]
            if (curChar == ESCAPE_CHAR) {
                isEscaped = !isEscaped
                indices[0] = -1 // escaping means fast path is not available...
                result++
            } else if (curChar == DELIM_START) {
                if (!isEscaped && messagePattern[i + 1] == DELIM_STOP) {
                    indices[result] = i
                    result++
                    i++
                }
                isEscaped = false
            } else {
                isEscaped = false
            }
            i++
        }
        return result
    }

    /**
     * Counts the number of unescaped placeholders in the given messagePattern.
     *
     * @param messagePattern the message pattern to be analyzed.
     * @return the number of unescaped placeholders.
     */
    fun countArgumentPlaceholders3(messagePattern: CharArray, length: Int, indices: IntArray): Int {
        var result = 0
        var isEscaped = false
        var i = 0
        while (i < length - 1) {
            val curChar = messagePattern[i]
            if (curChar == ESCAPE_CHAR) {
                isEscaped = !isEscaped
            } else if (curChar == DELIM_START) {
                if (!isEscaped && messagePattern[i + 1] == DELIM_STOP) {
                    indices[result] = i
                    result++
                    i++
                }
                isEscaped = false
            } else {
                isEscaped = false
            }
            i++
        }
        return result
    }

    /**
     * Replace placeholders in the given messagePattern with arguments.
     *
     * @param messagePattern the message pattern containing placeholders.
     * @param arguments      the arguments to be used to replace placeholders.
     * @return the formatted message.
     */
    fun format(messagePattern: String?, arguments: Array<Any>?): String {
        val result = StringBuilder()
        val argCount = arguments?.size ?: 0
        formatMessage(result, messagePattern, arguments, argCount)
        return result.toString()
    }

    /**
     * Replace placeholders in the given messagePattern with arguments.
     *
     * @param buffer         the buffer to write the formatted message into
     * @param messagePattern the message pattern containing placeholders.
     * @param arguments      the arguments to be used to replace placeholders.
     */
    fun formatMessage2(
        buffer: StringBuilder, messagePattern: String?,
        arguments: Array<Any>?, argCount: Int, indices: IntArray
    ) {
        if (messagePattern == null || arguments == null || argCount == 0) {
            buffer.append(messagePattern)
            return
        }
        var previous = 0
        for (i in 0..<argCount) {
            buffer.append(messagePattern, previous, indices[i])
            previous = indices[i] + 2
            recursiveDeepToString(arguments[i], buffer, null)
        }
        buffer.append(messagePattern, previous, messagePattern.length)
    }

    /**
     * Replace placeholders in the given messagePattern with arguments.
     *
     * @param buffer         the buffer to write the formatted message into
     * @param messagePattern the message pattern containing placeholders.
     * @param arguments      the arguments to be used to replace placeholders.
     */
    fun formatMessage3(
        buffer: StringBuilder, messagePattern: CharArray?, patternLength: Int,
        arguments: Array<Any>?, argCount: Int, indices: IntArray
    ) {
        if (messagePattern == null) {
            return
        }
        if (arguments == null || argCount == 0) {
            buffer.append(messagePattern)
            return
        }
        var previous = 0
        for (i in 0..<argCount) {
            buffer.appendRange(messagePattern, previous, previous + indices[i])
            previous = indices[i] + 2
            recursiveDeepToString(arguments[i], buffer, null)
        }
        buffer.appendRange(messagePattern, previous, previous + patternLength)
    }

    /**
     * Replace placeholders in the given messagePattern with arguments.
     *
     * @param buffer         the buffer to write the formatted message into
     * @param messagePattern the message pattern containing placeholders.
     * @param arguments      the arguments to be used to replace placeholders.
     */
    fun formatMessage(
        buffer: StringBuilder, messagePattern: String?,
        arguments: Array<Any>?, argCount: Int
    ) {
        if (messagePattern == null || arguments == null || argCount == 0) {
            buffer.append(messagePattern)
            return
        }
        var escapeCounter = 0
        var currentArgument = 0
        var i = 0
        val len = messagePattern.length
        while (i < len - 1) {
            // last char is excluded from the loop
            val curChar = messagePattern[i]
            if (curChar == ESCAPE_CHAR) {
                escapeCounter++
            } else {
                if (isDelimPair(curChar, messagePattern, i)) { // looks ahead one char
                    i++

                    // write escaped escape chars
                    writeEscapedEscapeChars(escapeCounter, buffer)

                    if (isOdd(escapeCounter)) {
                        // i.e. escaped: write escaped escape chars
                        writeDelimPair(buffer)
                    } else {
                        // unescaped
                        writeArgOrDelimPair(arguments, argCount, currentArgument, buffer)
                        currentArgument++
                    }
                } else {
                    handleLiteralChar(buffer, escapeCounter, curChar)
                }
                escapeCounter = 0
            }
            i++
        }
        handleRemainingCharIfAny(messagePattern, len, buffer, escapeCounter, i)
    }

    /**
     * Returns `true` if the specified char and the char at `curCharIndex + 1` in the specified message
     * pattern together form a "{}" delimiter pair, returns `false` otherwise.
     */
    // Profiling showed this method is important to log4j performance. Modify with care!
    // 22 bytes (allows immediate JVM inlining: < 35 bytes) LOG4J2-1096
    private fun isDelimPair(curChar: Char, messagePattern: String, curCharIndex: Int): Boolean {
        return curChar == DELIM_START && messagePattern[curCharIndex + 1] == DELIM_STOP
    }

    /**
     * Detects whether the message pattern has been fully processed or if an unprocessed character remains and processes
     * it if necessary, returning the resulting position in the result char array.
     */
    // Profiling showed this method is important to log4j performance. Modify with care!
    // 28 bytes (allows immediate JVM inlining: < 35 bytes) LOG4J2-1096
    private fun handleRemainingCharIfAny(
        messagePattern: String, len: Int,
        buffer: StringBuilder, escapeCounter: Int, i: Int
    ) {
        if (i == len - 1) {
            val curChar = messagePattern[i]
            handleLastChar(buffer, escapeCounter, curChar)
        }
    }

    /**
     * Processes the last unprocessed character and returns the resulting position in the result char array.
     */
    // Profiling showed this method is important to log4j performance. Modify with care!
    // 28 bytes (allows immediate JVM inlining: < 35 bytes) LOG4J2-1096
    private fun handleLastChar(buffer: StringBuilder, escapeCounter: Int, curChar: Char) {
        if (curChar == ESCAPE_CHAR) {
            writeUnescapedEscapeChars(escapeCounter + 1, buffer)
        } else {
            handleLiteralChar(buffer, escapeCounter, curChar)
        }
    }

    /**
     * Processes a literal char (neither an '\' escape char nor a "{}" delimiter pair) and returns the resulting
     * position.
     */
    // Profiling showed this method is important to log4j performance. Modify with care!
    // 16 bytes (allows immediate JVM inlining: < 35 bytes) LOG4J2-1096
    private fun handleLiteralChar(buffer: StringBuilder, escapeCounter: Int, curChar: Char) {
        // any other char beside ESCAPE or DELIM_START/STOP-combo
        // write unescaped escape chars
        writeUnescapedEscapeChars(escapeCounter, buffer)
        buffer.append(curChar)
    }

    /**
     * Writes "{}" to the specified result array at the specified position and returns the resulting position.
     */
    // Profiling showed this method is important to log4j performance. Modify with care!
    // 18 bytes (allows immediate JVM inlining: < 35 bytes) LOG4J2-1096
    private fun writeDelimPair(buffer: StringBuilder) {
        buffer.append(DELIM_START)
        buffer.append(DELIM_STOP)
    }

    /**
     * Returns `true` if the specified parameter is odd.
     */
    // Profiling showed this method is important to log4j performance. Modify with care!
    // 11 bytes (allows immediate JVM inlining: < 35 bytes) LOG4J2-1096
    private fun isOdd(number: Int): Boolean {
        return (number and 1) == 1
    }

    /**
     * Writes a '\' char to the specified result array (starting at the specified position) for each *pair* of
     * '\' escape chars encountered in the message format and returns the resulting position.
     */
    // Profiling showed this method is important to log4j performance. Modify with care!
    // 11 bytes (allows immediate JVM inlining: < 35 bytes) LOG4J2-1096
    private fun writeEscapedEscapeChars(escapeCounter: Int, buffer: StringBuilder) {
        val escapedEscapes = escapeCounter shr 1 // divide by two
        writeUnescapedEscapeChars(escapedEscapes, buffer)
    }

    /**
     * Writes the specified number of '\' chars to the specified result array (starting at the specified position) and
     * returns the resulting position.
     */
    // Profiling showed this method is important to log4j performance. Modify with care!
    // 20 bytes (allows immediate JVM inlining: < 35 bytes) LOG4J2-1096
    private fun writeUnescapedEscapeChars(escapeCounter: Int, buffer: StringBuilder) {
        var escapeCounter = escapeCounter
        while (escapeCounter > 0) {
            buffer.append(ESCAPE_CHAR)
            escapeCounter--
        }
    }

    /**
     * Appends the argument at the specified argument index (or, if no such argument exists, the "{}" delimiter pair) to
     * the specified result char array at the specified position and returns the resulting position.
     */
    // Profiling showed this method is important to log4j performance. Modify with care!
    // 25 bytes (allows immediate JVM inlining: < 35 bytes) LOG4J2-1096
    private fun writeArgOrDelimPair(
        arguments: Array<Any>, argCount: Int, currentArgument: Int,
        buffer: StringBuilder
    ) {
        if (currentArgument < argCount) {
            recursiveDeepToString(arguments[currentArgument], buffer, null)
        } else {
            writeDelimPair(buffer)
        }
    }

    /**
     * This method performs a deep toString of the given Object.
     * Primitive arrays are converted using their respective Arrays.toString methods while
     * special handling is implemented for "container types", i.e. Object[], Map and Collection because those could
     * contain themselves.
     *
     *
     * It should be noted that neither AbstractMap.toString() nor AbstractCollection.toString() implement such a
     * behavior. They only check if the container is directly contained in itself, but not if a contained container
     * contains the original one. Because of that, Arrays.toString(Object[]) isn't safe either.
     * Confusing? Just read the last paragraph again and check the respective toString() implementation.
     *
     *
     *
     * This means, in effect, that logging would produce a usable output even if an ordinary System.out.println(o)
     * would produce a relatively hard-to-debug StackOverflowError.
     *
     *
     * @param o The object.
     * @return The String representation.
     */
    fun deepToString(o: Any?): String? {
        if (o == null) {
            return null
        }
        if (o is String) {
            return o
        }
        val str = StringBuilder()
        val dejaVu: MutableSet<String?> = HashSet() // that's actually a neat name ;)
        recursiveDeepToString(o, str, dejaVu)
        return str.toString()
    }

    /**
     * This method performs a deep toString of the given Object.
     * Primitive arrays are converted using their respective Arrays.toString methods while
     * special handling is implemented for "container types", i.e. Object[], Map and Collection because those could
     * contain themselves.
     *
     *
     * dejaVu is used in case of those container types to prevent an endless recursion.
     *
     *
     *
     * It should be noted that neither AbstractMap.toString() nor AbstractCollection.toString() implement such a
     * behavior.
     * They only check if the container is directly contained in itself, but not if a contained container contains the
     * original one. Because of that, Arrays.toString(Object[]) isn't safe either.
     * Confusing? Just read the last paragraph again and check the respective toString() implementation.
     *
     *
     *
     * This means, in effect, that logging would produce a usable output even if an ordinary System.out.println(o)
     * would produce a relatively hard-to-debug StackOverflowError.
     *
     *
     * @param o      the Object to convert into a String
     * @param str    the StringBuilder that o will be appended to
     * @param dejaVu a list of container identities that were already used.
     */
    private fun recursiveDeepToString(o: Any, str: StringBuilder, dejaVu: MutableSet<String?>?) {
        if (appendSpecialTypes(o, str)) {
            return
        }
        if (isMaybeRecursive(o)) {
            appendPotentiallyRecursiveValue(o, str, dejaVu)
        } else {
            tryObjectToString(o, str)
        }
    }

    private fun appendSpecialTypes(o: Any?, str: StringBuilder): Boolean {
        if (o == null || o is String) {
            str.append(o as String?)
            return true
        } else if (o is CharSequence) {
            str.append(o)
            return true
        } else if (o is Int) { // LOG4J2-1415 unbox auto-boxed primitives to avoid calling toString()
            str.append(o)
            return true
        } else if (o is Long) {
            str.append(o)
            return true
        } else if (o is Double) {
            str.append(o)
            return true
        } else if (o is Boolean) {
            str.append(o)
            return true
        } else if (o is Char) {
            str.append(o)
            return true
        } else if (o is Short) {
            str.append(o.toInt())
            return true
        } else if (o is Float) {
            str.append(o)
            return true
        }
        return appendDate(o, str)
    }

    private fun appendDate(o: Any, str: StringBuilder): Boolean {
        if (o !is Date) {
            return false
        }
        str.append(dateTimeFormatter.format(o.toInstant().atZone(ZoneId.systemDefault())))
        return true
    }

    /**
     * Returns `true` if the specified object is an array, a Map or a Collection.
     */
    private fun isMaybeRecursive(o: Any): Boolean {
        return o.javaClass.isArray || o is Map<*, *> || o is Collection<*>
    }

    private fun appendPotentiallyRecursiveValue(
        o: Any, str: StringBuilder,
        dejaVu: MutableSet<String?>?
    ) {
        val oClass: Class<*> = o.javaClass
        if (oClass.isArray) {
            appendArray(o, str, dejaVu, oClass)
        } else if (o is Map<*, *>) {
            appendMap(o, str, dejaVu)
        } else if (o is Collection<*>) {
            appendCollection(o, str, dejaVu)
        }
    }

    private fun appendArray(
        o: Any, str: StringBuilder, dejaVu: MutableSet<String?>?,
        oClass: Class<*>
    ) {
        var dejaVu1 = dejaVu
        if (oClass == ByteArray::class.java) {
            str.append((o as ByteArray).contentToString())
        } else if (oClass == ShortArray::class.java) {
            str.append((o as ShortArray).contentToString())
        } else if (oClass == IntArray::class.java) {
            str.append((o as IntArray).contentToString())
        } else if (oClass == LongArray::class.java) {
            str.append((o as LongArray).contentToString())
        } else if (oClass == FloatArray::class.java) {
            str.append((o as FloatArray).contentToString())
        } else if (oClass == DoubleArray::class.java) {
            str.append((o as DoubleArray).contentToString())
        } else if (oClass == BooleanArray::class.java) {
            str.append((o as BooleanArray).contentToString())
        } else if (oClass == CharArray::class.java) {
            str.append((o as CharArray).contentToString())
        } else {
            if (dejaVu1 == null) {
                dejaVu1 = HashSet()
            }
            // special handling of container Object[]
            val id = identityToString(o)
            if (dejaVu1.contains(id)) {
                str.append(RECURSION_PREFIX).append(id).append(RECURSION_SUFFIX)
            } else {
                dejaVu1.add(id)
                val oArray = o as Array<*>
                str.append('[')
                var first = true
                for (current in oArray) {
                    current ?: continue
                    if (first) {
                        first = false
                    } else {
                        str.append(", ")
                    }
                    recursiveDeepToString(current, str, HashSet(dejaVu1))
                }
                str.append(']')
            }
            //str.append(Arrays.deepToString((Object[]) o));
        }
    }

    private fun appendMap(o: Map<*, *>, str: StringBuilder, dejaVu: MutableSet<String?>?) {
        // special handling of container Map
        var dejaVu1 = dejaVu
        if (dejaVu1 == null) {
            dejaVu1 = HashSet()
        }
        val id = identityToString(o)
        if (dejaVu1.contains(id)) {
            str.append(RECURSION_PREFIX).append(id).append(RECURSION_SUFFIX)
        } else {
            dejaVu1.add(id)
            val oMap = o
            str.append('{')
            var isFirst = true
            for (o1 in oMap.entries) {
                val current = o1 as Map.Entry<*, *>
                if (isFirst) {
                    isFirst = false
                } else {
                    str.append(", ")
                }
                val key = current.key!!
                val value = current.value!!
                recursiveDeepToString(key, str, HashSet(dejaVu1))
                str.append('=')
                recursiveDeepToString(value, str, HashSet(dejaVu1))
            }
            str.append('}')
        }
    }

    private fun appendCollection(o: Collection<*>, str: StringBuilder, dejaVu: MutableSet<String?>?) {
        // special handling of container Collection
        var dejaVu1 = dejaVu
        if (dejaVu1 == null) {
            dejaVu1 = HashSet()
        }
        val id = identityToString(o)
        if (dejaVu1.contains(id)) {
            str.append(RECURSION_PREFIX).append(id).append(RECURSION_SUFFIX)
        } else {
            dejaVu1.add(id)
            str.append('[')
            var isFirst = true
            for (anOCol in o) {
                anOCol ?: continue
                if (isFirst) {
                    isFirst = false
                } else {
                    str.append(", ")
                }
                recursiveDeepToString(anOCol, str, HashSet(dejaVu1))
            }
            str.append(']')
        }
    }

    private fun tryObjectToString(o: Any, str: StringBuilder) {
        // it's just some other Object, we can only use toString().
        try {
            str.append(o.toString())
        } catch (t: Throwable) {
            handleErrorInObjectToString(o, str, t)
        }
    }

    private fun handleErrorInObjectToString(o: Any, str: StringBuilder, t: Throwable) {
        str.append(ERROR_PREFIX)
        str.append(identityToString(o))
        str.append(ERROR_SEPARATOR)
        val msg = t.message
        val className = t.javaClass.name
        str.append(className)
        if (className != msg) {
            str.append(ERROR_MSG_SEPARATOR)
            str.append(msg)
        }
        str.append(ERROR_SUFFIX)
    }

    /**
     * This method returns the same as if Object.toString() would not have been
     * overridden in obj.
     *
     *
     * Note that this isn't 100% secure as collisions can always happen with hash codes.
     *
     *
     *
     * Copied from Object.hashCode():
     *
     * <blockquote>
     * As much as is reasonably practical, the hashCode method defined by
     * class `Object` does return distinct integers for distinct
     * objects. (This is typically implemented by converting the internal
     * address of the object into an integer, but this implementation
     * technique is not required by the Java&#8482; programming language.)
    </blockquote> *
     *
     * @param obj the Object that is to be converted into an identity string.
     * @return the identity string as also defined in Object.toString()
     */
    fun identityToString(obj: Any?): String? {
        if (obj == null) {
            return null
        }
        return obj.javaClass.name + '@' + Integer.toHexString(System.identityHashCode(obj))
    }
}