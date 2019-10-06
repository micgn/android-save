package de.mg.androidsave.server

import android.content.Context
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.mg.androidsave.db.EntryModelDao
import de.mg.androidsave.db.SaveDatabase
import de.mg.androidsave.model.EntryModel


class ServerResponseHandler(appContext: Context) {

    private val mapper = ObjectMapper()
    private val db: SaveDatabase = SaveDatabase.getDatabase(appContext)!!

    private val entryDao: EntryModelDao

    init {
        entryDao = db.EntryModelDao()
    }

    fun handle(method: String, path: String, responseStr: String) {

        if (method == "GET" && path == "entry") {

            val entries = mapper.readValue<List<EntryModel>>(responseStr,
                object : TypeReference<List<EntryModel>>() {})

            entryDao.deleteAll()
            entries.forEach { entryDao.insert(it) }
        }

    }
}