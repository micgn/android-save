package de.mg.androidsave.db


import android.app.Application
import androidx.lifecycle.LiveData
import de.mg.androidsave.model.EntryModel
import de.mg.androidsave.model.OnlineStatusModel
import de.mg.androidsave.model.ServerPasswordModel
import de.mg.androidsave.util.SimpleEncryptor


class SaveRepository(application: Application) {

    private val entryModelDao: EntryModelDao
    private val serverPasswordModelDao: ServerPasswordModelDao
    private val onlineStatusModelDao: OnlineStatusModelDao

    val allEntries: LiveData<List<EntryModel>>

    private val db: SaveDatabase = SaveDatabase.getDatabase(application)!!

    init {
        onlineStatusModelDao = db.OnlineStatusModelDao()
        entryModelDao = db.EntryModelDao()
        serverPasswordModelDao = db.serverPasswordModelDao()
        allEntries = entryModelDao.findAll()
    }

    fun exists(name: String) = entryModelDao.find(name) != null

    fun delete(name: String) {
        entryModelDao.delete(name)
    }

    fun update(name: String, entry: String, hash: String) {
        entryModelDao.update(EntryModel(name, entry, hash))
    }

    fun insert(name: String, entry: String, hash: String) {
        entryModelDao.insert(EntryModel(name, entry, hash))
    }


    fun saveServerPassword(pw: String) {
        val encryptedPw = SimpleEncryptor.caesarEncrypt(pw)
        val savedMetaModel = serverPasswordModelDao.find()
        if (savedMetaModel != null) {
            savedMetaModel.password = encryptedPw
            serverPasswordModelDao.update(savedMetaModel)
        } else {
            serverPasswordModelDao.insert(ServerPasswordModel(password = encryptedPw))
        }
    }

    fun getServerPassword(): String? {
        val pw = serverPasswordModelDao.find()
        return if (pw == null) null
        else SimpleEncryptor.caesarDecrypt(pw.password)
    }


    fun isOnline(): LiveData<OnlineStatusModel?> = onlineStatusModelDao.find()
}
