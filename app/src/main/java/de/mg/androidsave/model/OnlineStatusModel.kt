package de.mg.androidsave.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class OnlineStatusModel(

    @PrimaryKey
    @NonNull
    @ColumnInfo
    var id: Long = 1,

    @NonNull
    @ColumnInfo
    var online: Boolean? = null
)