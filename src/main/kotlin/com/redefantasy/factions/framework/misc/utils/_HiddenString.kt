package com.redefantasy.factions.framework.misc.utils

/**
 * @author Gutyerrez
 */
object _HiddenString {

    private val AVAILABLE_COLOR_CHARS = arrayOf(
        'a', 'b', 'c', 'd', 'f', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    )

    fun generate(size: Int = 3): String {
        val stringBuilder = StringBuilder()

        AVAILABLE_COLOR_CHARS.shuffle()

        do {
            var i = 0

            val char = AVAILABLE_COLOR_CHARS[
                    if (i > AVAILABLE_COLOR_CHARS.size) {
                        AVAILABLE_COLOR_CHARS.size
                    } else i
            ]

            stringBuilder.append("§$char")

            i++
        } while (i < size)

        return stringBuilder.toString()
    }

}