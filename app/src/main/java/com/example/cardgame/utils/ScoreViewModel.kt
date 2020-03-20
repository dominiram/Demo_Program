package com.example.cardgame.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class ScoreViewModel : AndroidViewModel {

    private var repository : Repository? = null
    private var allScores: List<Score>? = null

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

    fun getAllNotes() : List<Score>? {
        return allScores
    }

    fun endTheGame(currentScore: Int, name: String) {
        var pos = -1
        if (allScores != null) {
            for (sc in allScores!!) {
                if(sc.score < currentScore) {
                    pos = sc.position
                    break
                }
            }
        }
        if(allScores == null || allScores!!.isEmpty() || pos != -1) {
            if(pos != -1) {
                //todo this method should be called from highscores fragment and recieve name
                // and current score (score from gameplay fragments bundle/intent/arguments)
                insert(Score(name, currentScore))
                if(allScores?.size!! > 10) run {
                    var index = 0
                    var min = allScores?.get(0)?.score
                    for(score in allScores!!){
                        if(score.score < min!!) {
                            min = score.score
                            index = score.position
                        }
                    }
                    val sc: Score = allScores!![index]
                    delete(sc)
                }
            }
            else insert(Score(name, currentScore))
        }
        //todo create fragment, and fragment layout. Push fragment on backstack, get the
        // name from the player, then call this method, after that, pop the fragment
        // from the backstack and return to gameplay fragment to pop it as well
    }

    fun isScoreInTopTen(score: Int) : Boolean {
        var res = false
        if(allScores == null || allScores!!.isEmpty() || allScores!!.size < 10) {
            return true
        }
        for(sc in allScores!!) {
            if (sc.score < score)
                res = true
        }
        return res
    }
}