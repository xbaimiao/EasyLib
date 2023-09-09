package com.xbaimiao.easylib.util

object Strings {

    /**
     * 获取两段文本的相似度（0.0~1.0)
     *
     * @param strA 文本
     * @param strB 文本
     * @return double
     */
    fun similarDegree(strA: String, strB: String): Double {
        val newStrA = removeSign(max(strA, strB))
        val newStrB = removeSign(min(strA, strB))
        return try {
            val temp = kotlin.math.max(newStrA.length.toDouble(), newStrB.length.toDouble()).toInt()
            val temp2 = longestCommonSubstring(newStrA, newStrB).length
            temp2 * 1.0 / temp
        } catch (ignored: Exception) {
            0.0
        }
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (aByte in bytes) {
            val hex = Integer.toHexString(0xFF and aByte.toInt())
            if (hex.length == 1) {
                sb.append('0')
            }
            sb.append(hex)
        }
        return sb.toString()
    }

    private fun max(strA: String, strB: String): String {
        return if (strA.length >= strB.length) strA else strB
    }

    private fun min(strA: String, strB: String): String {
        return if (strA.length < strB.length) strA else strB
    }

    private fun removeSign(str: String): String {
        val builder = StringBuilder()
        for (item in str.toCharArray()) {
            if (charReg(item)) {
                builder.append(item)
            }
        }
        return builder.toString()
    }

    private fun charReg(charValue: Char): Boolean {
        return charValue.code >= 0x4E00 && charValue.code <= 0X9FA5 || charValue >= 'a' && charValue <= 'z' || charValue >= 'A' && charValue <= 'Z' || charValue >= '0' && charValue <= '9'
    }

    private fun longestCommonSubstring(strA: String, strB: String): String {
        val chars_strA = strA.toCharArray()
        val chars_strB = strB.toCharArray()
        var m = chars_strA.size
        var n = chars_strB.size
        val matrix = Array(m + 1) { IntArray(n + 1) }
        for (i in 1..m) {
            for (j in 1..n) {
                if (chars_strA[i - 1] == chars_strB[j - 1]) {
                    matrix[i][j] = matrix[i - 1][j - 1] + 1
                } else {
                    matrix[i][j] = kotlin.math.max(matrix[i][j - 1].toDouble(), matrix[i - 1][j].toDouble())
                        .toInt()
                }
            }
        }
        val result = CharArray(matrix[m][n])
        var currentIndex = result.size - 1
        while (matrix[m][n] != 0) {
            if (matrix[n] == matrix[n - 1]) {
                n--
            } else if (matrix[m][n] == matrix[m - 1][n]) {
                m--
            } else {
                result[currentIndex] = chars_strA[m - 1]
                currentIndex--
                n--
                m--
            }
        }
        return String(result)
    }

}
