package com.example.cardgame.utils

import android.annotation.SuppressLint
import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData

class Repository {
    private var scoreDAO: ScoreDAO? = null
    private var allScores: LiveData<List<Score>>? = null

    constructor(application: Application) {
        var db: DB? = DB.getInstance(application)
        scoreDAO = db?.scoreDao()
        allScores = scoreDAO?.getAllScores()
    }

    fun insert(score: Score) {
        InsertScoreAT(scoreDAO).execute(score)
    }

    fun delete(score: Score) {
        DeleteScoreAT(scoreDAO).execute(score)
    }

    fun getAllScores(): LiveData<List<Score>>? {
        return allScores
    }

    companion object {
        open class InsertScoreAT(private var scoreDAO: ScoreDAO?) :
            AsyncTask<Score, Void, Void>() {

            override fun doInBackground(vararg params: Score?): Void? {
                params[0]?.let { scoreDAO?.insert(it) }
                return null
            }
        }

        open class DeleteScoreAT(private var scoreDAO: ScoreDAO?) :
            AsyncTask<Score, Void, Void>() {

            override fun doInBackground(vararg params: Score?): Void? {
                params[0]?.let {
                    scoreDAO?.delete(it)
                }
                return null
            }
        }
    }

}