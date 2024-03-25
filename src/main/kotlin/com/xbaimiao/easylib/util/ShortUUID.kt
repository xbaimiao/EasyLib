package com.xbaimiao.easylib.util

import kotlin.random.Random

class ShortUUID(val value: String) : CharSequence {


    companion object {
        fun randomShortUUID(length: Int, uppercase: Boolean): ShortUUID {
            val range = ('a'..'z')
            val numRange = 0..9
            val stringBuilder = StringBuilder()
            repeat(length) {
                if (Random.nextBoolean()) {
                    if (!uppercase || Random.nextBoolean()) {
                        stringBuilder.append(range.random())
                    } else {
                        stringBuilder.append(range.random().uppercase())
                    }
                } else {
                    stringBuilder.append(numRange.random())
                }
            }
            return ShortUUID(stringBuilder.toString())
        }

        fun randomShortUUID(): ShortUUID {
            return randomShortUUID(8, false)
        }

    }

    /**
     * Returns the length of this character sequence.
     */
    override val length: Int
        get() = value.length

    /**
     * Returns the character at the specified [index] in this character sequence.
     *
     * @throws [IndexOutOfBoundsException] if the [index] is out of bounds of this character sequence.
     *
     * Note that the [String] implementation of this interface in Kotlin/JS has unspecified behavior
     * if the [index] is out of its bounds.
     */
    override fun get(index: Int): Char {
        return value[index]
    }

    /**
     * Returns a new character sequence that is a subsequence of this character sequence,
     * starting at the specified [startIndex] and ending right before the specified [endIndex].
     *
     * @param startIndex the start index (inclusive).
     * @param endIndex the end index (exclusive).
     */
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return value.subSequence(startIndex, endIndex)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShortUUID

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value
    }

}
