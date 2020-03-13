package com.example.cardgame.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class ScoreViewModel : AndroidViewModel {

    private var repository : Repository? = null
    private var allScores: LiveData<List<Score>>? = null

    constructor(application: Application) : super(application) {
        repository = Repository(application)
        allScores = repository?.getAllScores()
    }

    fun insert(score: Score) {
        repository?.insert(score)
    }

    fun delete(score: Score) {
        repository?.delete(score)
    }

    fun getAllNotes() : LiveData<List<Score>>? {
        return allScores
    }
}