package de.mg.androidsave.db


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.mg.androidsave.model.ServerPasswordModel

@Dao
interface ServerPasswordModelDao {

    @Query("SELECT * FROM ServerPasswordModel WHERE id=1")
    fun find(): ServerPasswordModel?

    @Insert
    fun insert(model: ServerPasswordModel)

    @Update
    fun update(model: ServerPasswordModel)

    @Query("DELETE FROM ServerPasswordModel WHERE id=1")
    fun delete()

}
