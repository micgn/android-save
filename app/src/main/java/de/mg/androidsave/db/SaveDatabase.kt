package de.mg.androidsave.db


import android.content.Context
import android.os.AsyncTask
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import de.mg.androidsave.Config
import de.mg.androidsave.model.EntryModel
import de.mg.androidsave.model.OnlineStatusModel
import de.mg.androidsave.model.ServerPasswordModel


@Database(
    entities = [EntryModel::class, ServerPasswordModel::class, OnlineStatusModel::class],
    version = 4
)
abstract class SaveDatabase : RoomDatabase() {

    abstract fun EntryModelDao(): EntryModelDao
    abstract fun serverPasswordModelDao(): ServerPasswordModelDao
    abstract fun OnlineStatusModelDao(): OnlineStatusModelDao

    private class PopulateDbAsync internal constructor(db: SaveDatabase) :
        AsyncTask<Void, Void, Void>() {

        private val entryModelDao: EntryModelDao = db.EntryModelDao()

        override fun doInBackground(vararg params: Void): Void? {

            if (!Config.DEV_MODE) return null

            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            entryModelDao.deleteAll()

            (1..25).forEach {
                INSTANCE?.EntryModelDao()?.insert(EntryModel("name $it", "text $it text text", "1"))
            }

            return null
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: SaveDatabase? = null

        fun getDatabase(context: Context): SaveDatabase? {
            if (INSTANCE == null) {
                synchronized(SaveDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            SaveDatabase::class.java,
                            "save_database"
                        )
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            .fallbackToDestructiveMigration()
                            // not recommended:
                            .allowMainThreadQueries()

                            //pre-populate data:
                            .addCallback(databaseCallback)
                            .build()
                    }
                }
            }
            return INSTANCE
        }

        private val databaseCallback = object : RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                PopulateDbAsync(INSTANCE!!).execute()
            }
        }


    }

}
