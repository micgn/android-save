package de.mg.androidsave.db


import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.mg.androidsave.model.EntryModel

@Dao
interface EntryModelDao {

    @Query("SELECT * FROM EntryModel ORDER BY name ASC")
    fun findAll(): LiveData<List<EntryModel>>

    @Query("SELECT * FROM EntryModel WHERE name = :name")
    fun find(name: String): EntryModel?

    @Insert
    fun insert(enryModel: EntryModel)

    @Update
    fun update(enryModel: EntryModel)

    @Query("DELETE FROM EntryModel")
    fun deleteAll()

    @Query("DELETE FROM EntryModel WHERE name = :name")
    fun delete(name: String)
}
