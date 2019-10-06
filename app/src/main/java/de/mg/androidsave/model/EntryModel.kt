package de.mg.androidsave.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class EntryModel(

    @PrimaryKey
    @NonNull
    @ColumnInfo
    var name: String = "",

    @NonNull
    @ColumnInfo
    var entry: String = "",

    @NonNull
    @ColumnInfo
    var hash: String = ""
)