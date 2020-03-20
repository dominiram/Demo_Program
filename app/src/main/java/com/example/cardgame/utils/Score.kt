package com.example.cardgame.utils

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.lang.reflect.Constructor


@Entity(tableName = "score_table")
data class Score (

    @ColumnInfo(name = "name")
    val name : String,

    @ColumnInfo(name = "score")
    val score : Int
) {
    @PrimaryKey(autoGenerate = true)
    var position : Int = 0
}
