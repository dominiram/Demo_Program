package com.example.cardgame.utils

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "score_table")
data class Score (

    @PrimaryKey(autoGenerate = true)
    val position : Int,

    @ColumnInfo(name = "name")
    val name : String,

    @ColumnInfo(name = "score")
    val score : Int
)
