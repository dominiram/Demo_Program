package com.example.cardgame.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Score::class], version = 1)
abstract class DB : RoomDatabase() {

    companion object {
        var instance : DB? = null

        fun getInstance(context: Context): DB? {
            if(instance == null) {
                synchronized(DB::class) {
                    instance = Room.databaseBuilder(context.applicationContext,
                        DB::class.java, "DataBase").fallbackToDestructiveMigration().build()
                }
            }
            return instance
        }
    }

    abstract fun scoreDao() : ScoreDAO
}
