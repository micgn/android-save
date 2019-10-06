package de.mg.androidsave.util

import java.util.*


object SimpleEncryptor {

    private val OFFSET = 4

    fun caesarEncrypt(plain: String): String {
        val b64encoded = Base64.getEncoder().encodeToString(plain.toByteArray())

        // Reverse the string
        val reverse = StringBuffer(b64encoded).reverse().toString()

        val tmp = StringBuilder()
        for (i in reverse.indices) {
            tmp.append((reverse[i].toInt() + OFFSET).toChar())
        }
        return tmp.toString()
    }

    fun caesarDecrypt(secret: String): String? {
        val tmp = StringBuilder()
        for (element in secret) {
            tmp.append((element.toInt() - OFFSET).toChar())
        }

        val reversed = StringBuffer(tmp.toString()).reverse().toString()
        try {
            return String(Base64.getDecoder().decode(reversed))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}