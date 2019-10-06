package de.mg.androidsave.db


import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.mg.androidsave.model.OnlineStatusModel

@Dao
interface OnlineStatusModelDao {

    @Query("SELECT * FROM OnlineStatusModel WHERE id=1")
    fun findImmediate(): OnlineStatusModel?

    @Query("SELECT * FROM OnlineStatusModel WHERE id=1")
    fun find(): LiveData<OnlineStatusModel?>

    @Insert
    fun insert(model: OnlineStatusModel)

    @Update
    fun update(model: OnlineStatusModel)

    @Query("DELETE FROM OnlineStatusModel WHERE id=1")
    fun delete()

}
