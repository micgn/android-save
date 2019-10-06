package de.mg.androidsave.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ServerPasswordModel(

    @PrimaryKey
    @NonNull
    @ColumnInfo
    var id: Long = 1,

    @NonNull
    @ColumnInfo
    var password: String = ""
)