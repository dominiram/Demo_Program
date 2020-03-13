package com.example.cardgame.utils

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScoreDAO {

    @Insert
    fun insert(score : Score)

    @Delete
    fun delete(score: Score)

    @Query("SELECT * FROM score_table ORDER BY position")
    fun getAllScores() : LiveData<List<Score>>
}