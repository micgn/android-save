package de.mg.androidsave.model

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import de.mg.androidsave.util.SimpleEncryptor

object InMemoryState {

    private val password = MutableLiveData<String>()
    var startMillis: Long? = null

    fun getPassword() =
        if (password.value != null) SimpleEncryptor.caesarDecrypt(password.value!!) else null

    fun setPassword(pw: String?) {
        password.value = if (pw != null) SimpleEncryptor.caesarEncrypt(pw) else null
        startMillis = if (pw != null) System.currentTimeMillis() else null
    }

    fun observePassword(activity: AppCompatActivity, callback: (pw: String?) -> Unit) {
        password.observe(activity, Observer<String?> { callback(it) })
    }

}