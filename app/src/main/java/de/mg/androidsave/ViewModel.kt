package de.mg.androidsave

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.mg.androidsave.db.SaveRepository
import de.mg.androidsave.model.EntryModel
import de.mg.androidsave.model.InMemoryState
import de.mg.androidsave.model.OnlineStatusModel
import de.mg.androidsave.server.CallServerWorkManager
import de.mg.androidsave.util.EncryptionUtil


class ViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SaveRepository(application)
    private val workManager = CallServerWorkManager()

    val online: LiveData<OnlineStatusModel?>
    val entries: LiveData<List<EntryModel>>

    val search = MutableLiveData<String>()
    val filteredEntries = MutableLiveData<List<EntryModel>>()


    init {
        online = repo.isOnline()
        entries = repo.allEntries
        entries.observeForever { updateFilteredEntries() }
    }

    fun updateFilteredEntries() {
        filteredEntries.value =
            entries.value?.filter { it.name.contains(search.value.orEmpty(), true) }
                ?.sortedBy { it.name.toLowerCase() }
    }


    fun delete(name: String): Boolean {
        val pw = getServerPassword()
        return if (pw != null) {
            workManager.delete(name, pw, getApplication())
            repo.delete(name)
            true
        } else false
    }

    fun update(entryName: String, text: String): Boolean {
        val pw = getServerPassword()
        val clientPw = InMemoryState.getPassword()
        return if (pw != null && clientPw != null && repo.exists(entryName)) {
            val hash = EncryptionUtil.md5(text)
            val encryptedText = EncryptionUtil.encrypt(clientPw, text)
            workManager.update(EntryModel(entryName, encryptedText, hash), pw, getApplication())
            repo.update(entryName, encryptedText, hash)
            true
        } else false
    }

    fun insert(entryName: String, text: String): Boolean {
        val pw = getServerPassword()
        val clientPw = InMemoryState.getPassword()
        return if (pw != null && clientPw != null && !repo.exists(entryName)) {
            val hash = EncryptionUtil.md5(text)
            val encryptedText = EncryptionUtil.encrypt(clientPw, text)
            workManager.create(EntryModel(entryName, encryptedText, hash), pw, getApplication())
            repo.insert(entryName, encryptedText, hash)
            true
        } else false
    }

    fun changeName(old: String, newName: String, text: String): Boolean {
        if (repo.exists(newName)) return false
        return delete(old) && insert(newName, text)
    }

    fun setServerPassword(pw: String) {
        repo.saveServerPassword(pw)
    }

    fun getServerPassword() = repo.getServerPassword()

    fun isClientPasswordCorrect(pw: String): Boolean {
        if (entries.value.isNullOrEmpty()) return true
        val someEntry = entries.value!![0]
        return EncryptionUtil.decrypt(pw, someEntry.entry, someEntry.hash) != null
    }

}
