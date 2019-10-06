package de.mg.androidsave.util

import android.text.Editable
import android.text.TextWatcher

object Utils {

    fun textWatcher(listener: (Editable) -> Unit): TextWatcher {
        return object : TextWatcher {

            override fun afterTextChanged(e: Editable) {
                listener(e)
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        }
    }
}